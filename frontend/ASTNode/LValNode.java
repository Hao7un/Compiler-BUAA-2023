package frontend.ASTNode;

import frontend.Symbol.SymbolTable;
import frontend.Symbol.VarSymbol;
import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;
import java.util.ArrayList;

public class LValNode {
    //  LVal → Ident {'[' Exp ']'}
    private Token ident;
    private ArrayList<Token> leftBracks;
    private ArrayList<Token> rightBracks;
    private ArrayList<ExpNode> expNodes;

    public LValNode(Token ident, ArrayList<Token> leftBracks, ArrayList<Token> rightBracks,
                    ArrayList<ExpNode> expNodes) {
        this.ident = ident;
        this.leftBracks = leftBracks;
        this.rightBracks = rightBracks;
        this.expNodes = expNodes;
    }

    public Token getIdent() {
        return ident;
    }

    public int getDimension(SymbolTable currentSymbolTable) {
        int dimension1 = leftBracks.size(); /*调用函数时的维度*/
        String varName = ident.getValue();
        VarSymbol varSymbol = (VarSymbol) currentSymbolTable.getVarSymbol(varName);
        if (varSymbol == null) {
            /*没找到这个symbol，别的错误*/
            return 0;
        }
        int dimension2 = varSymbol.getDimension();

        return dimension2 - dimension1;
    }

    public ArrayList<ExpNode> getExpNodes() {
        return expNodes;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ident.toString());
        if (expNodes != null && !expNodes.isEmpty()) {
            int i = 0;
            for (i = 0; i < expNodes.size(); i++)  {
                sb.append(leftBracks.get(i).toString());
                sb.append(expNodes.get(i).toString());
                sb.append(rightBracks.get(i).toString());
            }
        }
        sb.append("<LVal>\n");
        return sb.toString();
    }
}
