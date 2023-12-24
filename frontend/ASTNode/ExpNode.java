package frontend.ASTNode;

import frontend.Symbol.SymbolTable;
import utils.InOututils;

import java.io.IOException;

public class ExpNode {
    // Exp → AddExp
    private AddExpNode addExpNode;
    public ExpNode(AddExpNode addExpNode) {
        this.addExpNode = addExpNode;
    }

    public AddExpNode getAddExpNode() {
        return addExpNode;
    }

    public int getDimension(SymbolTable currentSymbolTable) {
        return addExpNode.getDimension(currentSymbolTable);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(addExpNode.toString());
        sb.append("<Exp>\n");
        return sb.toString();
    }

}
