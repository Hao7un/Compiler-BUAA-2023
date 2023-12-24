package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;
import java.util.ArrayList;

public class VarDefNode {
    //VarDef → Ident { '[' ConstExp ']' } | Ident { '[' ConstExp ']' } '=' InitVal
    private Token ident;
    private ArrayList<Token> leftBracks;
    private ArrayList<ConstExpNode> constExpNodes;
    private ArrayList<Token> rightBracks;

    private Token assignToken;
    private InitValNode initValNode;

    public VarDefNode(Token ident, ArrayList<Token> leftBracks, ArrayList<ConstExpNode> constExpNodes, ArrayList<Token> rightBracks ) {
        this.ident = ident;
        this.leftBracks = leftBracks;
        this.constExpNodes = constExpNodes;
        this.rightBracks = rightBracks;
    }
    public VarDefNode(Token ident, ArrayList<Token> leftBracks, ArrayList<ConstExpNode> constExpNodes, ArrayList<Token> rightBracks,
                      Token assignToken, InitValNode initValNode) {
        this.ident = ident;
        this.leftBracks = leftBracks;
        this.constExpNodes = constExpNodes;
        this.rightBracks = rightBracks;
        this.assignToken = assignToken;
        this.initValNode = initValNode;
    }

    public ArrayList<ConstExpNode> getConstExpNodes() {
        return constExpNodes;
    }

    public Token getIdent() {
        return ident;
    }

    public InitValNode getInitValNode() {
        return initValNode;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ident.toString());
        if (constExpNodes != null && !constExpNodes.isEmpty()) {
            int i;
            for (i =0; i < constExpNodes.size(); i++) {
                sb.append(leftBracks.get(i).toString());
                sb.append(constExpNodes.get(i).toString());
                sb.append(rightBracks.get(i).toString());
            }
        }
        if(assignToken != null) {
            sb.append(assignToken.toString());
            sb.append(initValNode.toString());
        }
        sb.append("<VarDef>\n");
        return sb.toString();
    }
}
