package midend.values.Instructions;

import midend.types.ValueType;
import midend.values.BasicBlock;
import midend.values.User;
import midend.values.Value;

import java.util.HashSet;

public class IRInstruction extends User {
    private BasicBlock parentBasicBlock;  // Instruction 所处在的Basic Block

    private boolean isUseful = false;
    public IRInstruction(String name, ValueType valueType, BasicBlock parentBasicBlock) {
        super(name, valueType);
        this.parentBasicBlock = parentBasicBlock;
    }

    public void setUseful() {
        this.isUseful = true;
    }

    public boolean isUseful() {
        return isUseful;
    }

    public BasicBlock getParentBasicBlock() {
        return parentBasicBlock;
    }

    // 寄存器分配中用到
    public HashSet<Value> getUseValue() {
        return new HashSet<>();
    }

    // 寄存器分配中用到
    public Value getDefValue() {
        return null;
    }


}
