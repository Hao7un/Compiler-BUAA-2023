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

    public void print() throws IOException {
        addExpNode.print();
        InOututils.write("<Exp>","output.txt");
    }


}
