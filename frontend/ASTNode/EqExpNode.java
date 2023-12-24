package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;

public class EqExpNode {
    // EqExp → RelExp | EqExp ('==' | '!=') RelExp
    //transform to RelExp | RelExp ('==' | '!=') EqExp

    private RelExpNode relExpNode;
    private Token operator;
    private EqExpNode eqExpNode;

    public EqExpNode(RelExpNode relExpNode) {
        this.relExpNode = relExpNode;
    }

    public EqExpNode(RelExpNode relExpNode, Token operator, EqExpNode eqExpNode) {
        this.relExpNode = relExpNode;
        this.operator = operator;
        this.eqExpNode = eqExpNode;
    }

    public EqExpNode getEqExpNode() {
        return eqExpNode;
    }

    public Token getOperator() {
        return operator;
    }

    public RelExpNode getRelExpNode() {
        return relExpNode;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(relExpNode.toString());
        sb.append("<EqExp>\n");
        if (operator!=null) {
            sb.append(operator.toString());
            sb.append(eqExpNode.toString());
        }
        return sb.toString();
    }
}
