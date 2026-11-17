package latex_formatter.config;

import latex_formatter.structure.Pair;

public class Config {
    public LineBreaks lineBreaks = new LineBreaks();
    public Indents indents = new Indents();
    public BlockSearcher searcher = new BlockSearcher();
    public double timeoutSeconds = 30.0;

    public static class LineBreaks {
        public boolean isEnableModifyLineBreaks = true;
        public int textWrapColumns = 120;
        public boolean wrapAfterIndent = true;
        public boolean keepOriginalLineBreaks = false;
        public boolean forceBreakLine = false;
        public boolean keepOriginalSpaces = false;
        public boolean formatAfterWrap = true;
        public boolean allowWrapComment = true;
        public String[] lineBreakDeniedHeadRegex = new String[] {"\\", "%"};
        public String[] wrapDeniedHeadRegex = new String[] {"%"};
        public String[] wrapDeniedContains = new String[] {"\\url{"};
        /**
         * ブロック開始部分(例: "\begin{lstlisting}" や "\item")がこれらの接頭辞のどれかで始まるブロックの中身は、
         * 空白の整形・折り返し・インデントの付け直しを一切行わず、元のまま出力する(空白は無視して前方一致)。
         */
        public String[] formatDeniedBlockPrefixes = new String[]
                {"\\begin{lstlisting", "\\begin{verbatim", "\\begin{Verbatim", "\\begin{minted", "\\begin{alltt"};
        /** 開始部分がこれらの接頭辞で始まるブロックの中身は折り返さない(空白の整形は行う)。 */
        public String[] wrapDeniedBlockPrefixes = new String[] {};
        /** 開始部分がこれらの接頭辞で始まるブロック(数式・表)の中身は、折り返しは許すが "\\" の直後で必ず改行する。 */
        public String[] forceBreakBlockPrefixes = new String[]
                {"\\begin{tabular", "\\begin{array", "\\begin{longtable", "\\begin{align", "\\begin{flalign",
                        "\\begin{gather", "\\begin{multline", "\\begin{eqnarray", "\\begin{split", "\\begin{cases",
                        "\\begin{matrix", "\\begin{pmatrix", "\\begin{bmatrix", "\\begin{Bmatrix", "\\begin{vmatrix",
                        "\\begin{Vmatrix", "\\begin{smallmatrix"};
        public String[] footRegex = new String[]
                {"\\\\", ") ", ",", "\" ", "\".", "\",", "' ", "'.", "',", ":", ",", ".", ";", ">", "~", "!", "@", " ",
                        "#", "$", "%", "^", "&", "]", "}", "?", "!", "、", "。", "，", "．", "」", "】", "』", "）", "！", "？", "て", "に",
                        "を", "は", "が", "の", "と", "も", "へ", "で", "や"};
        public String[] headRegex = new String[] {"/", " (", " \"", " '", "-", "=", "+", "*", "`", "「", "【", "『", "（"};
        public boolean wrapAtTrueLength = true;
        @SuppressWarnings("unchecked")
        public Pair<Integer, Integer>[] byteToTrueLength = new Pair[] {new Pair<>(1, 1), new Pair<>(2, 1), new Pair<>(3, 2), new Pair<>(4, 2)};

        public int getTrueLength(int byteLength) {
            for (Pair<Integer, Integer> pair : this.byteToTrueLength) {
                if (pair.getFirst() == byteLength) {
                    return pair.getSecond();
                }
            }
            return 1;
        }
    }

    public static class BlockSearcher {
        public TexFullBlockPair[] texFullBlockRegex =
                {new TexFullBlockPair(new TexBlockRegexPair("\\begin", "}"), new TexBlockRegexPair("\\end", "}"))};
        public TexBlockRegexPair[] texHalfBlockRegex = {new TexBlockRegexPair("\\item")};
        //public TexBlockRegexPair[] texHalfBlockRegexNoEndLineBreaks = {};
        public TexBlockRegexPair[] texHalfBlockRegexFixedIndent =
                {new TexBlockRegexPair("\\part", "}"), new TexBlockRegexPair("\\chapter", "}"),
                        new TexBlockRegexPair("\\section", "}"), new TexBlockRegexPair("\\subsection", "}"),
                        new TexBlockRegexPair("\\subsubsection", "}"), new TexBlockRegexPair("\\paragraph", "}"),
                        new TexBlockRegexPair("\\subparagraph", "}")};
        //public TexBlockRegexPair[] texHalfBlockRegexFixedIndentNoEndLineBreaks = {};
    }

    public static class Indents {
        public boolean isEnableIndentTexBlocks = true;
        public String indentStr = "\t";
    }

    public static class TexFullBlockPair {
        public final TexBlockRegexPair primary;
        public final TexBlockRegexPair end;

        public TexFullBlockPair(TexBlockRegexPair primary, TexBlockRegexPair end) {
            this.primary = primary;
            this.end = end;
        }

        public String toString() {
            return "[" + this.primary + ", " + this.end + "]";
        }
    }

    public static class TexBlockRegexPair {
        public final String primaryRegex;
        public final String endRegex;
        public static final TexBlockRegexPair EMPTY = new TexBlockRegexPair("", "");

        public TexBlockRegexPair(String primaryRegex, String endRegex) {
            this.primaryRegex = primaryRegex;
            this.endRegex = endRegex;
        }

        public TexBlockRegexPair(String primaryRegex) {
            this.primaryRegex = primaryRegex;
            this.endRegex = "";
        }

        public boolean isEmpty() {
            return this.primaryRegex.isEmpty() && this.endRegex.isEmpty();
        }

        public String toString() {
            return "[" + this.primaryRegex + ", " + this.endRegex + "]";
        }
    }
}
