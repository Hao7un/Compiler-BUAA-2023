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

    public void print() throws IOException {
        eqExpNode.print();
        InOututils.write("<LAndExp>","output.txt");
        if (and != null) {
            InOututils.write(and.toString(),"output.txt");
            lAndExpNode.print();
        }
    }

}
