package midend.values.Const;

import midend.types.ArrayType;
import midend.types.IntegerType;
import midend.types.PointerType;
import midend.types.ValueType;
import midend.values.Value;

public class GlobalVar extends Const{
    private Value varValue;
    private Boolean isConst;

    private static int stringCounter = 0;
    public GlobalVar(String name, ValueType type, Value varValue, Boolean isConst) {
        super(name,new PointerType(type));
        this.varValue = varValue;
        this.isConst = isConst;
    }

    public GlobalVar(String stringContent) {   // 全局字符串
        super("@str_" + stringCounter,new PointerType(new ArrayType(new IntegerType(8),stringContent.length() + 1)));
        stringCounter++;
        this.varValue = new ConstString(stringContent);
        this.isConst = true;
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
