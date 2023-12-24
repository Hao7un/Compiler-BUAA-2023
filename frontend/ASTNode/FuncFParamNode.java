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

    public String toString() {
        StringBuilder sb =  new StringBuilder();
        sb.append(bTypeNode.toString());
        sb.append(ident.toString());
        if (leftBracks != null && !leftBracks.isEmpty()) {
            sb.append(leftBracks.get(0).toString());
            sb.append(rightBracks.get(0).toString());
            int i;
            for (i = 1; i < leftBracks.size(); i++) {
                sb.append(leftBracks.get(i).toString());
                sb.append(constExpNodes.get(i-1).toString());
                sb.append(rightBracks.get(i).toString());
            }
        }
        sb.append("<FuncFParam>\n");
        return sb.toString();
    }
}
