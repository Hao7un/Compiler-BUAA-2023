package IR.values.Instructions.binary;

import IR.types.IntegerType;
import IR.values.BasicBlock;
import IR.values.Instructions.IRInstruction;
import IR.values.Value;

public class Srem extends IRInstruction {
    public Srem(String name,BasicBlock basicBlock,Value leftOperand,Value rightOperand) {
        super(name,new IntegerType(32),basicBlock);
        addOperand(leftOperand);
        addOperand(rightOperand);
    }

    @Override
    public String toString() {
        return this.getValueName() + " = srem i32" + " " +
                this.getOperand(0).getValueName() + ", "  + this.getOperand(1).getValueName();
    }

}
