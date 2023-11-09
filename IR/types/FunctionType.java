package IR.types;

import java.util.ArrayList;

public class FunctionType implements ValueType {
    private ValueType returnType;                //函数返回值类型
    public ArrayList<ValueType> parametersType;  // 函数形参类型

    public FunctionType(ValueType returnType, ArrayList<ValueType> parametersType) {
        this.returnType = returnType;
        this.parametersType = parametersType;
    }

    public ValueType getReturnType() {
        return returnType;
    }

    public ArrayList<ValueType> getParametersType() {
        return parametersType;
    }

    @Override
    public String toString() {
        return this.returnType.toString();
    }

    @Override
    public int getSize() {
        /*Should not reach here*/
        return -1;
    }
}
