package midend.values.Instructions.mem;

import midend.types.VoidType;
import midend.values.BasicBlock;
import midend.values.Instructions.IRInstruction;
import midend.values.Value;

import java.util.HashSet;

public class Store extends IRInstruction {
    public Store(String name,BasicBlock basicBlock , Value storeValue, Value operand) {
        super(name, new VoidType(), basicBlock);
        addOperand(storeValue);
        addOperand(operand);
    }

    public Value getToValue() {
        return this.getOperand(1);
    }

    public Value getFromValue() {
        return this.getOperand(0);
    }

    public HashSet<Value> getUseValue() {
        return new HashSet<>(getOperands());
    }

    // 寄存器分配中用到
    public Value getDefValue() {
        return null;
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
