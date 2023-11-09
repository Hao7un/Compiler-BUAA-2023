package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;

public class ForStmtNode {
    // ForStmt → LVal '=' Exp
    private LValNode lValNode;
    private Token assignToken;
    private ExpNode expNode;

    public ForStmtNode(LValNode lValNode, Token assignToken, ExpNode expNode) {
        this.lValNode = lValNode;
        this.assignToken = assignToken;
        this.expNode = expNode;
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    public LValNode getlValNode() {
        return lValNode;
    }

    public Token getAssignToken() {
        return assignToken;
    }

    public void print() throws IOException {
        lValNode.print();
        InOututils.write(assignToken.toString(),"output.txt");
        expNode.print();
        InOututils.write("<ForStmt>","output.txt");
    }
}
