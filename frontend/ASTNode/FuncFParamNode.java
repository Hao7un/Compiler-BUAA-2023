package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;
import java.util.ArrayList;

public class FuncFParamNode {
    //FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    private BTypeNode bTypeNode;
    private Token ident;
    private ArrayList<Token> leftBracks;
    private ArrayList<Token> rightBracks;
    private ArrayList<ConstExpNode> constExpNodes;

    public FuncFParamNode(BTypeNode bTypeNode, Token ident, ArrayList<Token> leftBracks,
                          ArrayList<Token> rightBracks, ArrayList<ConstExpNode> constExpNodes) {
        this.bTypeNode = bTypeNode;
        this.ident = ident;
        this.leftBracks = leftBracks;
        this.rightBracks = rightBracks;
        this.constExpNodes = constExpNodes;
    }

    public Token getIdent() {
        return ident;
    }

    public ArrayList<Token> getLeftBracks() {
        return leftBracks;
    }

    public Integer getDimension() { //获取函数参数的维度
        return leftBracks.size();
    }

    public ArrayList<ConstExpNode> getConstExpNodes() {
        return constExpNodes;
    }

    public void print() throws IOException {
        bTypeNode.print();
        InOututils.write(ident.toString(),"output.txt");
        if (leftBracks != null && leftBracks.size() != 0) {
            InOututils.write(leftBracks.get(0).toString(),"output.txt");
            InOututils.write(rightBracks.get(0).toString(),"output.txt");
            int i;
            for (i = 1; i < leftBracks.size(); i++) {
                InOututils.write(leftBracks.get(i).toString(),"output.txt");
                constExpNodes.get(i-1).print();
                InOututils.write(rightBracks.get(i).toString(),"output.txt");
            }
        }
        InOututils.write("<FuncFParam>","output.txt");
    }
}
