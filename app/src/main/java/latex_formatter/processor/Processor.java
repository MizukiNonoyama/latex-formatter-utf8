package latex_formatter.processor;

import latex_formatter.config.Config;
import latex_formatter.config.ConfigManager;
import latex_formatter.structure.*;

import java.util.*;

public class Processor {
    public static List<String> process(List<String> input) {
        List<String> result = new ArrayList<>();
        try {
            // PreInit
            List<Config.TexFullBlockPair> texFullBlockPairs = Arrays.asList(ConfigManager.getInstance().getConfig().searcher.texFullBlockRegex);
            //List<Config.TexBlockRegexPair> texHalfBlockNoEndLineBreaks = Arrays.asList(ConfigManager.getInstance().getConfig().searcher.texHalfBlockRegexNoEndLineBreaks);
            List<Config.TexBlockRegexPair> texHalfBlockEndLineBreaks = Arrays.asList(ConfigManager.getInstance().getConfig().searcher.texHalfBlockRegex);
            List<Config.TexBlockRegexPair> texHalfBlockFixedIndentEndLineBreaks = Arrays.asList(ConfigManager.getInstance().getConfig().searcher.texHalfBlockRegexFixedIndent);
            //List<Config.TexBlockRegexPair> texHalfBlockFixedIndentNoEndLineBreaks = Arrays.asList(ConfigManager.getInstance().getConfig().searcher.texHalfBlockRegexFixedIndentNoEndLineBreaks);
            List<Config.TexBlockRegexPair> endLineBreaks = new ArrayList<>();
            endLineBreaks.addAll(texHalfBlockEndLineBreaks);
            endLineBreaks.addAll(texHalfBlockFixedIndentEndLineBreaks);
            List<Config.TexBlockRegexPair> fixedIndents = new ArrayList<>();
            fixedIndents.addAll(texHalfBlockFixedIndentEndLineBreaks);
            //fixedIndents.addAll(texHalfBlockFixedIndentNoEndLineBreaks);
            List<Config.TexBlockRegexPair> allHalfBlocks = new ArrayList<>();
            allHalfBlocks.addAll(texHalfBlockEndLineBreaks);
            //allHalfBlocks.addAll(texHalfBlockNoEndLineBreaks);
            allHalfBlocks.addAll(texHalfBlockFixedIndentEndLineBreaks);
            //allHalfBlocks.addAll(texHalfBlockFixedIndentNoEndLineBreaks);
            List<Config.TexBlockRegexPair> regexPairs = new ArrayList<>();
            regexPairs.addAll(allHalfBlocks);
            for (Config.TexFullBlockPair pair : texFullBlockPairs) {
                regexPairs.add(pair.primary);
                regexPairs.add(pair.end);
            }

            Deque<IncompletedTexBlock> processingTexBlocks = new ArrayDeque<>();
            List<TexBlock> tempResults = new ArrayList<>();
            Deque<String> inputQueue = new ArrayDeque<>(input);

            // Processing
            String processing = inputQueue.pollFirst();
            while (processing != null) {
                int min = Integer.MAX_VALUE;
                Pair<Config.TexBlockRegexPair, Integer> minRegex = null;
                for (Config.TexBlockRegexPair regexPair : regexPairs) {
                    int value = StructureUtils.find(processing, regexPair.primaryRegex);
                    if (value < 0) {
                        continue;
                    }
                    if (min > value) {
                        min = value;
                        minRegex = new Pair<>(regexPair, value);
                    }
                }
                Deque<IncompletedTexBlock> tempStorage = new ArrayDeque<>();
                if (minRegex != null) {
                    // TODO while
                    while (!processingTexBlocks.isEmpty()) {
                        IncompletedTexBlock processingTexBlock = processingTexBlocks.getLast();
                        if (!processingTexBlock.exteriorRegex.isEmpty() &&
                                !processingTexBlock.exteriorRegex.endRegex.isEmpty()) {
                            int value = StructureUtils.find(processing, processingTexBlock.exteriorRegex.endRegex);
                            if (value >= 0 && value < minRegex.getSecond()) {
                                minRegex = new Pair<>(minRegex.getFirst(), minRegex.getSecond() - value -
                                        processingTexBlock.exteriorRegex.endRegex.length());
                                Pair<String, String> processingPair =
                                        StructureUtils.split(processing, processingTexBlock.exteriorRegex.endRegex);
                                processingTexBlock = processingTexBlocks.pollLast();
                                if (!processingPair.getFirst().isEmpty()) {
                                    processingTexBlock.objects.add(processingPair.getFirst());
                                }
                                processingTexBlock.endRegex = processingTexBlock.exteriorRegex.endRegex;
                                if (allHalfBlocks.contains(processingTexBlock.exteriorRegex) &&
                                        !fixedIndents.contains(processingTexBlock.exteriorRegex)) {
                                    TexBlock block = processingTexBlock.getFixed();
                                    if (!processingTexBlocks.isEmpty()) {
                                        processingTexBlock = processingTexBlocks.getLast();
                                        if (processingTexBlock.startRegex instanceof TexBlock &&
                                                ((TexBlock) processingTexBlock.startRegex).isEquals(block)) {
                                            processingTexBlock = processingTexBlocks.pollLast();
                                            if (!processingTexBlocks.isEmpty()) {
                                                TexBlock block1 = processingTexBlock.getFixed();
                                                processingTexBlocks.getLast().objects.add(block1);
                                            } else {
                                                tempResults.add(processingTexBlock.getFixed());
                                            }
                                        }
                                    }
                                    IncompletedTexBlock incompletedTexBlock = new IncompletedTexBlock(block,
                                            Config.TexBlockRegexPair.EMPTY, new ArrayList<>(),
                                            endLineBreaks.contains(block.exteriorRegex));
                                    processingTexBlocks.add(incompletedTexBlock);
                                } else if (allHalfBlocks.contains(processingTexBlock.exteriorRegex) &&
                                        fixedIndents.contains(processingTexBlock.exteriorRegex)) {
                                    // [\chapter{, '}']
                                    TexBlock block = processingTexBlock.getFixed();
                                    if (!processingTexBlocks.isEmpty()) {
                                        boolean isFirstAppear = true;
                                        for (IncompletedTexBlock incompletedTexBlock : processingTexBlocks) {
                                            if (incompletedTexBlock.startRegex instanceof TexBlock &&
                                                    ((TexBlock) incompletedTexBlock.startRegex).isEquals(block)) {
                                                isFirstAppear = false;
                                                break;
                                            }
                                        }
                                        if (!isFirstAppear) {
                                            while (!processingTexBlocks.isEmpty()) {
                                                processingTexBlock = processingTexBlocks.pollLast();
                                                if (processingTexBlock.startRegex instanceof TexBlock &&
                                                        ((TexBlock) processingTexBlock.startRegex).isEquals(block)) {
                                                    if (!processingTexBlocks.isEmpty()) {
                                                        TexBlock block1 = processingTexBlock.getFixed();
                                                        processingTexBlocks.getLast().objects.add(block1);
                                                    } else {
                                                        tempResults.add(processingTexBlock.getFixed());
                                                    }
                                                    break;
                                                } else if (!processingTexBlocks.isEmpty()) {
                                                    processingTexBlocks.getLast().objects.add(
                                                            processingTexBlock.getFixed());
                                                }
                                            }
                                        }
                                    }
                                    IncompletedTexBlock incompletedTexBlock = new IncompletedTexBlock(block,
                                            Config.TexBlockRegexPair.EMPTY, new ArrayList<>(),
                                            endLineBreaks.contains(block.exteriorRegex));
                                    processingTexBlocks.add(incompletedTexBlock);
                                } else {
                                    TexBlock block = processingTexBlock.getFixed();
                                    // [\begin{, '}'] || [\end{, '}']
                                    boolean isPrimary = false;
                                    Config.TexFullBlockPair fullBlockPair1 = null;
                                    for (Config.TexFullBlockPair fullBlockPair : texFullBlockPairs) {
                                        if (block.isStartOf(fullBlockPair)) {
                                            isPrimary = true;
                                            fullBlockPair1 = fullBlockPair;
                                            break;
                                        }
                                        if (block.isEndOf(fullBlockPair)) {
                                            fullBlockPair1 = fullBlockPair;
                                            break;
                                        }
                                    }
                                    if (fullBlockPair1 != null) {
                                        if (isPrimary) {
                                            IncompletedTexBlock incompletedTexBlock = new IncompletedTexBlock(block,
                                                    Config.TexBlockRegexPair.EMPTY, new ArrayList<>(), true);
                                            processingTexBlocks.add(incompletedTexBlock);
                                        } else {
                                            boolean isFirstAppear = true;
                                            for (IncompletedTexBlock incompletedTexBlock : processingTexBlocks) {
                                                if (incompletedTexBlock.startRegex instanceof TexBlock &&
                                                        ((TexBlock) incompletedTexBlock.startRegex).isStartOf(
                                                                fullBlockPair1) &&
                                                        ((TexBlock) incompletedTexBlock.startRegex).isSameInteriorOf(
                                                                block)) {
                                                    isFirstAppear = false;
                                                    break;
                                                }
                                            }
                                            if (!isFirstAppear) {
                                                while (!processingTexBlocks.isEmpty()) {
                                                    processingTexBlock = processingTexBlocks.pollLast();
                                                    if (processingTexBlock.startRegex instanceof TexBlock &&
                                                            ((TexBlock) processingTexBlock.startRegex).isStartOf(
                                                                    fullBlockPair1) &&
                                                            ((TexBlock) processingTexBlock.startRegex).isSameInteriorOf(
                                                                    block)) {
                                                        processingTexBlock.endRegex = block;
                                                        if (!processingTexBlocks.isEmpty()) {
                                                            processingTexBlocks.getLast().objects.add(
                                                                    processingTexBlock.getFixed());
                                                        } else {
                                                            tempResults.add(processingTexBlock.getFixed());
                                                        }
                                                        break;
                                                    } else if (!processingTexBlocks.isEmpty()) {
                                                        processingTexBlocks.getLast().objects.add(
                                                                processingTexBlock.getFixed());
                                                    }
                                                }
                                            } else {
                                                processingTexBlocks.getLast().objects.add(block);
                                            }
                                        }
                                    } else {
                                        // DO NOTHING
                                    }
                                }
                                processing = processingPair.getSecond();
                                tempStorage.clear();
                                continue;
                            }
                        }
                        tempStorage.addFirst(processingTexBlocks.pollLast());
                    }
                    processingTexBlocks.addAll(tempStorage);
                    Pair<String, String> pair = StructureUtils.split(processing, minRegex.getFirst().primaryRegex);
                    if (processingTexBlocks.isEmpty()) {
                        if (!pair.getFirst().isEmpty()) {
                            tempResults.add(new TexBlock(pair.getFirst()));
                        }
                        IncompletedTexBlock block =
                                new IncompletedTexBlock(minRegex.getFirst().primaryRegex, "", new ArrayList<>(), false);
                        block.exteriorRegex = minRegex.getFirst();
                        processing = pair.getSecond();
                        processingTexBlocks.add(block);
                    } else {
                        IncompletedTexBlock processingTexBlock = processingTexBlocks.getLast();
                        if (!pair.getFirst().isEmpty()) {
                            processingTexBlock.objects.add(pair.getFirst());
                        }
                        processing = pair.getSecond();
                        if (minRegex.getFirst().endRegex.isEmpty()) {
                            if (allHalfBlocks.contains(minRegex.getFirst()) &&
                                    !fixedIndents.contains(minRegex.getFirst())) {
                                // \item
                                if (processingTexBlock.exteriorRegex.primaryRegex.equals(
                                        minRegex.getFirst().primaryRegex)) {
                                    processingTexBlock = processingTexBlocks.pollLast();
                                    if (!processingTexBlocks.isEmpty()) {
                                        TexBlock block1 = processingTexBlock.getFixed();
                                        processingTexBlocks.getLast().objects.add(block1);
                                    } else {
                                        tempResults.add(processingTexBlock.getFixed());
                                    }
                                }
                                IncompletedTexBlock incompletedTexBlock =
                                        new IncompletedTexBlock(minRegex.getFirst().primaryRegex, "", new ArrayList<>(),
                                                endLineBreaks.contains(minRegex.getFirst()));
                                incompletedTexBlock.exteriorRegex = minRegex.getFirst();
                                processingTexBlocks.add(incompletedTexBlock);
                            } else if (allHalfBlocks.contains(minRegex.getFirst()) &&
                                    fixedIndents.contains(minRegex.getFirst())) {
                                boolean isFirstAppear = true;
                                for (IncompletedTexBlock incompletedTexBlock : processingTexBlocks) {
                                    if (incompletedTexBlock.exteriorRegex.primaryRegex.equals(
                                            minRegex.getFirst().primaryRegex)) {
                                        isFirstAppear = false;
                                        break;
                                    }
                                }
                                if (!isFirstAppear) {
                                    while (!processingTexBlocks.isEmpty()) {
                                        processingTexBlock = processingTexBlocks.pollLast();
                                        if (processingTexBlock.exteriorRegex.primaryRegex.equals(
                                                minRegex.getFirst().primaryRegex)) {
                                            if (!processingTexBlocks.isEmpty()) {
                                                TexBlock block1 = processingTexBlock.getFixed();
                                                processingTexBlocks.getLast().objects.add(block1);
                                            } else {
                                                tempResults.add(processingTexBlock.getFixed());
                                            }
                                            break;
                                        } else if (!processingTexBlocks.isEmpty()) {
                                            processingTexBlocks.getLast().objects.add(processingTexBlock.getFixed());
                                        }
                                    }
                                }
                                IncompletedTexBlock incompletedTexBlock =
                                        new IncompletedTexBlock(minRegex.getFirst().primaryRegex, "", new ArrayList<>(),
                                                endLineBreaks.contains(minRegex.getFirst()));
                                incompletedTexBlock.exteriorRegex = minRegex.getFirst();
                                processingTexBlocks.add(incompletedTexBlock);
                            } else {
                                // TODO \itemのような使い方でかつbeginとendのある処理を書く(現状不必要)
                            }
                        } else {
                            IncompletedTexBlock incompletedTexBlock =
                                    new IncompletedTexBlock(minRegex.getFirst().primaryRegex, "", new ArrayList<>(),
                                            false);
                            incompletedTexBlock.exteriorRegex = minRegex.getFirst();
                            processingTexBlocks.add(incompletedTexBlock);
                        }
                    }
                } else {
                    while (!processingTexBlocks.isEmpty()) {
                        IncompletedTexBlock processingTexBlock = processingTexBlocks.getLast();
                        if (!processingTexBlock.exteriorRegex.isEmpty() &&
                                !processingTexBlock.exteriorRegex.endRegex.isEmpty()) {
                            int value = StructureUtils.find(processing, processingTexBlock.exteriorRegex.endRegex);
                            if (value >= 0) {
                                Pair<String, String> processingPair =
                                        StructureUtils.split(processing, processingTexBlock.exteriorRegex.endRegex);
                                processingTexBlock = processingTexBlocks.pollLast();
                                if (!processingPair.getFirst().isEmpty()) {
                                    processingTexBlock.objects.add(processingPair.getFirst());
                                }
                                processingTexBlock.endRegex = processingTexBlock.exteriorRegex.endRegex;
                                if (allHalfBlocks.contains(processingTexBlock.exteriorRegex) &&
                                        !fixedIndents.contains(processingTexBlock.exteriorRegex)) {
                                    TexBlock block = processingTexBlock.getFixed();
                                    if (!processingTexBlocks.isEmpty()) {
                                        processingTexBlock = processingTexBlocks.getLast();
                                        if (processingTexBlock.startRegex instanceof TexBlock &&
                                                ((TexBlock) processingTexBlock.startRegex).isEquals(block)) {
                                            processingTexBlock = processingTexBlocks.pollLast();
                                            if (!processingTexBlocks.isEmpty()) {
                                                TexBlock block1 = processingTexBlock.getFixed();
                                                processingTexBlocks.getLast().objects.add(block1);
                                            } else {
                                                tempResults.add(processingTexBlock.getFixed());
                                            }
                                        }
                                    }
                                    IncompletedTexBlock incompletedTexBlock = new IncompletedTexBlock(block,
                                            Config.TexBlockRegexPair.EMPTY, new ArrayList<>(),
                                            endLineBreaks.contains(block.exteriorRegex));
                                    processingTexBlocks.add(incompletedTexBlock);
                                } else if (allHalfBlocks.contains(processingTexBlock.exteriorRegex) &&
                                        fixedIndents.contains(processingTexBlock.exteriorRegex)) {
                                    // [\chapter{, '}']
                                    TexBlock block = processingTexBlock.getFixed();
                                    if (!processingTexBlocks.isEmpty()) {
                                        boolean isFirstAppear = true;
                                        for (IncompletedTexBlock incompletedTexBlock : processingTexBlocks) {
                                            if (incompletedTexBlock.startRegex instanceof TexBlock &&
                                                    ((TexBlock) incompletedTexBlock.startRegex).isEquals(block)) {
                                                isFirstAppear = false;
                                                break;
                                            }
                                        }
                                        if (!isFirstAppear) {
                                            while (!processingTexBlocks.isEmpty()) {
                                                processingTexBlock = processingTexBlocks.pollLast();
                                                if (processingTexBlock.startRegex instanceof TexBlock &&
                                                        ((TexBlock) processingTexBlock.startRegex).isEquals(block)) {
                                                    if (!processingTexBlocks.isEmpty()) {
                                                        TexBlock block1 = processingTexBlock.getFixed();
                                                        processingTexBlocks.getLast().objects.add(block1);
                                                    } else {
                                                        tempResults.add(processingTexBlock.getFixed());
                                                    }
                                                    break;
                                                } else if (!processingTexBlocks.isEmpty()) {
                                                    processingTexBlocks.getLast().objects.add(
                                                            processingTexBlock.getFixed());
                                                }
                                            }
                                        }
                                    }
                                    IncompletedTexBlock incompletedTexBlock = new IncompletedTexBlock(block,
                                            Config.TexBlockRegexPair.EMPTY, new ArrayList<>(),
                                            endLineBreaks.contains(block.exteriorRegex));
                                    processingTexBlocks.add(incompletedTexBlock);
                                } else {
                                    TexBlock block = processingTexBlock.getFixed();
                                    // [\begin{, '}'] || [\end{, '}']
                                    boolean isPrimary = false;
                                    Config.TexFullBlockPair fullBlockPair1 = null;
                                    for (Config.TexFullBlockPair fullBlockPair : texFullBlockPairs) {
                                        if (block.isStartOf(fullBlockPair)) {
                                            isPrimary = true;
                                            fullBlockPair1 = fullBlockPair;
                                            break;
                                        }
                                        if (block.isEndOf(fullBlockPair)) {
                                            fullBlockPair1 = fullBlockPair;
                                            break;
                                        }
                                    }
                                    if (fullBlockPair1 != null) {
                                        if (isPrimary) {
                                            IncompletedTexBlock incompletedTexBlock = new IncompletedTexBlock(block,
                                                    Config.TexBlockRegexPair.EMPTY, new ArrayList<>(), true);
                                            processingTexBlocks.add(incompletedTexBlock);
                                        } else {
                                            boolean isFirstAppear = true;
                                            for (IncompletedTexBlock incompletedTexBlock : processingTexBlocks) {
                                                if (incompletedTexBlock.startRegex instanceof TexBlock &&
                                                        ((TexBlock) incompletedTexBlock.startRegex).isStartOf(
                                                                fullBlockPair1) &&
                                                        ((TexBlock) incompletedTexBlock.startRegex).isSameInteriorOf(
                                                                block)) {
                                                    isFirstAppear = false;
                                                    break;
                                                }
                                            }
                                            if (!isFirstAppear) {
                                                while (!processingTexBlocks.isEmpty()) {
                                                    processingTexBlock = processingTexBlocks.pollLast();
                                                    if (processingTexBlock.startRegex instanceof TexBlock &&
                                                            ((TexBlock) processingTexBlock.startRegex).isStartOf(
                                                                    fullBlockPair1) &&
                                                            ((TexBlock) processingTexBlock.startRegex).isSameInteriorOf(
                                                                    block)) {
                                                        processingTexBlock.endRegex = block;
                                                        if (!processingTexBlocks.isEmpty()) {
                                                            processingTexBlocks.getLast().objects.add(
                                                                    processingTexBlock.getFixed());
                                                        } else {
                                                            tempResults.add(processingTexBlock.getFixed());
                                                        }
                                                        break;
                                                    } else if (!processingTexBlocks.isEmpty()) {
                                                        processingTexBlocks.getLast().objects.add(
                                                                processingTexBlock.getFixed());
                                                    }
                                                }
                                            } else {
                                                processingTexBlocks.getLast().objects.add(block);
                                            }
                                        }
                                    } else {
                                        // DO NOTHING
                                    }
                                }
                                processing = processingPair.getSecond();
                                tempStorage.clear();
                                continue;
                            }
                        }
                        tempStorage.addFirst(processingTexBlocks.pollLast());
                    }
                    processingTexBlocks.addAll(tempStorage);
                    if (!inputQueue.isEmpty()) {
                        processing += "\n" + inputQueue.pollFirst();
                    } else {
                        processing = null;
                    }
                }
            }

            List<IndentedTexBlock> resultIndent = tryIndent(tempResults);
            resultIndent = tryTextFormat(resultIndent);
            result = StructureUtils.buildText(resultIndent, ConfigManager.getInstance().getConfig().indents.indentStr);
        } catch (NullPointerException e) {
            throw new RuntimeException(e);
        }

        if (result.isEmpty()) {
            return input;
        }
        return result;
    }

    public static List<IndentedTexBlock> tryIndent(List<TexBlock> allContents) {
        List<IndentedTexBlock> tempResult = new ArrayList<>();
        for (TexBlock object : allContents) {
            if (ConfigManager.getInstance().getConfig().indents.isEnableIndentTexBlocks) {
                tempResult.add(StructureUtils.makeIndentedTexBlock(object, 0));
            } else {
                tempResult.add(StructureUtils.makeNonIndentedTexBlock(object));
            }
        }
        return tempResult;
    }

    public static List<IndentedTexBlock> tryTextFormat(List<IndentedTexBlock> allContents) {
        final boolean keepOriginalLineBreak = ConfigManager.getInstance().getConfig().lineBreaks.keepOriginalLineBreaks;
        final boolean keepOriginalSpaces = ConfigManager.getInstance().getConfig().lineBreaks.keepOriginalSpaces;
        final boolean enableTextWrap = ConfigManager.getInstance().getConfig().lineBreaks.isEnableModifyLineBreaks;
        final int wrapSize = ConfigManager.getInstance().getConfig().lineBreaks.textWrapColumns;
        final boolean afterIndent = ConfigManager.getInstance().getConfig().lineBreaks.wrapAfterIndent;
        final boolean allowWrapComment = ConfigManager.getInstance().getConfig().lineBreaks.allowWrapComment;
        final int indentSize = ConfigManager.getInstance().getConfig().indents.indentStr.length();
        allContents = StructureUtils.makeCombinedText(allContents);
        allContents = StructureUtils.makeFormattedText(allContents, keepOriginalSpaces);
        if (enableTextWrap) {
            allContents = StructureUtils.wrapText(allContents, keepOriginalLineBreak, wrapSize, indentSize, afterIndent, allowWrapComment);
            if (ConfigManager.getInstance().getConfig().lineBreaks.formatAfterWrap) {
                allContents = StructureUtils.makeFormattedText(allContents, keepOriginalSpaces);
                allContents = StructureUtils.wrapText(allContents, keepOriginalLineBreak, wrapSize, indentSize, afterIndent, allowWrapComment);
                allContents = StructureUtils.makeFormattedText(allContents, keepOriginalSpaces);
            }
        }
        return allContents;
    }
}
