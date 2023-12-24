package midend.values;

import midend.types.ValueType;

public class Argument extends Value{
    public Argument(String valueName, ValueType valueType) {
        /*e.g. i32 %1*/
        super(valueName, valueType);
    }

}
