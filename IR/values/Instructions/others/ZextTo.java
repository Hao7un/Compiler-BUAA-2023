package IR.values.Instructions.others;

import IR.types.ValueType;
import IR.values.BasicBlock;
import IR.values.Instructions.IRInstruction;
import IR.values.Value;

public class ZextTo extends IRInstruction {
    public ZextTo(String name, ValueType valueType, BasicBlock basicBlock, Value operand1) {
        super(name, valueType, basicBlock);
        addOperand(operand1);
    }

    @Override
    public String toString() {
        return  this.getValueName() + " = zext " + getOperand(0).getValueType().toString() + " " + getOperand(0).getValueName() +
        " to " + getValueType().toString();
    }
}
