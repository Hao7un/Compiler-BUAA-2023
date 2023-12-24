package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;
import java.util.ArrayList;

public class VarDeclNode {
    //  VarDecl → BType VarDef { ',' VarDef } ';'
    private BTypeNode bTypeNode;
    private ArrayList<VarDefNode> varDefNodes;
    private ArrayList<Token> commas;
    private Token semicnToken;

    public VarDeclNode(BTypeNode bTypeNode, ArrayList<VarDefNode> varDefNodes, ArrayList<Token> commas, Token semicnToken) {
        this.bTypeNode = bTypeNode;
        this.varDefNodes = varDefNodes;
        this.commas = commas;
        this.semicnToken = semicnToken;
    }

    public ArrayList<VarDefNode> getVarDefNodes() {
        return varDefNodes;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(bTypeNode.toString());
        sb.append(varDefNodes.get(0).toString());
        if (commas != null && !commas.isEmpty()) {
            int i;
            for (i = 0; i < commas.size(); i++) {
                sb.append(commas.get(i).toString());
                sb.append(varDefNodes.get(i+1).toString());
            }
        }
        sb.append(semicnToken.toString());
        sb.append("<VarDecl>\n");
        return sb.toString();
    }
}
