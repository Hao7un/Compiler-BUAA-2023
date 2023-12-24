package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;

public class LAndExpNode {
    // LAndExp → EqExp | LAndExp '&&' EqExp
    // transform into  LAndExp → EqExp | EqExp '&&' LAndExp
    private EqExpNode eqExpNode;
    private LAndExpNode lAndExpNode;
    private Token and;

    public LAndExpNode(EqExpNode eqExpNode) {
        this.eqExpNode = eqExpNode;
    }
    public LAndExpNode(EqExpNode eqExpNode, Token and, LAndExpNode lAndExpNode) {
        this.eqExpNode = eqExpNode;
        this.lAndExpNode = lAndExpNode;
        this.and = and;
    }

    public LAndExpNode getlAndExpNode() {
        return lAndExpNode;
    }

    public EqExpNode getEqExpNode() {
        return eqExpNode;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(eqExpNode.toString());
        sb.append("<LAndExp>\n");

        if (and != null) {
            sb.append(and.toString());
            sb.append(lAndExpNode);
        }

        return sb.toString();
    }

}
