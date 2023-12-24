package midend.values.Instructions.others;

import midend.types.ValueType;
import midend.types.VoidType;
import midend.values.BasicBlock;
import midend.values.Instructions.IRInstruction;
import midend.values.Value;

import java.util.ArrayList;

public class PC extends IRInstruction {
    private ArrayList<Value> dsts;
    private ArrayList<Value> srcs;

    public PC(String name,BasicBlock parentBasicBlock) {
        super(name,new VoidType(),parentBasicBlock);
        this.dsts = new ArrayList<>();
        this.srcs = new ArrayList<>();
    }

    public void addDst(Value dst) {
        this.dsts.add(dst);
    }

    public void addSrc(Value src) {
        this.srcs.add(src);
    }

    public ArrayList<Value> getDsts() {
        return dsts;
    }

    public ArrayList<Value> getSrcs() {
        return srcs;
    }
}
