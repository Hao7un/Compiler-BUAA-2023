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

    public void print() throws IOException {
        InOututils.write(intToken.toString(),"output.txt");
        InOututils.write(mainToken.toString(),"output.txt");
        InOututils.write(leftParent.toString(),"output.txt");
        InOututils.write(rightParent.toString(),"output.txt");
        blockNode.print();
        InOututils.write("<MainFuncDef>","output.txt");
    }
}
