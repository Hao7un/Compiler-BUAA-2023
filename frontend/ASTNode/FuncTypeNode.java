package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;

public class FuncTypeNode {
    //  FuncType → 'void' | 'int'
    private Token funcType;

    public FuncTypeNode(Token funcType) {
        this.funcType = funcType;
    }

    public Token getFuncType() {
        return funcType;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(funcType.toString());
        sb.append("<FuncType>\n");

        return sb.toString();
    }
}
