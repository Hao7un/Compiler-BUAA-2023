package IR.values.Instructions.terminator;

import IR.types.VoidType;
import IR.values.BasicBlock;
import IR.values.Instructions.IRInstruction;
import IR.values.Value;

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

    public BasicBlock getTrueBlock() {
        return trueBlock;
    }

    public BasicBlock getFalseBlock() {
        return falseBlock;
    }

    public boolean isDirectBranch() {
        return this.falseBlock == null;
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
