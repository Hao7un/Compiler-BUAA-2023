package midend.values.Instructions.terminator;

import midend.types.VoidType;
import midend.values.BasicBlock;
import midend.values.Value;
import midend.values.Instructions.IRInstruction;

import java.util.HashSet;

public class Ret extends IRInstruction {
    private boolean isVoid;
    public Ret(BasicBlock basicBlock) {
        super("",new VoidType(),basicBlock);
        this.isVoid = true;
    }

    public Ret(BasicBlock basicBlock, Value returnValue) {
        super("",returnValue.getValueType(), basicBlock);
        addOperand(returnValue);
        this.isVoid = false;
    }

    public boolean isVoid() {
        return isVoid;
    }

    public HashSet<Value> getUseValue() {
        if (isVoid) {
            return new HashSet<>();
        } else {
            HashSet<Value> useValue = new HashSet<>();
            useValue.add(getOperand(0));
            return useValue;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isVoid) {
            sb.append("ret void");
        } else {
            sb.append("ret i32 ");
            sb.append(this.getOperand(0).getValueName());
        }
        return sb.toString();
    }
}
