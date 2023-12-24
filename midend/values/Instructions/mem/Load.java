package midend.values.Instructions.mem;

import midend.types.ValueType;
import midend.values.BasicBlock;
import midend.values.Instructions.IRInstruction;
import midend.values.Value;

import java.util.HashSet;

public class Load extends IRInstruction {
    public Load(String name, ValueType valueType,BasicBlock basicBlock, Value operand) {
        super(name, valueType, basicBlock);
        addOperand(operand);
    }


    public HashSet<Value> getUseValue() {
        return new HashSet<>(getOperands());
    }

    public Value getDefValue() {
        return this;
    }

    @Override
    public String toString() {
        return this.getValueName() + " = " + "load " + getValueType() +
                ", " + this.getOperand(0).getValueType() +
                " " +
                this.getOperand(0).getValueName();
    }
}
