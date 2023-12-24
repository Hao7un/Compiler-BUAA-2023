package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;

public class RelExpNode {
    // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    // transform into RelExp → AddExp |AddExp ('<' | '>' | '<=' | '>=')  RelExp
    private AddExpNode addExpNode;
    private RelExpNode relExpNode;
    private Token operator;

    public RelExpNode(AddExpNode addExpNode) {
        this.addExpNode = addExpNode;
    }

    public RelExpNode(AddExpNode addExpNode, Token operator, RelExpNode relExpNode) {
        this.addExpNode = addExpNode;
        this.relExpNode = relExpNode;
        this.operator = operator;
    }

    public RelExpNode getRelExpNode() {
        return relExpNode;
    }

    public Token getOperator() {
        return operator;
    }

    public AddExpNode getAddExpNode() {
        return addExpNode;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(addExpNode.toString());
        sb.append("<RelExp>\n");
        if (operator!=null) {
            sb.append(operator.toString());
            sb.append(relExpNode.toString());
        }
        return sb.toString();
    }
}
