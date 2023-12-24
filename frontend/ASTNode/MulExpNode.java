package frontend.ASTNode;

import frontend.Symbol.SymbolTable;
import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;

public class MulExpNode {
    //  MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    //  transform into MulExp → UnaryExp | UnaryExp ('*' | '/' | '%')  MulExp
    private UnaryExpNode unaryExpNode;
    private Token operator;
    private MulExpNode mulExpNode;

    public MulExpNode(UnaryExpNode unaryExpNode) {
        this.unaryExpNode = unaryExpNode;
    }

    public MulExpNode(UnaryExpNode unaryExpNode, Token operator, MulExpNode mulExpNode) {
        this.unaryExpNode = unaryExpNode;
        this.operator = operator;
        this.mulExpNode = mulExpNode;
    }

    public int getDimension(SymbolTable currentSymbolTable) {
        return unaryExpNode.getDimension(currentSymbolTable);
    }

    public Token getOperator() {
        return operator;
    }

    public UnaryExpNode getUnaryExpNode() {
        return unaryExpNode;
    }

    public MulExpNode getMulExpNode() {
        return mulExpNode;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(unaryExpNode.toString());
        sb.append("<MulExp>\n");
        if (operator!=null) {
           sb.append(operator.toString());
           sb.append(mulExpNode.toString());
        }
        return sb.toString();
    }

}
