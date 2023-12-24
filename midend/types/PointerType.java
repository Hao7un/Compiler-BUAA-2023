package midend.types;

public class PointerType implements ValueType {
    /*
     * 记录指针的信息
     */
    private ValueType pointedType; // alloca , getlemenptr

    public PointerType(ValueType pointedType) {
        this.pointedType = pointedType;
    }

    public ValueType getPointedType() {
        return pointedType;
    }

    @Override
    public String toString() {
        return pointedType.toString() + "*";
    }

    @Override
    public int getSize() {
        return 4;
    }
}
