package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;

public class BTypeNode {
    // BType → 'int'
    private Token intToken;
    public BTypeNode(Token intToken) {
        this.intToken = intToken;
    }

    public void print() throws IOException {
        InOututils.write(intToken.toString(),"output.txt");
    }
}
