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

    public void print() throws IOException {
        InOututils.write(funcType.toString(),"output.txt");
        InOututils.write("<FuncType>","output.txt");
    }
}
