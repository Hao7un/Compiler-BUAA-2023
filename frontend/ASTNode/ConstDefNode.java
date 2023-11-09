package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;
import java.util.ArrayList;

public class ConstDefNode {
    //ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal

    private Token Ident;
    private ArrayList<ConstExpNode> constExpNodes;
    private ArrayList<Token> leftBracks;
    private ArrayList<Token> rightBracks;
    private Token ASSIGN;
    private ConstInitValNode constInitValNode;

    public ConstDefNode(Token Ident, ArrayList<ConstExpNode> constExpNodes, ArrayList<Token> leftBracks, ArrayList<Token> rightBracks, Token ASSIGN, ConstInitValNode constInitValNode) {
        this.Ident = Ident;
        this.constExpNodes = constExpNodes;
        this.leftBracks = leftBracks;
        this.rightBracks = rightBracks;
        this.ASSIGN = ASSIGN;
        this.constInitValNode = constInitValNode;
    }

    public ArrayList<ConstExpNode> getConstExpNodes() {
        return constExpNodes;
    }

    public ConstInitValNode getConstInitValNode() {
        return constInitValNode;
    }

    public Token getIdent() {
        return Ident;
    }

    public void print() throws IOException {
        InOututils.write(Ident.toString(),"output.txt");
        int i = 0;
        for (ConstExpNode constExpNode : constExpNodes) {
            InOututils.write(leftBracks.get(i).toString(),"output.txt");
            constExpNode.print();
            InOututils.write(rightBracks.get(i).toString(),"output.txt");
            i++;
        }
        InOututils.write(ASSIGN.toString(),"output.txt");
        constInitValNode.print();
        InOututils.write("<ConstDef>","output.txt");
    }
}
