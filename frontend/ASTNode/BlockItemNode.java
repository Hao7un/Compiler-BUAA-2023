package frontend.ASTNode;

import frontend.tokens.Token;

import java.io.IOException;
import java.util.ArrayList;

public class BlockItemNode {
    //BlockItem → Decl | Stmt
    private DeclNode declNode;
    private StmtNode stmtNode;

    public BlockItemNode(DeclNode declNode) {
        this.declNode = declNode;
    }
    public BlockItemNode(StmtNode stmtNode) {
        this.stmtNode = stmtNode;
    }

    public ArrayList<Token> getReturnTokens() {
        if (stmtNode == null) {
            return new ArrayList<>();
        } else {
            return stmtNode.getReturnTokens();
        }
    }

    public DeclNode getDeclNode() {
        return declNode;
    }

    public StmtNode getStmtNode() {
        return stmtNode;
    }

    public void print() throws IOException {
        if (declNode != null) {
            declNode.print();
        } else {
            stmtNode.print();
        }
    }
}
