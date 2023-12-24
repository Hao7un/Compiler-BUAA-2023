package midend.values.Instructions.others;

import midend.types.VoidType;
import midend.values.BasicBlock;
import midend.values.Instructions.IRInstruction;
import midend.values.Value;

public class Move extends IRInstruction {
    /*伪指令*/
    public Move(String name, Value dst, Value src, BasicBlock parentBlock) {
        super(name,new VoidType(),parentBlock);
        addOperand(dst);
        addOperand(src);
    }

    public void setSrc(Value newValue) {
        setOperand(newValue,1);
    }

    public Value getSrc() {
        return getOperand(1);
    }

    public Value getDst() {
        return getOperand(0);
    }

    @Override
    public String toString() {
        return "move " + getOperand(0).getValueType()+" " +getOperand(0).getValueName()
                + " <-- " + getOperand(1).getValueName();
    }
}
