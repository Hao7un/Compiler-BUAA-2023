package backend.Symbol;

import frontend.Symbol.Symbol;

import java.util.HashMap;

public class MipsSymbolTable {
    private HashMap<String, MipsSymbol> symbols; // symbol table

    public MipsSymbolTable() {
        this.symbols = new HashMap<>();
    }

    public boolean containSymbol(String name) {
        return symbols.containsKey(name);
    }


    public void addSymbol(MipsSymbol mipsSymbol) {
        symbols.put(mipsSymbol.getName(),mipsSymbol);
    }

    public MipsSymbol getSymbol(String name) {
        return symbols.get(name);
    }
}
