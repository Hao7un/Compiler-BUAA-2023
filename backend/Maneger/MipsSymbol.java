package backend.Maneger;

public class MipsSymbol {
    private final String name;
    private final int offset;
    public MipsSymbol(String name,int offset) {
        this.name = name;
        this.offset = offset;
    }

    public String getName() {
        return name;
    }
    public int getOffset() {
        return offset;
    }
}
