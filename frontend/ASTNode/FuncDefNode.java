package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;

public class FuncDefNode {
    //  FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    private FuncTypeNode funcTypeNode;
    private Token ident;
    private Token leftParent;
    private FuncFParamsNode funcFParamsNode;
    private Token rightParent;
    private BlockNode blockNode;

    public FuncDefNode(FuncTypeNode funcTypeNode, Token ident, Token leftParent, FuncFParamsNode funcFParamsNode,
                       Token rightParent, BlockNode blockNode) {
        this.funcTypeNode = funcTypeNode;
        this.ident = ident;
        this.leftParent = leftParent;
        this.funcFParamsNode = funcFParamsNode;
        this.rightParent = rightParent;
        this.blockNode = blockNode;
    }

    public BlockNode getBlockNode() {
        return blockNode;
    }

    public Token getIdent() {
        return ident;
    }

    public FuncFParamsNode getFuncFParamsNode() {
        return funcFParamsNode;
    }

    public FuncTypeNode getFuncTypeNode() {
        return funcTypeNode;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(funcTypeNode.toString());
        sb.append(ident.toString());
        sb.append(leftParent.toString());
        if(funcFParamsNode != null) {
            sb.append(funcFParamsNode.toString());
        }
        sb.append(rightParent.toString());
        sb.append(blockNode.toString());
        sb.append("<FuncDef>\n");

        return sb.toString();
    }
}
