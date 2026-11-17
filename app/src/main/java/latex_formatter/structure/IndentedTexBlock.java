package latex_formatter.structure;

import java.util.List;

public class IndentedTexBlock {
    public IndentedTexBlock startRegex;
    public IndentedTexBlock endRegex;
    public List<IndentedTexBlock> objects;
    public final int objectsIndent;
    public String text;
    public final boolean needLineBreakAtHead;
    /** true なら、この生テキストは整形・折り返し・インデント付けをせず元のまま出力する(config の formatDeniedBlockPrefixes)。 */
    public boolean formatDenied = false;
    /** true なら、この生テキストは折り返さない(config の wrapDeniedBlockPrefixes)。 */
    public boolean wrapDenied = false;
    /** true なら、この生テキストは折り返しても "\\" の直後で必ず改行する(config の forceBreakBlockPrefixes)。 */
    public boolean forceBreak = false;
    public IndentedTexBlock(IndentedTexBlock header, IndentedTexBlock footer, List<IndentedTexBlock> blocks, int objectsIndent) {
        this.startRegex = header;
        this.endRegex = footer;
        this.objects = blocks;
        this.objectsIndent = objectsIndent;
        this.needLineBreakAtHead = false;
    }

    public IndentedTexBlock(String text, int objectsIndent, boolean needLineBreakAtHead) {
        this.startRegex = null;
        this.endRegex = null;
        this.objects = null;
        this.text = text;
        this.objectsIndent = objectsIndent;
        this.needLineBreakAtHead = needLineBreakAtHead;
    }

    public boolean isRawText() {
        return this.startRegex == null && this.endRegex == null;
    }

    public String getRawText() {
        if (this.isRawText()) {
            return text;
        }
        return "";
    }

    public void setRawText(String text) {
        if (this.isRawText()) {
            this.text = text;
        }
    }
}
