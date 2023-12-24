package backend.Maneger;

import java.util.HashMap;

public class StackManager {
    private HashMap<String, MipsSymbol> symbols; // symbol table

    public StackManager() {
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
