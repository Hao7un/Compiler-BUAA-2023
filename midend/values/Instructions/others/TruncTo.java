package midend.values.Instructions.others;

import midend.types.ValueType;
import midend.values.BasicBlock;
import midend.values.Instructions.IRInstruction;
import midend.values.Value;

import java.util.HashSet;

public class TruncTo extends IRInstruction {
    public TruncTo(String name, ValueType valueType, BasicBlock basicBlock, Value operand1) {
        super(name, valueType, basicBlock);
        addOperand(operand1);
    }

    public HashSet<Value> getUseValue() {
        HashSet<Value> useValue = new HashSet<>();
        useValue.add(getOperand(0));
        return useValue;
    }

    // 寄存器分配中用到
    public Value getDefValue() {
        return this;
    }

    @Override
    public String toString() {
        return  this.getValueName() + " = trunc " + getOperand(0).getValueType().toString() + " " + getOperand(0).getValueName() +
                " to " + getValueType().toString();
    }
}
