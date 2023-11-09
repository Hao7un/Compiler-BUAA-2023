package IR.values.Instructions.mem;

import IR.types.VoidType;
import IR.values.BasicBlock;
import IR.values.Instructions.IRInstruction;
import IR.values.Value;

public class Store extends IRInstruction {
    public Store(String name,BasicBlock basicBlock , Value storeValue, Value operand) {
        super(name, new VoidType(), basicBlock);
        addOperand(storeValue);
        addOperand(operand);
    }

    @Override
    public String toString() {
        return "store " +this.getOperand(0).getValueType() +
                " " +
                this.getOperand(0).getValueName() +
                ", " +
                this.getOperand(1).getValueType() +
                " " +
                this.getOperand(1).getValueName();
    }
}
