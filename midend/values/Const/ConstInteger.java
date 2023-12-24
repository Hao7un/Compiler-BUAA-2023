package midend.values.Const;

import midend.types.IntegerType;

public class ConstInteger extends Const{
    private int bits;
    private int value;
    public ConstInteger(int bits, int value) {
        super(String.valueOf(value),new IntegerType(bits));
        this.bits = bits;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "i32" + " " + this.getValueName();
    }
}
