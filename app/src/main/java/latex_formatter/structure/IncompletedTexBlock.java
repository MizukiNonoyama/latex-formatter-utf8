package latex_formatter.structure;

import latex_formatter.config.Config;

import java.util.List;

public class IncompletedTexBlock {
    public Object startRegex;
    public Object endRegex;
    public List<Object> objects;
    public boolean lineBreakAfterStartRegex;
    public Config.TexBlockRegexPair exteriorRegex;
    public IncompletedTexBlock(Object startRegex, Object endRegex, List<Object> object, boolean lineBreakAfterStartRegex) {
        this.startRegex = startRegex;
        this.endRegex = endRegex;
        this.objects = object;
        this.lineBreakAfterStartRegex = lineBreakAfterStartRegex;
        this.exteriorRegex = Config.TexBlockRegexPair.EMPTY;
    }

    public IncompletedTexBlock(Object startRegex, List<Object> object, boolean lineBreakAfterStartRegex) {
        this(startRegex, "", object, lineBreakAfterStartRegex);
    }

    public IncompletedTexBlock(Object startRegex, Object endRegex, List<Object> object) {
        this(startRegex, endRegex, object, true);
    }

    public TexBlock getFixed() {
        return new TexBlock(startRegex, endRegex, objects, lineBreakAfterStartRegex, exteriorRegex);
    }
}
