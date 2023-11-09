package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;
import java.util.ArrayList;

public class VarDefNode {
    //VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
    private Token ident;
    private ArrayList<Token> leftBracks;
    private ArrayList<ConstExpNode> constExpNodes;
    private ArrayList<Token> rightBracks;

    private Token assignToken;
    private InitValNode initValNode;

    public VarDefNode(Token ident, ArrayList<Token> leftBracks, ArrayList<ConstExpNode> constExpNodes, ArrayList<Token> rightBracks ) {
        this.ident = ident;
        this.leftBracks = leftBracks;
        this.constExpNodes = constExpNodes;
        this.rightBracks = rightBracks;
    }
    public VarDefNode(Token ident, ArrayList<Token> leftBracks, ArrayList<ConstExpNode> constExpNodes, ArrayList<Token> rightBracks,
                      Token assignToken, InitValNode initValNode) {
        this.ident = ident;
        this.leftBracks = leftBracks;
        this.constExpNodes = constExpNodes;
        this.rightBracks = rightBracks;
        this.assignToken = assignToken;
        this.initValNode = initValNode;
    }

    public ArrayList<ConstExpNode> getConstExpNodes() {
        return constExpNodes;
    }

    public Token getIdent() {
        return ident;
    }

    public InitValNode getInitValNode() {
        return initValNode;
    }

    public void print() throws IOException {
        InOututils.write(ident.toString(),"output.txt");
        if (constExpNodes != null && constExpNodes.size() != 0) {
            int i;
            for (i =0; i < constExpNodes.size(); i++) {
                InOututils.write(leftBracks.get(i).toString(),"output.txt");
                constExpNodes.get(i).print();
                InOututils.write(rightBracks.get(i).toString(),"output.txt");
            }
        }
        if(assignToken != null) {
            InOututils.write(assignToken.toString(),"output.txt");
            initValNode.print();
        }
        InOututils.write("<VarDef>","output.txt");
    }
}
