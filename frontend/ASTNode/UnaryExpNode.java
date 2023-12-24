package frontend.ASTNode;

import frontend.Symbol.SymbolTable;
import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;

public class UnaryExpNode {
    //UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp

    /*PrimaryExp*/
    private PrimaryExpNode primaryExpNode;

    /* Ident '(' [FuncRParams] ')' */
    private Token ident;
    private Token leftParent;
    private FuncRParamsNode funcRParamsNode;
    private Token rightParent;

    /*UnaryOp UnaryExp*/
    private UnaryOpNode unaryOpNode;
    private UnaryExpNode unaryExpNode;

    public UnaryExpNode(PrimaryExpNode primaryExpNode) {
        this.primaryExpNode = primaryExpNode;
    }
    public UnaryExpNode(Token ident, Token leftParent, FuncRParamsNode funcRParamsNode, Token rightParent) {
        this.ident = ident;
        this.leftParent  = leftParent;
        this.funcRParamsNode = funcRParamsNode;
        this.rightParent = rightParent;
    }

    public UnaryExpNode(UnaryOpNode unaryOpNode, UnaryExpNode unaryExpNode) {
        this.unaryExpNode = unaryExpNode;
        this.unaryOpNode = unaryOpNode;
    }

    public int getDimension(SymbolTable currentSymbolTable) {
        if (unaryExpNode != null) {
            return unaryExpNode.getDimension(currentSymbolTable);
        } else if (primaryExpNode != null) {
            return primaryExpNode.getDimension(currentSymbolTable);
        } else {
            /*类似于func(func(1,2))，可能返回int 或者 void*/
            return -1;
        }
    }

    public UnaryExpNode getUnaryExpNode() {
        return unaryExpNode;
    }

    public UnaryOpNode getUnaryOpNode() {
        return unaryOpNode;
    }

    public Token getIdent() {
        return ident;
    }

    public FuncRParamsNode getFuncRParamsNode() {
        return funcRParamsNode;
    }

    public PrimaryExpNode getPrimaryExpNode() {
        return primaryExpNode;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (primaryExpNode != null) {
            sb.append(primaryExpNode.toString());
        } else if (ident != null) {
            sb.append(ident.toString());
            sb.append(leftParent.toString());
            if (funcRParamsNode != null) {
                sb.append(funcRParamsNode.toString());
            }
            sb.append(rightParent.toString());
        } else {
            sb.append(unaryOpNode.toString());
            sb.append(unaryExpNode.toString());
        }
        sb.append("<UnaryExp>\n");
        return sb.toString();
    }
}
