package latex_formatter.structure;

import latex_formatter.config.ConfigManager;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class StructureUtils {
    public static int find(@NonNull String rawText, String regex) {
        if (rawText.contains(regex)) {
            String[] splits = rawText.split(Pattern.quote(regex), 2);
            return (rawText.startsWith(regex)) ? 0 : splits[0].length();
        }
        return -1;
    }

    @NonNull
    public static Pair<String, String> split(@NonNull String rawText, String regex) {
        String[] splits = rawText.split(Pattern.quote(regex), 2);
        if (splits.length == 2) {
            return new Pair<>(splits[0], splits[1]);
        } else if (rawText.startsWith(regex)) {
            return new Pair<>("", splits[0]);
        } else if (splits.length == 1) {
            return new Pair<>(splits[0], "");
        }
        return new Pair<>("", "");
    }

    @NonNull
    public static Pair<String, String> split(@NonNull String rawText, int firstSize) {
        char[] first = new char[firstSize];
        char[] second = new char[rawText.length() - firstSize];
        rawText.getChars(firstSize, rawText.length(), second, 0);
        rawText.getChars(0, firstSize, first, 0);
        return new Pair<>(new String(first), new String(second));
    }

    @NonNull
    public static IndentedTexBlock makeIndentedTexBlock(@NonNull TexBlock texBlock, int nextIndent) {
        if (texBlock.isRawText()) {
            return new IndentedTexBlock(texBlock.getRawText(), nextIndent, texBlock.lineBreakAfterStartRegex);
        }
        IndentedTexBlock startRegex = new IndentedTexBlock("", nextIndent, false);
        if (texBlock.startRegex instanceof TexBlock) {
            startRegex = makeIndentedTexBlock((TexBlock) texBlock.startRegex, nextIndent);
        } else if (texBlock.startRegex instanceof String) {
            startRegex = new IndentedTexBlock((String) texBlock.startRegex, nextIndent, true);
        }
        IndentedTexBlock endRegex = new IndentedTexBlock("", nextIndent, false);
        if (texBlock.endRegex instanceof TexBlock) {
            endRegex = makeIndentedTexBlock((TexBlock) texBlock.endRegex, nextIndent);
        } else if (texBlock.endRegex instanceof String) {
            endRegex = new IndentedTexBlock((String) texBlock.endRegex, nextIndent, false);
        }
        List<IndentedTexBlock> objects = new ArrayList<>();
        boolean header = texBlock.lineBreakAfterStartRegex;
        Object lastObject = null;
        for (Object object : texBlock.objects) {
            IndentedTexBlock indentedTexBlock = new IndentedTexBlock("", nextIndent + 1, header);
            if (object instanceof TexBlock) {
                indentedTexBlock = makeIndentedTexBlock((TexBlock) object, nextIndent + 1);
            } else if (object instanceof String) {
                indentedTexBlock = new IndentedTexBlock((String) object, nextIndent + 1, header || lastObject instanceof TexBlock);
            }
            lastObject = object;
            objects.add(indentedTexBlock);
            header = false;
        }
        return new IndentedTexBlock(startRegex, endRegex, objects, nextIndent);
    }

    @NonNull
    public static IndentedTexBlock makeNonIndentedTexBlock(@NonNull TexBlock texBlock) {
        if (texBlock.isRawText()) {
            return new IndentedTexBlock(texBlock.getRawText(), 0, texBlock.lineBreakAfterStartRegex);
        }
        IndentedTexBlock startRegex = new IndentedTexBlock("", 0, false);
        if (texBlock.startRegex instanceof TexBlock) {
            startRegex = makeNonIndentedTexBlock((TexBlock) texBlock.startRegex);
        } else if (texBlock.startRegex instanceof String) {
            startRegex = new IndentedTexBlock((String) texBlock.startRegex, 0, true);
        }
        IndentedTexBlock endRegex = new IndentedTexBlock("", 0, false);
        if (texBlock.endRegex instanceof TexBlock) {
            endRegex = makeNonIndentedTexBlock((TexBlock) texBlock.endRegex);
        } else if (texBlock.endRegex instanceof String) {
            endRegex = new IndentedTexBlock((String) texBlock.endRegex, 0, true);
        }
        List<IndentedTexBlock> objects = new ArrayList<>();
        boolean header = texBlock.lineBreakAfterStartRegex;
        for (Object object : texBlock.objects) {
            IndentedTexBlock indentedTexBlock = new IndentedTexBlock("", 0, header);
            if (object instanceof TexBlock) {
                indentedTexBlock = makeNonIndentedTexBlock((TexBlock) object);
            } else if (object instanceof String) {
                indentedTexBlock = new IndentedTexBlock((String) object, 0, true);
            }
            objects.add(indentedTexBlock);
            header = false;
        }
        return new IndentedTexBlock(startRegex, endRegex, objects, 0);
    }

    @NonNull
    public static List<IndentedTexBlock> makeCombinedText(@NonNull List<IndentedTexBlock> texBlocks) {
        List<IndentedTexBlock> tempResult = new ArrayList<>();
        for (IndentedTexBlock indentedTexBlock : texBlocks) {
            if (indentedTexBlock.isRawText()) {
                if (!tempResult.isEmpty() && tempResult.getLast().isRawText()) {
                    // TODO \nが必要かも？
                    tempResult.getLast().text += indentedTexBlock.getRawText();
                } else {
                    tempResult.add(indentedTexBlock);
                }
            } else {
                indentedTexBlock.startRegex =
                        makeCombinedText(Collections.singletonList(indentedTexBlock.startRegex)).getFirst();
                indentedTexBlock.objects = makeCombinedText(indentedTexBlock.objects);
                indentedTexBlock.endRegex =
                        makeCombinedText(Collections.singletonList(indentedTexBlock.endRegex)).getFirst();
                tempResult.add(indentedTexBlock);
            }
        }
        return tempResult;
    }

    @NonNull
    public static List<IndentedTexBlock> makeFormattedText(@NonNull List<IndentedTexBlock> texBlocks,
            boolean keepOriginalSpace) {
        List<IndentedTexBlock> tempResult = new ArrayList<>();
        for (IndentedTexBlock indentedTexBlock : texBlocks) {
            if (indentedTexBlock.isRawText()) {
                if (!keepOriginalSpace) {
                    String text = "";
                    String[] splitLineBreaks = indentedTexBlock.getRawText().split("\n");
                    for (int j = 0; j < splitLineBreaks.length; j++) {
                        String[] split = splitLineBreaks[j].split(" +");
                        String temp = "";
                        for (int i = 0; i < split.length; i++) {
                            if (split[i].isEmpty()) {
                                continue;
                            }
                            temp += split[i] + (i + 1 < split.length ? " " : "");
                        }
                        split = temp.split("\t+");
                        String temp1 = "";
                        for (int i = 0; i < split.length; i++) {
                            if (split[i].isEmpty()) {
                                continue;
                            }
                            temp1 += split[i] + (i + 1 < split.length ? "\t" : "");
                        }
                        text += temp1 + (j + 1 < splitLineBreaks.length ? "\n" : "");
                    }
                    indentedTexBlock.text = text;
                }
                tempResult.add(indentedTexBlock);
            } else {
                indentedTexBlock.startRegex = makeFormattedText(Collections.singletonList(indentedTexBlock.startRegex),
                        keepOriginalSpace).getFirst();
                indentedTexBlock.objects = makeFormattedText(indentedTexBlock.objects, keepOriginalSpace);
                indentedTexBlock.endRegex = makeFormattedText(Collections.singletonList(indentedTexBlock.endRegex),
                        keepOriginalSpace).getFirst();
                tempResult.add(indentedTexBlock);
            }
        }
        return tempResult;
    }

    @NonNull
    public static List<IndentedTexBlock> wrapText(@NonNull List<IndentedTexBlock> texBlocks,
            boolean keepOriginalLineBreak, int wrapSize, int indentBaseLength, boolean wrapAfterIndent, boolean allowWrapComment) {
        List<IndentedTexBlock> tempResult = new ArrayList<>();
        for (IndentedTexBlock indentedTexBlock : texBlocks) {
            if (indentedTexBlock.isRawText()) {
                List<String> temp = textWrap(indentedTexBlock.getRawText(), keepOriginalLineBreak,
                        wrapSize - (wrapAfterIndent ? indentBaseLength * indentedTexBlock.objectsIndent : 0), allowWrapComment);
                String text = "";
                for (int i = 0; i < temp.size(); i++) {
                    text += temp.get(i) + (i + 1 < temp.size() ? "\n" : "");
                }
                indentedTexBlock.text = text;
                tempResult.add(indentedTexBlock);
            } else {
                indentedTexBlock.startRegex =
                        wrapText(Collections.singletonList(indentedTexBlock.startRegex), keepOriginalLineBreak,
                                wrapSize, indentBaseLength, wrapAfterIndent, allowWrapComment).getFirst();
                indentedTexBlock.objects =
                        wrapText(indentedTexBlock.objects, keepOriginalLineBreak, wrapSize, indentBaseLength,
                                wrapAfterIndent, allowWrapComment);
                indentedTexBlock.endRegex =
                        wrapText(Collections.singletonList(indentedTexBlock.endRegex), keepOriginalLineBreak, wrapSize,
                                indentBaseLength, wrapAfterIndent, allowWrapComment).getFirst();
                tempResult.add(indentedTexBlock);
            }
        }
        return tempResult;
    }

    @NonNull
    public static List<String> textWrap(@NonNull String rawText, boolean keepOriginalLineBreak, int wrapSize, boolean allowWrapComment) {
        List<String> result = new ArrayList<>();
        String[] splitWithOriginalLineBreaks = rawText.split("\n+");
        String temp = "";
        label:
        for (int i = 0; i < splitWithOriginalLineBreaks.length; i++) {
            if (keepOriginalLineBreak) {
                result.addAll(textWrap(splitWithOriginalLineBreaks[i], wrapSize, 0));
            } else {
                if (splitWithOriginalLineBreaks[i].startsWith("%") && allowWrapComment) {
                    result.addAll(textWrap(temp, wrapSize, 0));
                    temp = "";
                    List<String> split = textWrap(splitWithOriginalLineBreaks[i], wrapSize - 2, 2);
                    boolean header = true;
                    for (String s : split) {
                        if (!header) {
                            s = "%" + s;
                        }
                        header = false;
                        result.add(s);
                    }
                } else {
                    for (String regex : ConfigManager.getInstance().getConfig().lineBreaks.lineBreakDeniedHeadRegex) {
                        if (splitWithOriginalLineBreaks[i].startsWith(regex)) {
                            // Avoid wrap
                            result.addAll(textWrap(temp, wrapSize, 0));
                            temp = "";
                            result.add(splitWithOriginalLineBreaks[i]);
                            continue label;
                        }
                    }
                    temp += splitWithOriginalLineBreaks[i] + (i + 1 < splitWithOriginalLineBreaks.length ? " " : "");
                    if (i + 1 == splitWithOriginalLineBreaks.length && !temp.isEmpty()) {
                        result.addAll(textWrap(temp, wrapSize, 0));
                    }
                }
            }
        }
        return result;
    }

    @NonNull
    public static List<String> textWrap(@NonNull String rawText, int wrapSize, int margin) {
        List<String> result = new ArrayList<>();
        String[] endRegexList = ConfigManager.getInstance().getConfig().lineBreaks.footRegex;
        String[] beginRegexList = ConfigManager.getInstance().getConfig().lineBreaks.headRegex;
        String tempText = rawText;
        label:
        while (tempText.length() > wrapSize + margin) {
            int baseSize = wrapSize;
            while (baseSize > 1) {
                Pair<String, String> pairB = StructureUtils.split(tempText, baseSize);
                Pair<String, String> pairE = StructureUtils.split(tempText, baseSize - 1);
                for (String regex : endRegexList) {
                    if (pairE.getSecond().startsWith(regex)) {
                        Pair<String, String> split = StructureUtils.split(pairE.getSecond(), regex);
                        tempText = split.getSecond();
                        result.add(pairE.getFirst() + regex);
                        continue label;
                    }
                }
                for (String regex : beginRegexList) {
                    if (pairB.getSecond().startsWith(regex)) {
                        tempText = pairB.getSecond();
                        result.add(pairB.getFirst());
                        continue label;
                    }
                }
                baseSize--;
            }
            break;
        }
        if (!tempText.isEmpty()) {
            result.add(tempText);
        }
        return result;
    }

    @NonNull
    public static List<String> buildText(@NonNull List<IndentedTexBlock> texBlocks, String indent) {
        List<String> tempResult = new ArrayList<>();
        for (IndentedTexBlock indentedTexBlock : texBlocks) {
            String indents = "";
            for (int i = 0; i < indentedTexBlock.objectsIndent; i++) {
                indents += indent;
            }
            if (indentedTexBlock.isRawText()) {
                String result = "";
                String[] split = indentedTexBlock.getRawText().split(Pattern.quote("\n"), 0);
                boolean flag = true;
                for (int i = 0; i < split.length; i++) {
                    if (flag) {
                        result += (indentedTexBlock.needLineBreakAtHead && !split[i].isEmpty() && !split[i].startsWith("\n") ? ("\n" + indents) : "");
                        result += split[i] + (i + 1 < split.length ? "\n" : "");
                    } else {
                        result += indents + split[i] + (i + 1 < split.length ? "\n" : "");
                    }
                    flag = false;
                }
                if (!result.isEmpty() && !result.matches("\n")) {
                    tempResult.add(result);
                }
            } else {
                tempResult.addAll(buildText(Collections.singletonList(indentedTexBlock.startRegex), indent));
                tempResult.addAll(buildText(indentedTexBlock.objects, indent));
                tempResult.addAll(buildText(Collections.singletonList(indentedTexBlock.endRegex), indent));
            }
        }
        return tempResult;
    }
}
