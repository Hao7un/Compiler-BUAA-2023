package midend.values.Instructions.others;

import midend.types.IntegerType;
import midend.types.ValueType;
import midend.values.BasicBlock;
import midend.values.Instructions.IRInstruction;
import midend.values.UndefinedValue;
import midend.values.Value;

import java.util.ArrayList;
import java.util.HashSet;

public class Phi extends IRInstruction {

    private ArrayList<BasicBlock> prevs;
    public Phi(String name, ArrayList<BasicBlock> prevs, BasicBlock basicBlock) {
        super(name, new IntegerType(32), basicBlock);
        this.prevs = prevs;
        for (BasicBlock prevBlock : prevs) {
            addOperand(new UndefinedValue());
        }
    }

    public void addPhiOperand(Value value, BasicBlock prevBlock) {
        value.addUse(this); //与User的addOperand保持一致
        int idx = prevs.indexOf(prevBlock);
        setOperand(value,idx);
    }

    public HashSet<Value> getUseValue() {
        return new HashSet<>(getOperands());
    }

    // 寄存器分配中用到
    public Value getDefValue() {
        return this;
    }

    @Override
    public String toString() {
        // %4 = phi i32 [ 1, %2 ], [ %6, %5 ]
        StringBuilder sb = new StringBuilder();
        sb.append(getValueName());
        sb.append(" = phi ");
        sb.append(getValueType());
        sb.append(" ");
        int idx = 0;
        for (Value operand : getOperands()) {
            sb.append("[ ");
            sb.append(operand.getValueName());
            sb.append(", ");
            sb.append("%");
            sb.append(prevs.get(idx).getValueName());
            sb.append(" ], ");
            idx ++;
        }
        sb.delete(sb.length()-2,sb.length());
        return sb.toString();
    }
}
