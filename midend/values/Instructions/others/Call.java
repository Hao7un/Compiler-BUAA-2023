package midend.values.Instructions.others;

import midend.types.FunctionType;
import midend.types.IntegerType;
import midend.values.BasicBlock;
import midend.values.Const.Function;
import midend.values.Instructions.IRInstruction;
import midend.values.Value;

import java.util.ArrayList;
import java.util.HashSet;

public class Call extends IRInstruction {
    /* %3 = call i32 @getint() */
    /* call void @putch(i32 58) */
    public Call(String name, FunctionType functionType, BasicBlock basicBlock, Function function, ArrayList<Value> arguments) {
        super(name, functionType, basicBlock);
        // name  : null or "%*"
        //valueType : void or i 32
        addOperand(function);
        for (Value value : arguments) {
            addOperand(value);
        }
        /*first operand is function , the rest are arguments*/
    }

    public HashSet<Value> getUseValue() {
        HashSet<Value> useValue =  new HashSet<>();
        for (int i = 1; i < getOperands().size(); i++) {
            useValue.add(getOperand(i));
        }
        return useValue;
    }

    public Value getDefValue() {
        if (((FunctionType)getValueType()).getReturnType() instanceof IntegerType) {
            return this;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (((FunctionType)getValueType()).getReturnType() instanceof IntegerType) {
            sb.append(this.getValueName());
            sb.append(" = ");
        }
        sb.append("call ");
        sb.append(((FunctionType)this.getValueType()).getReturnType().toString());
        sb.append(" ");
        sb.append("@").append((getOperand(0)).getValueName());
        sb.append("(");
        for (int i = 1; i < this.getOperands().size();i++) {
            Value argument = this.getOperand(i);
            sb.append(argument.getValueType().toString());
            sb.append(" ");
            sb.append(argument.getValueName());
            if (i != this.getOperands().size() - 1) {
                sb.append(",");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
