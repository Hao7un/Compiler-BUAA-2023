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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CONSTTK.toString());
        sb.append(bTypeNode.toString());
        int i;
        sb.append(constDefNodes.get(0).toString());
        for (i = 1; i < constDefNodes.size(); i++) {
            sb.append(commas.get(i - 1).toString());
            sb.append(constDefNodes.get(i).toString());
        }
        sb.append(SEMICN.toString());
        sb.append("<ConstDecl>\n");
        return sb.toString();
    }
}
