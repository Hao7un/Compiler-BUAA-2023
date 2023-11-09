package IR.values.Instructions.mem;

import IR.types.ArrayType;
import IR.types.IntegerType;
import IR.types.PointerType;
import IR.types.ValueType;
import IR.values.BasicBlock;
import IR.values.Instructions.IRInstruction;
import IR.values.Value;

import java.util.ArrayList;

public class GetElementPtr extends IRInstruction {
    // <result> = getelementptr <ty>, <ty>* <ptrval>, {<ty> <index>}*
    public GetElementPtr(String name, Value pointer, ArrayList<Value> indices, BasicBlock basicBlock) {
        super(name,new PointerType(getElementType(pointer,indices)), basicBlock);
        addOperand(pointer);
        for (Value index : indices ) {
            addOperand(index);
        }
    }

    public static ValueType getElementType(Value pointer , ArrayList<Value> indices) {
        /*Calculate the value type this pointer is pointing at*/
        ValueType valueType = ((PointerType) pointer.getValueType()).getPointedType();
        if (valueType instanceof IntegerType) {
            return valueType;
        } else if (valueType instanceof ArrayType) {
            for (int i = 1; i < indices.size(); i++) {
                valueType = ((ArrayType) valueType).getElementType();
            }
            return valueType;
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getValueName());
        sb.append(" = getelementptr ");

        sb.append(((PointerType) getOperand(0).getValueType()).getPointedType());
        sb.append(", ");

        sb.append(getOperand(0).getValueType().toString());
        sb.append(" ");
        sb.append(getOperand(0).getValueName());
        sb.append(", ");
        for (int i = 1; i < getOperands().size() ; i++) {
            Value value = getOperand(i);
            sb.append(value.getValueType());
            sb.append(" ");
            sb.append(value.getValueName());
            sb.append(", ");
        }
        sb.delete(sb.length()-2,sb.length());
        return sb.toString();
    }
}
