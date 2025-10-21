package latex_formatter.config;

import latex_formatter.structure.Pair;

public class Config {
    public LineBreaks lineBreaks = new LineBreaks();
    public Indents indents = new Indents();
    public BlockSearcher searcher = new BlockSearcher();

    public static class LineBreaks {
        public boolean isEnableModifyLineBreaks = true;
        public int textWrapColumns = 80;
        public boolean wrapAfterIndent = true;
        public boolean keepOriginalLineBreaks = false;
        public boolean keepOriginalSpaces = false;
        public boolean allowWrapComment = true;
        public boolean formatAfterWrap = true;
        public String[] lineBreakDeniedHeadRegex = new String[] {"\\", "%"};
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
        public int maxProcess = 100000;
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
