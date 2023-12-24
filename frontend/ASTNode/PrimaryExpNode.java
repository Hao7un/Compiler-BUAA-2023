package frontend.ASTNode;

import frontend.Symbol.SymbolTable;
import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;

public class PrimaryExpNode {
    // PrimaryExp → '(' Exp ')' | LVal | Number
    private Token leftParent;
    private ExpNode expNode;
    private Token rightParent;
    private LValNode lValNode;
    private NumberNode numberNode;

    public PrimaryExpNode(Token leftParent, ExpNode expNode, Token rightParent) {
        this.leftParent = leftParent;
        this.expNode = expNode;
        this.rightParent = rightParent;
    }
    public PrimaryExpNode(LValNode lValNode) {
        this.lValNode = lValNode;
    }
    public PrimaryExpNode(NumberNode numberNode) {
        this.numberNode = numberNode;
    }

    public int getDimension(SymbolTable currentSymbolTable) {
        if (numberNode != null) {
            return 0;
        } else if(lValNode != null) {
            return lValNode.getDimension(currentSymbolTable);
        } else {
            /*(Exp)的维度只能为0*/
            return 0;
        }
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    public NumberNode getNumberNode() {
        return numberNode;
    }

    public LValNode getlValNode() {
        return lValNode;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (expNode != null) {
            sb.append(leftParent.toString());
            sb.append(expNode.toString());
            sb.append(rightParent.toString());
        } else if (lValNode != null) {
            sb.append(lValNode.toString());
        } else {
            sb.append(numberNode.toString());
        }
        sb.append("<PrimaryExp>\n");
        return sb.toString();
    }
}
