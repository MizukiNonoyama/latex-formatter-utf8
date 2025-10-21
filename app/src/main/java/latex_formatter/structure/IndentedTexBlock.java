package latex_formatter.structure;

import org.jspecify.annotations.NonNull;

import java.util.List;

public class IndentedTexBlock {
    public IndentedTexBlock startRegex;
    public IndentedTexBlock endRegex;
    public List<IndentedTexBlock> objects;
    public final int objectsIndent;
    public String text;
    public final boolean needLineBreakAtHead;
    public IndentedTexBlock(IndentedTexBlock header, IndentedTexBlock footer, @NonNull List<IndentedTexBlock> blocks, int objectsIndent) {
        this.startRegex = header;
        this.endRegex = footer;
        this.objects = blocks;
        this.objectsIndent = objectsIndent;
        this.needLineBreakAtHead = false;
    }

    public IndentedTexBlock(@NonNull String text, int objectsIndent, boolean needLineBreakAtHead) {
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
