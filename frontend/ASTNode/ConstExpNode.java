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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(addExpNode.toString());
        sb.append("<ConstExp>\n");

        return sb.toString();
    }
}
