package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;

public class UnaryOpNode {
    //UnaryOp → '+' | '−' | '!'
    private Token operator;
    public UnaryOpNode(Token operator) {
        this.operator = operator;
    }

    public Token getOperator() {
        return operator;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(operator.toString());
        sb.append("<UnaryOp>\n");
        return sb.toString();
    }
}
