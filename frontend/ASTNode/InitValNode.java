package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;
import java.util.ArrayList;

public class InitValNode {
    // InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'

    private ExpNode expNode;
    private Token leftBrace;
    private ArrayList<InitValNode> initValNodes;
    private ArrayList<Token> commas;
    private Token rightBrace;

    public InitValNode(ExpNode expNode) {
        this.expNode = expNode;
    }
    public InitValNode(Token leftBrace, ArrayList<InitValNode> initValNodes,
                       ArrayList<Token> commas, Token rightBrace) {
        this.leftBrace = leftBrace;
        this.initValNodes = initValNodes;
        this.commas = commas;
        this.rightBrace =rightBrace;
    }

    public ArrayList<InitValNode> getInitValNodes() {
        return initValNodes;
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(expNode != null) {
            sb.append(expNode.toString());
        } else {
            sb.append(leftBrace.toString());
            sb.append(initValNodes.get(0));
            if(commas != null && !commas.isEmpty()) {
                int i;
                for (i = 0;i < commas.size();i++) {
                    sb.append(commas.get(i).toString());
                    sb.append(initValNodes.get(i + 1).toString());
                }
            }
            sb.append(rightBrace.toString());
        }
        sb.append("<InitVal>\n");

        return sb.toString();
    }
}
