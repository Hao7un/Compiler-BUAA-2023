package backend.Components;

import IR.types.IntegerType;
import IR.values.Const.Function;
import backend.Symbol.MipsSymbol;
import backend.Symbol.MipsSymbolTable;

import java.util.ArrayList;

public class MipsFunction {
    /*MIPS 函数*/
    private String name;
    private Function irFunction;
    private ArrayList<MipsBasicBlock> mipsBasicBlocks;
    private MipsSymbolTable mipsSymbolTable;
    public MipsFunction(String name,Function irFunction) {
        this.name = name;
        this.irFunction = irFunction;
        this.mipsBasicBlocks = new ArrayList<>();
        this.mipsSymbolTable = new MipsSymbolTable();
    }


    public String getName() {
        return name;
    }

    public Integer getNameOffset(String name) {
        if (mipsSymbolTable.containSymbol(name)) {
            return mipsSymbolTable.getSymbol(name).getOffset();
        } else {
            return null;
        }
    }

    public MipsSymbolTable getMipsSymbolTable() {
        return mipsSymbolTable;
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
        mipsSymbolTable.addSymbol(mipsSymbol);
    }
}
