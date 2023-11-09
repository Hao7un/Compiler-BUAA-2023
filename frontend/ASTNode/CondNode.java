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

    public void print() throws IOException {
        lOrExpNode.print();
        InOututils.write("<Cond>","output.txt");
    }
}
