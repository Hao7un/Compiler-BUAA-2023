package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;

public class LOrExpNode {
    //  LOrExp → LAndExp | LOrExp '||' LAndExp
    // transform into LOrExp → LAndExp | LAndExp '||' LOrExp
    private LAndExpNode lAndExpNode;
    private Token or;
    private LOrExpNode lOrExpNode;

    public LOrExpNode(LAndExpNode lAndExpNode) {
        this.lAndExpNode = lAndExpNode;
    }

    public LOrExpNode(LAndExpNode lAndExpNode, Token or, LOrExpNode lOrExpNode) {
        this.lAndExpNode = lAndExpNode;
        this.or = or;
        this.lOrExpNode = lOrExpNode;
    }

    public LOrExpNode getlOrExpNode() {
        return lOrExpNode;
    }

    public LAndExpNode getlAndExpNode() {
        return lAndExpNode;
    }

    public void print() throws IOException {
        lAndExpNode.print();
        InOututils.write("<LOrExp>","output.txt");
        if (or != null) {
            InOututils.write(or.toString(),"output.txt");
            lOrExpNode.print();
        }
    }
}
