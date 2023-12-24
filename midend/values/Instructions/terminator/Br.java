package midend.values.Instructions.terminator;

import midend.types.VoidType;
import midend.values.BasicBlock;
import midend.values.Instructions.IRInstruction;
import midend.values.Value;

import java.util.HashSet;

public class Br extends IRInstruction {
    private BasicBlock trueBlock;
    private BasicBlock falseBlock;

    public Br(BasicBlock trueBlock,BasicBlock curBasicBlock) {
        super("", new VoidType(), curBasicBlock);
        this.trueBlock = trueBlock;
    }

    public Br(BasicBlock trueBlock, BasicBlock falseBlock, BasicBlock curBasicBlock, Value value) {
        super("", new VoidType(), curBasicBlock);
        this.trueBlock = trueBlock;
        this.falseBlock = falseBlock;
        addOperand(value);
    }

    public void setTrueBlock(BasicBlock trueBlock) {
        this.trueBlock = trueBlock;
    }

    public void setFalseBlock(BasicBlock falseBlock) {
        this.falseBlock = falseBlock;
    }

    public BasicBlock getTrueBlock() {
        return trueBlock;
    }

    public BasicBlock getFalseBlock() {
        return falseBlock;
    }

    public boolean isDirectBranch() {
        return this.falseBlock == null;
    }


    public HashSet<Value> getUseValue() {
        HashSet<Value> useValue = new HashSet<>();
        if (this.falseBlock == null) {
            return useValue;
        }
        useValue.add(getOperand(0));
        return useValue;
    }

    @Override
    public String toString() {
        if (this.falseBlock == null) {
            return  "br label %" + trueBlock.getValueName();
        } else {
            /*    br i1 %2, label %12, label %3 */
            return "br i1 " + getOperand(0).getValueName()+", label %" + trueBlock.getValueName() +
                    ", label %"+falseBlock.getValueName();
        }
    }

}
