package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;

public class MainFuncDefNode {
    // MainFuncDef → 'int' 'main' '(' ')' Block
    private Token intToken;
    private Token mainToken;
    private Token leftParent;
    private Token rightParent;
    private BlockNode blockNode;

    public MainFuncDefNode(Token intToken, Token mainToken, Token leftParent, Token rightParent, BlockNode blockNode) {
        this.intToken = intToken;
        this.mainToken = mainToken;
        this.leftParent = leftParent;
        this.rightParent = rightParent;
        this.blockNode = blockNode;
    }

    public Token getMainToken() {
        return mainToken;
    }

    public BlockNode getBlockNode() {
        return blockNode;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(intToken.toString());
        sb.append(mainToken.toString());
        sb.append(leftParent.toString());
        sb.append(rightParent.toString());
        sb.append(blockNode.toString());
        sb.append("<MainFuncDef>\n");

        return sb.toString();
    }
}
