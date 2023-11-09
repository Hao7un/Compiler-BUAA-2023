package IR.types;

public class IntegerType implements ValueType {
    /*
    * 代表了后面数字决定的位宽的类型
    * */

    private final int bits;

    public IntegerType(int bits) {
        this.bits = bits;
    }

    public int getBits() {
        return bits;
    }

    public int getSize() {
        return 4;
    }
    @Override
    public String toString() {
        return "i" + bits;
    }
}
