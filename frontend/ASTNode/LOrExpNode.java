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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lAndExpNode.toString());
        sb.append("<LOrExp>\n");
        if (or != null) {
            sb.append(or.toString());
            sb.append(lOrExpNode.toString());
        }
        return sb.toString();
    }
}
