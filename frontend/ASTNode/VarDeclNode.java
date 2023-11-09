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

    public void print() throws IOException {
        bTypeNode.print();
        varDefNodes.get(0).print();
        if (commas != null && commas.size() != 0) {
            int i;
            for (i = 0; i < commas.size(); i++) {
                InOututils.write(commas.get(i).toString(),"output.txt");
                varDefNodes.get(i+1).print();
            }
        }
        InOututils.write(semicnToken.toString(),"output.txt");
        InOututils.write("<VarDecl>","output.txt");
    }
}
