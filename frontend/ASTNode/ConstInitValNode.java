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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(constExpNode != null) {
            sb.append(constExpNode.toString());
        } else {
            sb.append(leftBrace.toString());
            if(constInitValNodes != null && !constInitValNodes.isEmpty()) {
                sb.append(constInitValNodes.get(0).toString());
                int i;
                for (i = 1; i < constInitValNodes.size(); i++) {
                    sb.append(commas.get(i - 1).toString());
                    sb.append(constInitValNodes.get(i).toString());
                }
            }
            sb.append(rightBrace.toString());
        }
        sb.append("<ConstInitVal>\n");

        return sb.toString();
    }
}
