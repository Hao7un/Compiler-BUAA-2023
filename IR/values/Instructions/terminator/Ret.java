package IR.values.Instructions.terminator;

import IR.types.VoidType;
import IR.values.BasicBlock;
import IR.values.Value;
import IR.values.Instructions.IRInstruction;

public class Ret extends IRInstruction {
    private boolean isVoid;
    public Ret(BasicBlock basicBlock) {
        super("",new VoidType(),basicBlock);
        this.isVoid = true;
    }

    public Ret(BasicBlock basicBlock, Value returnValue) {
        super("",returnValue.getValueType(), basicBlock);
        addOperand(returnValue);
        this.isVoid = false;
    }

    public boolean isVoid() {
        return isVoid;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isVoid) {
            sb.append("ret void");
        } else {
            sb.append("ret i32 ");
            sb.append(this.getOperand(0).getValueName());
        }
        return sb.toString();
    }
}
