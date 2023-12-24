package backend.Components;

import midend.values.Const.Function;
import backend.Maneger.MipsSymbol;
import backend.Maneger.StackManager;

import java.util.ArrayList;

public class MipsFunction {
    /*MIPS 函数*/
    private String name;
    private Function irFunction;
    private ArrayList<MipsBasicBlock> mipsBasicBlocks;
    private StackManager stackManager;
    public MipsFunction(String name,Function irFunction) {
        this.name = name;
        this.irFunction = irFunction;
        this.mipsBasicBlocks = new ArrayList<>();
        this.stackManager = new StackManager();
    }

    public ArrayList<MipsBasicBlock> getMipsBasicBlocks() {
        return mipsBasicBlocks;
    }

    public String getName() {
        return name;
    }

    public Integer getNameOffset(String name) {
        if (stackManager.containSymbol(name)) {
            return stackManager.getSymbol(name).getOffset();
        } else {
            return null;
        }
    }

    public StackManager getMipsSymbolTable() {
        return stackManager;
    }

    public void addBasicBlock(MipsBasicBlock mipsBasicBlock) {
        this.mipsBasicBlocks.add(mipsBasicBlock);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name);
        sb.append(":\n");
        for (MipsBasicBlock mipsBasicBlock : mipsBasicBlocks) {
            sb.append(mipsBasicBlock.toString());
        }
        sb.append("\n");
        return sb.toString();
    }

    public void insertToSymbolTable(String name, int curOffset) {
        MipsSymbol mipsSymbol = new MipsSymbol(name,curOffset);
        stackManager.addSymbol(mipsSymbol);
    }
}
