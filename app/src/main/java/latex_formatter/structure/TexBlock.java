package latex_formatter.structure;

import latex_formatter.config.Config;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class TexBlock {
    public final Object startRegex;
    public final Object endRegex;
    public final List<Object> objects;
    public final boolean lineBreakAfterStartRegex;
    public final Config.TexBlockRegexPair exteriorRegex;
    public TexBlock(Object startRegex, Object endRegex, List<Object> object, boolean lineBreakAfterStartRegex, Config.TexBlockRegexPair regex) {
        this.startRegex = startRegex;
        this.endRegex = endRegex;
        this.objects = object;
        this.lineBreakAfterStartRegex = lineBreakAfterStartRegex;
        this.exteriorRegex = regex;
    }

    public TexBlock(String object) {
        this.objects = new ArrayList<>();
        this.objects.add(object);
        this.startRegex = "";
        this.endRegex = "";
        this.lineBreakAfterStartRegex = false;
        this.exteriorRegex = Config.TexBlockRegexPair.EMPTY;
    }

    public String toString() {
        if (this.isRawText()) {
            return objects.toString();
        }
        return "[" + startRegex.toString() + ", " + (lineBreakAfterStartRegex ? "break, " : "") + this.objects.toString() + ", " + endRegex.toString() + "]";
    }

    public boolean isRawText() {
        boolean flag0 = this.startRegex instanceof String && ((String)this.startRegex).isEmpty();
        boolean flag1 = this.endRegex instanceof String && ((String)this.endRegex).isEmpty();
        boolean flag2 = this.objects.size() == 1 && this.objects.getFirst() instanceof String;
        return flag0 && flag1 && flag2;
    }

    public String getRawText() {
        if (this.isRawText()) {
            return (String)this.objects.getFirst();
        }
        return "";
    }

    public boolean isEquals(@NonNull TexBlock block) {
        if (block.startRegex instanceof String && this.startRegex instanceof String) {
            if (block.endRegex instanceof String && this.endRegex instanceof String) {
                return block.startRegex.equals(this.startRegex) && block.endRegex.equals(this.endRegex);
            }
        }
        return false;
    }

    public boolean isStartOf(Config.TexFullBlockPair pair) {
        if (this.startRegex instanceof String) {
            if (this.endRegex instanceof String) {
                return this.startRegex.equals(pair.primary.primaryRegex) && this.endRegex.equals(pair.primary.endRegex);
            }
        }
        return false;
    }

    public boolean isEndOf(Config.TexFullBlockPair pair) {
        if (this.startRegex instanceof String) {
            if (this.endRegex instanceof String) {
                return this.startRegex.equals(pair.end.primaryRegex) && this.endRegex.equals(pair.end.endRegex);
            }
        }
        return false;
    }

    public boolean isSameInteriorOf(@NonNull TexBlock texBlock) {
        if (texBlock.objects.size() == 1 && texBlock.objects.getFirst() instanceof String && this.objects.size() == 1 && this.objects.getFirst() instanceof String) {
            return texBlock.objects.getFirst().equals(this.objects.getFirst());
        }
        return false;
    }
}
