package latex_formatter.structure;

import latex_formatter.config.Config;
import latex_formatter.config.ConfigManager;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StructureUtils {
    public static int find(String rawText, String regex) {
        if (rawText.contains(regex)) {
            String[] splits = rawText.split(Pattern.quote(regex), 2);
            return (rawText.startsWith(regex)) ? 0 : splits[0].length();
        }
        return -1;
    }

    public static Pair<String, String> split(String rawText, String regex) {
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

    public static Pair<String, String> split(String rawText, int firstSize) {
        char[] first = new char[firstSize];
        char[] second = new char[rawText.length() - firstSize];
        rawText.getChars(firstSize, rawText.length(), second, 0);
        rawText.getChars(0, firstSize, first, 0);
        return new Pair<>(new String(first), new String(second));
    }

    public static IndentedTexBlock makeIndentedTexBlock(TexBlock texBlock, int nextIndent) {
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

    public static IndentedTexBlock makeNonIndentedTexBlock(TexBlock texBlock) {
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

    public static List<IndentedTexBlock> makeCombinedText(List<IndentedTexBlock> texBlocks) {
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

    public static List<IndentedTexBlock> makeFormattedText(List<IndentedTexBlock> texBlocks,
            boolean keepOriginalSpace) {
        List<IndentedTexBlock> tempResult = new ArrayList<>();
        for (IndentedTexBlock indentedTexBlock : texBlocks) {
            if (indentedTexBlock.isRawText()) {
                if (indentedTexBlock.formatDenied) {
                    // verbatim 系ブロックの中身は一切触らない
                    tempResult.add(indentedTexBlock);
                    continue;
                }
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
                            temp += split[i] + (i + 1 < split.length && isNeedSpace(split[i], split[i + 1]) ? " " : "");
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
                } else {
                    // 行頭の空白・タブは buildText が objectsIndent に応じて改めて付け直し、行末の空白・タブは
                    // wrapText が折り返し位置に応じて改めて付け直す(あるいは付けない)ため、ここで取り除いて
                    // おかないと再フォーマットのたびにインデントや行末の空白が二重・多重に積み上がってしまう。
                    // 行の内部の空白(単語間の連続スペースなど)は keepOriginalSpace の趣旨通りそのまま残す。
                    String text = "";
                    String[] splitLineBreaks = indentedTexBlock.getRawText().split("\n");
                    for (int j = 0; j < splitLineBreaks.length; j++) {
                        text += addSpaceAfterCommentMarker(splitLineBreaks[j].replaceAll("^[ \\t]+|[ \\t]+$", "")) +
                                (j + 1 < splitLineBreaks.length ? "\n" : "");
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

    public static List<IndentedTexBlock> wrapText(List<IndentedTexBlock> texBlocks,
            boolean keepOriginalLineBreak, boolean forceBreakLine, int wrapSize, int indentBaseLength, boolean wrapAfterIndent, boolean allowWrapComment) {
        List<IndentedTexBlock> tempResult = new ArrayList<>();
        for (IndentedTexBlock indentedTexBlock : texBlocks) {
            if (indentedTexBlock.isRawText()) {
                if (indentedTexBlock.formatDenied || indentedTexBlock.wrapDenied) {
                    tempResult.add(indentedTexBlock);
                    continue;
                }
                List<String> temp = textWrap(indentedTexBlock.getRawText(), keepOriginalLineBreak,
                        forceBreakLine || indentedTexBlock.forceBreak,
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
                                forceBreakLine, wrapSize, indentBaseLength, wrapAfterIndent, allowWrapComment).getFirst();
                indentedTexBlock.objects =
                        wrapText(indentedTexBlock.objects, keepOriginalLineBreak, forceBreakLine, wrapSize, indentBaseLength,
                                wrapAfterIndent, allowWrapComment);
                indentedTexBlock.endRegex =
                        wrapText(Collections.singletonList(indentedTexBlock.endRegex), keepOriginalLineBreak, forceBreakLine, wrapSize,
                                indentBaseLength, wrapAfterIndent, allowWrapComment).getFirst();
                tempResult.add(indentedTexBlock);
            }
        }
        return tempResult;
    }

    /** LaTeX の強制改行 "\\"(直後の "*" や "[2pt]" といった省略可能引数を含む)。 */
    private static final Pattern FORCE_BREAK = Pattern.compile("\\\\\\\\(?:\\*|\\[[^\\]]*\\])?");

    /** 強制改行で区切った1断片。endsWithBreak なら末尾が強制改行(その直後で必ず改行する)。 */
    private record Segment(String text, boolean endsWithBreak) {
    }

    /**
     * text を強制改行 "\\" の直後で区切る。2つ目以降の断片は先頭の空白を取り除き、空白のみの末尾断片は捨てる。
     * エスケープ("\%" など)を誤認しないよう、正規表現で左から順に消費して "\\" を見つける。
     */
    private static List<Segment> splitAfterForcedBreaks(String text) {
        List<Segment> parts = new ArrayList<>();
        Matcher matcher = FORCE_BREAK.matcher(text);
        int start = 0;
        while (matcher.find()) {
            parts.add(new Segment(parts.isEmpty() ? text.substring(start, matcher.end()) :
                    stripLeadingSpaces(text.substring(start, matcher.end())), true));
            start = matcher.end();
        }
        if (start < text.length()) {
            String rest = parts.isEmpty() ? text.substring(start) : stripLeadingSpaces(text.substring(start));
            if (!rest.isEmpty()) {
                parts.add(new Segment(rest, false));
            }
        }
        return parts;
    }

    private static String stripLeadingSpaces(String text) {
        return text.replaceFirst("^[ \t]+", "");
    }

    /** 1行分のコード(コメントを含まない)を折り返す。force なら "\\" の直後で必ず改行する。 */
    private static List<String> wrapCodeLine(String code, int wrapSize, boolean force) {
        if (!force) {
            return textWrap(code, wrapSize, 0);
        }
        List<String> result = new ArrayList<>();
        for (Segment segment : splitAfterForcedBreaks(code)) {
            result.addAll(textWrap(segment.text(), wrapSize, 0));
        }
        return result;
    }

    /**
     * 先頭の "%"(の連続)の直後に空白が無ければ空白を1つ入れる。"%" は findRealCommentStart で見つけた
     * 本物のコメント開始のみが対象なので "\%" は触らない。"%!TEX ..." のような単独 "%" + "!" の
     * マジックコメントは意味が変わりうるため触らない。
     */
    private static String addSpaceAfterCommentMarker(String line) {
        int start = findRealCommentStart(line);
        if (start < 0) {
            return line;
        }
        int end = start;
        while (end < line.length() && line.charAt(end) == '%') {
            end++;
        }
        if (end >= line.length()) {
            return line;
        }
        char next = line.charAt(end);
        if (next == ' ' || next == '\t' || (end - start == 1 && next == '!')) {
            return line;
        }
        return line.substring(0, end) + " " + line.substring(end);
    }

    /**
     * wrapDeniedContains のうち "{" で終わるもの(例: url コマンド)について、text 中のその引数部分の範囲を返す。
     * URL 中の "%20" のような "%" をコメント開始と誤認しないために使う。閉じ括弧が無ければ行末まで。
     */
    private static List<int[]> findProtectedSpans(String text) {
        List<int[]> spans = new ArrayList<>();
        for (String key : ConfigManager.getInstance().getConfig().lineBreaks.wrapDeniedContains) {
            if (key.isEmpty() || !key.endsWith("{")) {
                continue;
            }
            int from = 0;
            int idx;
            while ((idx = text.indexOf(key, from)) >= 0) {
                int depth = 1;
                int end = idx + key.length();
                while (end < text.length() && depth > 0) {
                    char c = text.charAt(end);
                    if (c == '{') {
                        depth++;
                    } else if (c == '}') {
                        depth--;
                    }
                    end++;
                }
                spans.add(new int[] {idx, end});
                from = end;
            }
        }
        return spans;
    }

    /**
     * text 中で、実際にコメントを開始する(エスケープされていない)最初の "%" の位置を返す。
     * "%" の直前に連続する "\" の個数が偶数(0 を含む)であれば、それらの "\" は "\\"(強制改行)として
     * 消費し尽くされておりエスケープに関与しないため、その "%" は本物のコメント開始とみなす。
     * 奇数であれば直前の "\" とペアで "\%"(リテラルな %)を構成しているため、コメントとはみなさない。
     */
    private static int findRealCommentStart(String text) {
        List<int[]> protectedSpans = findProtectedSpans(text);
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) != '%') {
                continue;
            }
            boolean inProtectedSpan = false;
            for (int[] span : protectedSpans) {
                if (i >= span[0] && i < span[1]) {
                    inProtectedSpan = true;
                    break;
                }
            }
            if (inProtectedSpan) {
                continue;
            }
            int backslashRun = 0;
            for (int j = i - 1; j >= 0 && text.charAt(j) == '\\'; j--) {
                backslashRun++;
            }
            if (backslashRun % 2 == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 行の途中に本物のコメント開始 "%" が現れる場合、その行をコメント開始位置で
     * [コメント前の本文, "%"から始まるコメント本体] の2つの断片に分割する。
     * 行頭からコメントが始まる行はそのまま(既存の startsWith("%") 分岐に処理を委ねる)。
     */
    private static List<String> expandForInlineComments(String[] lines, boolean allowWrapComment) {
        List<String> expanded = new ArrayList<>();
        for (String line : lines) {
            int commentStart = allowWrapComment ? findRealCommentStart(line) : -1;
            if (commentStart > 0) {
                String codePart = line.substring(0, commentStart);
                if (codePart.endsWith(" ")) {
                    codePart = codePart.substring(0, codePart.length() - 1);
                }
                expanded.add(codePart);
                expanded.add(line.substring(commentStart));
            } else {
                expanded.add(line);
            }
        }
        return expanded;
    }

    /**
     * "%" から始まるコメント本文(margin 込みの wrapSize で折り返す)を、継続行にも "%" を
     * 付けながら折り返す。header(1つ目)の行は commentPart 自身に含まれる "%" をそのまま使う。
     */
    private static List<String> wrapCommentPart(String commentPart, int wrapSize) {
        List<String> result = new ArrayList<>();
        // 継続行には "% " を付けるので、その分(2桁)を引いた幅で折り返す
        List<String> split = textWrap(commentPart, wrapSize - 2, 0);
        boolean header = true;
        for (String s : split) {
            if (header) {
                result.add(addSpaceAfterCommentMarker(s));
            } else {
                result.add(s.startsWith(" ") ? "%" + s : "% " + s);
            }
            header = false;
        }
        return result;
    }

    /**
     * 1行分のテキストを、本物のコメント開始 "%" を認識した上で折り返す。
     * コメントが無ければ通常通り幅で折り返すだけ。コメントがあれば、コメント前の本文は通常通り折り返し、
     * コメント本体(とその継続行)は必ず "%" から始まるようにする — そうしないと折り返し後の継続行が
     * LaTeX 上コメントとして認識されなくなるため。
     */
    private static List<String> wrapLineCommentAware(String line, int wrapSize, boolean allowWrapComment,
            boolean force) {
        int commentStart = allowWrapComment ? findRealCommentStart(line) : -1;
        if (commentStart < 0) {
            return wrapCodeLine(line, wrapSize, force);
        }
        List<String> result = new ArrayList<>();
        if (commentStart > 0) {
            result.addAll(wrapCodeLine(line.substring(0, commentStart), wrapSize, force));
        }
        result.addAll(wrapCommentPart(line.substring(commentStart), wrapSize));
        return result;
    }

    public static List<String> textWrap(String rawText, boolean keepOriginalLineBreak, boolean forceBreakLine, int wrapSize, boolean allowWrapComment) {
        List<String> result = new ArrayList<>();
        String[] rawLines = rawText.split("\n+");
        String[] splitWithOriginalLineBreaks = keepOriginalLineBreak ? rawLines :
                expandForInlineComments(rawLines, allowWrapComment).toArray(new String[0]);
        String temp = "";
        label:
        for (int i = 0; i < splitWithOriginalLineBreaks.length; i++) {
            if (keepOriginalLineBreak) {
                result.addAll(wrapLineCommentAware(splitWithOriginalLineBreaks[i], wrapSize, allowWrapComment, forceBreakLine));
            } else {
                if (splitWithOriginalLineBreaks[i].startsWith("%") && allowWrapComment) {
                    result.addAll(textWrap(temp, wrapSize, 0));
                    temp = "";
                    result.addAll(wrapCommentPart(splitWithOriginalLineBreaks[i], wrapSize));
                } else {
                    List<String> wrapDeniedHeadRegex = Arrays.asList(ConfigManager.getInstance().getConfig().lineBreaks.wrapDeniedHeadRegex);
                    String[] wrapDeniedContains = ConfigManager.getInstance().getConfig().lineBreaks.wrapDeniedContains;
                    for (String regex : ConfigManager.getInstance().getConfig().lineBreaks.lineBreakDeniedHeadRegex) {
                        if (splitWithOriginalLineBreaks[i].startsWith(regex)) {
                            // Avoid wrap
                            result.addAll(textWrap(temp, wrapSize, 0));
                            temp = "";
                            boolean flag = false;
                            for (String key : wrapDeniedContains) {
                                if (splitWithOriginalLineBreaks[i].contains(key)) {
                                    flag = true;
                                }
                            }
                            if (wrapDeniedHeadRegex.contains(regex) || flag) {
                                result.add(splitWithOriginalLineBreaks[i]);
                            } else {
                                result.addAll(wrapCodeLine(splitWithOriginalLineBreaks[i], wrapSize, forceBreakLine));
                            }
                            continue label;
                        } else {
                            for (String key : wrapDeniedContains) {
                                if (splitWithOriginalLineBreaks[i].contains(key)) {
                                    result.addAll(textWrap(temp, wrapSize, 0));
                                    temp = "";
                                    result.add(splitWithOriginalLineBreaks[i]);
                                    continue label;
                                }
                            }
                        }
                    }
                    if (forceBreakLine) {
                        for (Segment segment : splitAfterForcedBreaks(splitWithOriginalLineBreaks[i])) {
                            temp += segment.text() + (segment.endsWithBreak() ? "" :
                                    (i + 1 < splitWithOriginalLineBreaks.length ? " " : ""));
                            if (segment.endsWithBreak()) {
                                result.addAll(textWrap(temp, wrapSize, 0));
                                temp = "";
                            }
                        }
                    } else {
                        temp += splitWithOriginalLineBreaks[i] + (i + 1 < splitWithOriginalLineBreaks.length ? " " : "");
                    }
                    if (i + 1 == splitWithOriginalLineBreaks.length && !temp.isEmpty()) {
                        result.addAll(textWrap(temp, wrapSize, 0));
                    }
                }
            }
        }
        return result;
    }

    public static List<String> textWrap(String rawText, int wrapSize, int margin) {
        List<String> result = new ArrayList<>();
        String[] endRegexList = ConfigManager.getInstance().getConfig().lineBreaks.footRegex;
        String[] beginRegexList = ConfigManager.getInstance().getConfig().lineBreaks.headRegex;
        final boolean atTrueLength = ConfigManager.getInstance().getConfig().lineBreaks.wrapAtTrueLength;
        String tempText = rawText;
        label:
        while (StructureUtils.strLength(tempText, atTrueLength) > wrapSize + margin) {
            int baseSize = wrapSize;
            while (baseSize > 1) {
                Pair<String, String> pairB = StructureUtils.splitAt(tempText, baseSize, atTrueLength);
                Pair<String, String> pairE = StructureUtils.splitAt(tempText, baseSize - 1, atTrueLength);
                if (pairE.getFirst().equals(pairB.getFirst())) {
                    pairE = StructureUtils.splitAt(tempText, baseSize - 2, atTrueLength);
                }
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

    /** ブロックの開始部分(例: "\begin{lstlisting}")を、空白を除いて連結して返す。 */
    private static String openingText(IndentedTexBlock block) {
        if (block.isRawText()) {
            return block.getRawText().replaceAll("\\s+", "");
        }
        StringBuilder sb = new StringBuilder(openingText(block.startRegex));
        for (IndentedTexBlock object : block.objects) {
            sb.append(openingText(object));
            if (sb.length() > 200) {
                break;
            }
        }
        sb.append(openingText(block.endRegex));
        return sb.toString();
    }

    private static boolean matchesAnyPrefix(String opening, String[] prefixes) {
        for (String prefix : prefixes) {
            String normalized = prefix.replaceAll("\\s+", "");
            if (!normalized.isEmpty() && opening.startsWith(normalized)) {
                return true;
            }
        }
        return false;
    }

    /**
     * config の formatDeniedBlockPrefixes / wrapDeniedBlockPrefixes / forceBreakBlockPrefixes に一致する
     * ブロックの中身(生テキスト)に、整形禁止・折り返し禁止・"\\" 強制改行のフラグを付ける。
     * フラグはネストした子ブロックにも引き継がれる。ブロック自身の開始・終了部分(egin{..} など)には付けない。
     */
    public static void markBlockPolicies(List<IndentedTexBlock> blocks, boolean inFormatDenied, boolean inWrapDenied,
            boolean inForceBreak) {
        Config.LineBreaks config = ConfigManager.getInstance().getConfig().lineBreaks;
        for (IndentedTexBlock block : blocks) {
            if (block.isRawText()) {
                block.formatDenied = inFormatDenied;
                block.wrapDenied = inWrapDenied;
                block.forceBreak = inForceBreak;
                continue;
            }
            String opening = openingText(block.startRegex);
            boolean formatDenied = inFormatDenied || matchesAnyPrefix(opening, config.formatDeniedBlockPrefixes);
            boolean wrapDenied = inWrapDenied || matchesAnyPrefix(opening, config.wrapDeniedBlockPrefixes);
            boolean forceBreak = inForceBreak || matchesAnyPrefix(opening, config.forceBreakBlockPrefixes);
            markBlockPolicies(Collections.singletonList(block.startRegex), inFormatDenied, inWrapDenied, inForceBreak);
            markBlockPolicies(block.objects, formatDenied, wrapDenied, forceBreak);
            markBlockPolicies(Collections.singletonList(block.endRegex), inFormatDenied, inWrapDenied, inForceBreak);
        }
    }

    public static List<String> buildText(List<IndentedTexBlock> texBlocks, String indent) {
        List<String> tempResult = new ArrayList<>();
        for (IndentedTexBlock indentedTexBlock : texBlocks) {
            String indents = "";
            // 整形禁止(verbatim 系)の生テキストにはインデントを付け足さない(中身が変わってしまうため)
            for (int i = 0; i < (indentedTexBlock.formatDenied ? 0 : indentedTexBlock.objectsIndent); i++) {
                indents += indent;
            }
            if (indentedTexBlock.isRawText()) {
                String result = "";
                // 整形禁止ブロックの末尾に残る空白は直後の \end 行のインデントなので、毎回付け直されるものとして落とす
                String rawText = indentedTexBlock.formatDenied ?
                        indentedTexBlock.getRawText().replaceAll("[ \\t]+$", "") : indentedTexBlock.getRawText();
                String[] split = rawText.split(Pattern.quote("\n"), 0);
                boolean flag = true;
                for (int i = 0; i < split.length; i++) {
                    if (flag) {
                        result += (indentedTexBlock.needLineBreakAtHead && !indentedTexBlock.formatDenied && !split[i].isEmpty() && !split[i].startsWith("\n") ? ("\n" + indents) : "");
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

    public static int getTrueLengthOfChar(char c) {
        return ConfigManager.getInstance().getConfig().lineBreaks.getTrueLength(String.valueOf(c).getBytes(StandardCharsets.UTF_8).length);
    }

    public static boolean isNeedSpace(String first, String second) {
        return getTrueLengthOfChar(first.charAt(first.length() - 1)) == 1 || getTrueLengthOfChar(second.charAt(0)) == 1;
    }

    public static int getTrueLength(String string) {
        int all = 0;
        for (int i = 0; i < string.length(); i++) {
            all += ConfigManager.getInstance().getConfig().lineBreaks.getTrueLength(String.valueOf(string.charAt(i)).getBytes(StandardCharsets.UTF_8).length);
        }
        return all;
    }

    public static Pair<String, String> splitTrueLength(String rawText, int firstSize) {
        int size = Math.min(rawText.length(), firstSize);
        while (size > 1) {
            Pair<String, String> pair = StructureUtils.split(rawText, size);
            if (StructureUtils.getTrueLength(pair.getFirst()) <= firstSize) {
                return pair;
            }
            size--;
        }
        return StructureUtils.split(rawText, size);
    }

    public static int strLength(String value, boolean trueLength) {
        return trueLength ? getTrueLength(value) : value.length();
    }

    public static Pair<String, String> splitAt(String value, int firstSize, boolean trueLength) {
        return trueLength ? splitTrueLength(value, firstSize) : split(value, firstSize);
    }
}
