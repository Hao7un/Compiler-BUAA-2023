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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(intConst.toString());
        sb.append("<Number>\n");
        return sb.toString();
    }
}
