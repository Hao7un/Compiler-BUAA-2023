package IR.types;

public class VoidType implements ValueType {
    private static final VoidType voidType = new VoidType();

    public static VoidType getVoidType() {
        return voidType;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public String toString() {
        return "void";
    }
}
