package frontend.ASTNode;

import utils.InOututils;

import java.io.IOException;

public class ConstExpNode {
    //  ConstExp → AddExp
    private AddExpNode addExpNode;
    public ConstExpNode(AddExpNode addExpNode) {
        this.addExpNode = addExpNode;
    }

    public AddExpNode getAddExpNode() {
        return addExpNode;
    }

    public void print() throws IOException {
        addExpNode.print();
        InOututils.write("<ConstExp>","output.txt");
    }
}
