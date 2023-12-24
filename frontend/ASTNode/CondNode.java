package frontend.ASTNode;

import utils.InOututils;

import java.io.IOException;

public class CondNode {
    //Cond → LOrExp
    private LOrExpNode lOrExpNode;
    public CondNode(LOrExpNode lOrExpNode) {
        this.lOrExpNode = lOrExpNode;
    }

    public LOrExpNode getlOrExpNode() {
        return lOrExpNode;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lOrExpNode.toString());
        sb.append("<Cond>\n");
        return sb.toString();
    }
}
