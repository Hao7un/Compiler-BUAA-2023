package IR.values.Instructions.binary;

import IR.types.IntegerType;
import IR.values.BasicBlock;
import IR.values.Instructions.IRInstruction;
import IR.values.Value;


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
