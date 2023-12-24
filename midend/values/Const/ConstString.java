package midend.values.Const;

import midend.types.IntegerType;
import midend.types.PointerType;
import midend.types.ValueType;
import midend.values.Value;

public class ConstString extends Value {

    private String stringValue;
    private int stringLength;
    public ConstString(String stringContent) {
        super(stringContent, new PointerType(new IntegerType(8)));  // i8*
        this.stringLength = stringContent.length() + 1;
        this.stringValue = stringContent.replace("\\n","\\0a") + "\\00";
    }

    @Override
    public String toString() {
        return "[" + stringLength + " x " + ((PointerType)getValueType()).getPointedType() +"] c\"" + stringValue + "\"";
    }
}
