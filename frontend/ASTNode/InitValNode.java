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

    public void print() throws IOException {
        if(expNode != null) {
            expNode.print();
        } else {
            InOututils.write(leftBrace.toString(),"output.txt");
            initValNodes.get(0).print();
            if(commas != null && commas.size() != 0) {
                int i;
                for (i = 0;i < commas.size();i++) {
                    InOututils.write(commas.get(i).toString(),"output.txt");
                    initValNodes.get(i + 1).print();
                }
            }
            InOututils.write(rightBrace.toString(),"output.txt");
        }
        InOututils.write("<InitVal>","output.txt");
    }
}
