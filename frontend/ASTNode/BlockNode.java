package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;
import java.util.ArrayList;

public class BlockNode {
    //Block → '{' { BlockItem } '}'
    private Token leftBrace;
    private ArrayList<BlockItemNode> blockItemNodes;
    private Token rightBrace;

    public BlockNode(Token leftBrace, ArrayList<BlockItemNode> blockItemNodes, Token rightBrace) {
        this.leftBrace = leftBrace;
        this.blockItemNodes = blockItemNodes;
        this.rightBrace = rightBrace;
    }

    public ArrayList<BlockItemNode> getBlockItemNodes() {
        return blockItemNodes;
    }

    public ArrayList<Token> getReturnTokens(){
        ArrayList<Token> returnTokens = new ArrayList<>();
        for (BlockItemNode blockItemNode : blockItemNodes) {
            ArrayList<Token> temp = blockItemNode.getReturnTokens();
            returnTokens.addAll(temp);
        }
        return returnTokens;
    }

    public StmtNode getLastStmt() {
        int len = blockItemNodes.size();
        if (len == 0) {
            return null;
        } else {
            return blockItemNodes.get(len - 1).getStmtNode();
        }
    }
    public Token getRightBrace() {
        return rightBrace;
    }


    public void print() throws IOException {
        InOututils.write(leftBrace.toString(),"output.txt");
        for (BlockItemNode blockItemNode : blockItemNodes) {
            blockItemNode.print();
        }
        InOututils.write(rightBrace.toString(),"output.txt");
        InOututils.write("<Block>","output.txt");
    }

}
