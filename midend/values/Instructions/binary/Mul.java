package midend.values.Instructions.binary;

import midend.types.IntegerType;
import midend.values.BasicBlock;
import midend.values.Instructions.IRInstruction;
import midend.values.Value;

import java.util.HashSet;

public class Mul extends IRInstruction {

    public Mul(String name,BasicBlock basicBlock,Value leftOperand,Value rightOperand) {
        super(name,new IntegerType(32),basicBlock);
        addOperand(leftOperand);
        addOperand(rightOperand);
    }

    public HashSet<Value> getUseValue() {
        return new HashSet<>(getOperands());
    }

    public Value getDefValue() {
        return this;
    }

    @Override
    public String toString() {
        return this.getValueName() + " = mul i32" + " " +
                this.getOperand(0).getValueName() + ", "  + this.getOperand(1).getValueName();
    }

}
