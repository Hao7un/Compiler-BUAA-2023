package IR.values.Instructions.mem;

import IR.types.ValueType;
import IR.values.BasicBlock;
import IR.values.Instructions.IRInstruction;
import IR.values.Value;

public class Load extends IRInstruction {
    public Load(String name, ValueType valueType,BasicBlock basicBlock, Value operand) {
        super(name, valueType, basicBlock);
        addOperand(operand);
    }

    @Override
    public String toString() {
        return this.getValueName() + " = " + "load " + getValueType() +
                ", " + this.getOperand(0).getValueType() +
                " " +
                this.getOperand(0).getValueName();
    }
}
