package midend.values;

import midend.types.IntegerType;

public class UndefinedValue extends Value{
    public UndefinedValue() {
        super("0",new IntegerType(32));
    }

    @Override
    public String toString() {
        return "Undefined Value";
    }
}
