package backend.Components.instrutions;

public class MipsComment extends MipsInstruction{
    String content;

    public MipsComment(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "\n#"+content;
    }
}
