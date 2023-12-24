package frontend.Symbol;

import java.util.ArrayList;

public class FuncSymbol extends Symbol {
    private int retype; // 1: void 0: int

    private ArrayList<Symbol> paramTypeList;
    public FuncSymbol(String name, int retype, ArrayList<Symbol> paramTypeList) {
        super(name);
        this.retype = retype;
        this.paramTypeList = paramTypeList;
    }

    public int getRetype() {
        return retype;
    }

    public ArrayList<Symbol> getParamTypeList() {
        return paramTypeList;
    }

    public void setParamTypeList(ArrayList<Symbol> paramTypeList) {
        this.paramTypeList = paramTypeList;
    }
}
