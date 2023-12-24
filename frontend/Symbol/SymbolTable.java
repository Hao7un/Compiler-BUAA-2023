package frontend.Symbol;

import java.util.HashMap;

public class SymbolTable {
    private SymbolTable parentTable; 	// 当前符号表的parentTable。
    private HashMap<String, Symbol> symbols = new HashMap<>(); //  name -> frontend.Symbol

    public SymbolTable(SymbolTable parentTable) {
        this.parentTable = parentTable;
    }

    public Symbol getVarSymbol(String varName) {
        if (symbols.containsKey(varName) && symbols.get(varName) instanceof VarSymbol) {
            return symbols.get(varName);
        } else if (parentTable != null) {
            return parentTable.getVarSymbol(varName);
        } else {
            return null;
        }
    }

    public Symbol getFuncSymbol(String funcName) {
        if (symbols.containsKey(funcName) && symbols.get(funcName) instanceof FuncSymbol) {
            return symbols.get(funcName);
        } else if (parentTable != null) {
            return parentTable.getFuncSymbol(funcName);
        } else {
            return null;
        }
    }

    public HashMap<String, Symbol> getSymbols() {
        return this.symbols;
    }

    public SymbolTable getParentTable() {
        return this.parentTable;
    }
    public void addSymbol(Symbol symbol) {
        this.symbols.put(symbol.getName(),symbol);
    }
}
