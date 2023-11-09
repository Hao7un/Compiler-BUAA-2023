package IR.values.Instructions.others;

import IR.types.FunctionType;
import IR.types.IntegerType;
import IR.values.BasicBlock;
import IR.values.Const.Function;
import IR.values.Instructions.IRInstruction;
import IR.values.Value;

import java.util.ArrayList;

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
