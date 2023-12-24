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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lValNode.toString());
        sb.append(assignToken.toString());
        sb.append(expNode.toString());
        sb.append("<ForStmt>\n");

        return sb.toString();
    }
}
