package IR.values.Instructions.binary;

import IR.types.IntegerType;
import IR.values.BasicBlock;
import IR.values.Instructions.IRInstruction;
import IR.values.Value;

public class Add extends IRInstruction {

    public Add(String name,BasicBlock basicBlock,Value leftOperand,Value rightOperand) {
        super(name,new IntegerType(32),basicBlock);
        addOperand(leftOperand);
        addOperand(rightOperand);
    }

    @Override
    public String toString() {
        return this.getValueName() + " = add i32" + " " +
                this.getOperand(0).getValueName() + ", "  + this.getOperand(1).getValueName();
    }

}
