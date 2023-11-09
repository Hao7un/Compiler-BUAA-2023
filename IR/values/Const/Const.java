package IR.values.Const;

import IR.types.ValueType;
import IR.values.User;
import IR.values.Value;

public class Const extends User {

    public Const(String valueName, ValueType valueType) {
        super(valueName, valueType);
    }
}
