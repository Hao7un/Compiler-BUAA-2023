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

    public void print() throws IOException {
        InOututils.write(operator.toString(),"output.txt");
        InOututils.write("<UnaryOp>","output.txt");
    }
}
