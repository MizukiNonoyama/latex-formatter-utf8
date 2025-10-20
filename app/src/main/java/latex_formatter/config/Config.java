package latex_formatter.config;

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
        public String[] endRegex =
                {"\\\\", ") ", ",", "\" ", "\".", "\",", "' ", "'.", "',", ":", ",", ".", ";", ">", "~", "!", "@", " ",
                        "#", "$", "%", "^", "&", "]", "}", "?", "!", "、", "。", "」", "】", "』", "）", "！", "？", "て", "に",
                        "を", "は", "が", "の", "と", "も", "へ", "で", "や", "，", "．"};
        public String[] startRegex = {"/", " (", " \"", " '", "-", "=", "+", "*", "`", "「", "【", "『", "（"};
    }

    public static class BlockSearcher {
        public TexFullBlockPair[] texFullBlockRegex =
                {new TexFullBlockPair(new TexBlockRegexPair("\\begin", "}"), new TexBlockRegexPair("\\end", "}"))};
        public TexBlockRegexPair[] texHalfBlockRegex = {};
        public TexBlockRegexPair[] texHalfBlockRegexNoEndLineBreaks = {new TexBlockRegexPair("\\item ")};
        public TexBlockRegexPair[] texHalfBlockRegexFixedIndent =
                {new TexBlockRegexPair("\\part", "}"), new TexBlockRegexPair("\\chapter", "}"),
                        new TexBlockRegexPair("\\section", "}"), new TexBlockRegexPair("\\subsection", "}"),
                        new TexBlockRegexPair("\\subsubsection", "}"), new TexBlockRegexPair("\\paragraph", "}"),
                        new TexBlockRegexPair("\\subparagraph", "}")};
        public TexBlockRegexPair[] texHalfBlockRegexFixedIndentNoEndLineBreaks = {};
        public int maxProcess = 100000;
    }

    public static class Indents {
        public boolean isEnableIndentTexBlocks = true;
        public String indentStr = "\t\t";
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
