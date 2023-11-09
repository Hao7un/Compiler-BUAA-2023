package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;
import java.util.ArrayList;

public class ConstInitValNode {
    // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    private ConstExpNode constExpNode;
    private Token leftBrace;
    private ArrayList<ConstInitValNode> constInitValNodes;
    private ArrayList<Token> commas;
    private Token rightBrace;

    public ConstInitValNode(ConstExpNode constExpNode) {
        this.constExpNode = constExpNode;
    }

    public ConstInitValNode(Token leftBrace, ArrayList<ConstInitValNode> constInitValNodes,
                            ArrayList<Token> commas, Token rightBrace) {
        this.leftBrace = leftBrace;
        this.constInitValNodes = constInitValNodes;
        this.commas = commas;
        this.rightBrace =rightBrace;
    }

    public ConstExpNode getConstExpNode() {
        return constExpNode;
    }

    public ArrayList<ConstInitValNode> getConstInitValNodes() {
        return constInitValNodes;
    }

    public void print() throws IOException {
        if(constExpNode != null) {
            constExpNode.print();
        } else {
            InOututils.write(leftBrace.toString(),"output.txt");
            if(constInitValNodes != null && constInitValNodes.size() != 0) {
                constInitValNodes.get(0).print();
                int i;
                for (i = 1; i < constInitValNodes.size(); i++) {
                    InOututils.write(commas.get(i - 1).toString(),"output.txt");
                    constInitValNodes.get(i).print();
                }
            }
            InOututils.write(rightBrace.toString(),"output.txt");
        }
        InOututils.write("<ConstInitVal>","output.txt");
    }
}
