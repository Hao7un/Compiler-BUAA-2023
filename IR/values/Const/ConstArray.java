package IR.values.Const;

import IR.types.ValueType;
import IR.values.Value;

import java.util.ArrayList;

public class ConstArray extends Const{

    boolean hasInitialValue;

    public ConstArray(ValueType valueType , ArrayList<Value> elements) {
        super("", valueType);
        for (Value element : elements) {
            addOperand(element);
        }
        hasInitialValue = true;
    }

    public int getEleValue(int dim1) {
        return Integer.parseInt(getOperand(dim1).getValueName());
    }
    public int getEleValue(int dim1,int dim2) {
        return  Integer.parseInt(((ConstArray)getOperand(dim1)).getOperand(dim2).getValueName());
    }

    public boolean hasInitialValue() {
        return hasInitialValue;
    }

    public ConstArray(ValueType valueType) {
        super("", valueType);
        hasInitialValue = false;
    }

    /*Judging Whether the const array is all zero
    * can be optimized by zero initializer
    * */
    public boolean isAllZero() {
        ArrayList<Value> elements = getOperands();
        for (Value element : elements) {
            if (element instanceof ConstInteger) {
                if (((ConstInteger)element).getValue() != 0) {
                    return false;
                }
            } else if (element instanceof ConstArray) {
                if (!((ConstArray) element).isAllZero()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getValueType().toString());
        sb.append(" ");
        if (isAllZero() || !hasInitialValue) {
            sb.append("zeroinitializer");
        } else {
            sb.append("[");
            ArrayList<Value> elements = getOperands();
            for (int i = 0; i < elements.size(); i++) {
                sb.append(elements.get(i).toString());
                if (i != elements.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
        }
        return sb.toString();
    }
}
