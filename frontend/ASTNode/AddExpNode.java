package frontend.ASTNode;

import frontend.Symbol.SymbolTable;
import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;

public class AddExpNode {
    //AddExp → MulExp | AddExp ('+' | '−') MulExp
    // transform to AddExp → MulExp | MulExp ('+' | '−') AddExp
    private MulExpNode mulExpNode;
    private  Token operator;
    private AddExpNode addExpNode;

    public AddExpNode(MulExpNode mulExpNode) {
        this.mulExpNode = mulExpNode;
    }

    public int getDimension(SymbolTable currentSymbolTable) {
        return mulExpNode.getDimension(currentSymbolTable);
    }

    public AddExpNode(MulExpNode mulExpNode, Token operator, AddExpNode addExpNode) {
        this.mulExpNode = mulExpNode;
        this.operator = operator;
        this.addExpNode = addExpNode;
    }

    public AddExpNode getAddExpNode() {
        return addExpNode;
    }

    public Token getOperator() {
        return operator;
    }

    public MulExpNode getMulExpNode() {
        return mulExpNode;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(mulExpNode.toString());
        sb.append("<AddExp>\n");
        if (operator != null) {
            sb.append(operator.toString());
            sb.append(addExpNode.toString());
        }
        return sb.toString();
    }
}
