package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;
import java.util.ArrayList;

public class ConstDeclNode {
    //  ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    private Token CONSTTK;
    private BTypeNode bTypeNode;

    private ArrayList<ConstDefNode> constDefNodes;
    private ArrayList<Token> commas;
    private Token SEMICN;
    public ConstDeclNode(Token CONSTTK, BTypeNode bTypeNode, ArrayList<ConstDefNode> constDefNodes, ArrayList<Token> commas, Token SEMICN) {
        this.CONSTTK = CONSTTK;
        this.bTypeNode = bTypeNode;
        this.constDefNodes = constDefNodes;
        this.commas = commas;
        this.SEMICN = SEMICN;
    }

    public ArrayList<ConstDefNode> getConstDefNodes() {
        return constDefNodes;
    }

    public void print() throws IOException {
        InOututils.write(CONSTTK.toString(),"output.txt");
        bTypeNode.print();
        int i;
        constDefNodes.get(0).print();
        for (i = 1; i < constDefNodes.size(); i++) {
            InOututils.write(commas.get(i - 1).toString(),"output.txt");
            constDefNodes.get(i).print();
        }
        InOututils.write(SEMICN.toString(),"output.txt");
        InOututils.write("<ConstDecl>","output.txt");
    }
}
