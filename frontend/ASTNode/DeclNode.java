package frontend.ASTNode;

import java.io.IOException;

public class DeclNode {
    //Decl → ConstDecl | VarDecl
    private ConstDeclNode constDeclNode;
    private VarDeclNode varDeclNode;
    public DeclNode(ConstDeclNode constDeclNode) {
        this.constDeclNode = constDeclNode;
    }
    public DeclNode(VarDeclNode varDeclNode) {
        this.varDeclNode = varDeclNode;
    }

    public ConstDeclNode getConstDeclNode() {
        return constDeclNode;
    }

    public VarDeclNode getVarDeclNode() {
        return varDeclNode;
    }

    public void print() throws IOException {
        if (constDeclNode != null) {
            constDeclNode.print();
        } else {
            varDeclNode.print();
        }
    }
}
