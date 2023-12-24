package midend.values.Instructions.binary;

import midend.types.IntegerType;
import midend.values.BasicBlock;
import midend.values.Instructions.IRInstruction;
import midend.values.Value;

import java.util.HashSet;


public class Icmp extends IRInstruction {

    String icmpOp;

    /*Icmp support following operations:
    * eq  ==
    * ne  !=
    * sge >=
    * sgt >
    * sle <=
    * slt <
    * */

    public Icmp(String name, BasicBlock basicBlock, String icmpOp, Value operand1,Value operand2) {
        super(name,new IntegerType(1),basicBlock);
        this.icmpOp = icmpOp;

        addOperand(operand1);
        addOperand(operand2);
    }

    public HashSet<Value> getUseValue() {
        return new HashSet<>(getOperands());
    }

    public Value getDefValue() {
        return this;
    }

    public String getIcmpOp() {
        return icmpOp;
    }

    @Override
    public String toString() {
        return getValueName() + " = icmp " + icmpOp + " " +
                getOperand(0).getValueType() + " " +
                getOperand(0).getValueName() + ", "  + getOperand(1).getValueName();
    }
}
