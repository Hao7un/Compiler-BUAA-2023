package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;

public class NumberNode {
    // Number → IntConst
    private Token intConst;

    public NumberNode(Token intConst) {
        this.intConst = intConst;
    }

    public Token getIntConst() {
        return intConst;
    }

    public void print() throws IOException {
        InOututils.write(intConst.toString(),"output.txt");
        InOututils.write("<Number>","output.txt");
    }
}
