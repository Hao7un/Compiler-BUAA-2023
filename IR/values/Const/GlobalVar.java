package IR.values.Const;

import IR.types.PointerType;
import IR.types.ValueType;
import IR.values.Value;

public class GlobalVar extends Const{
    private Value varValue;
    private Boolean isConst;
    public GlobalVar(String name, ValueType type, Value varValue, Boolean isConst) {
        super(name,new PointerType(type));
        this.varValue = varValue;
        this.isConst = isConst;
    }

    public Value getVarValue(){
        return varValue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getValueName());
        sb.append(" = ");
        if (isConst) {
            sb.append("constant");
        } else {
            sb.append("global");
        }
        sb.append(" ");
        sb.append(varValue.toString());
        return sb.toString();
    }
}
