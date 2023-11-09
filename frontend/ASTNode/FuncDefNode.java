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

    public void print() throws IOException {
        funcTypeNode.print();
        InOututils.write(ident.toString(),"output.txt");
        InOututils.write(leftParent.toString(),"output.txt");
        if(funcFParamsNode != null) {
            funcFParamsNode.print();
        }
        InOututils.write(rightParent.toString(),"output.txt");
        blockNode.print();
        InOututils.write("<FuncDef>","output.txt");
    }
}
