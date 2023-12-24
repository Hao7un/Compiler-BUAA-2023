package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;
import java.util.ArrayList;

public class FuncRParamsNode {
    // FuncRParams → Exp { ',' Exp }
    private ArrayList<ExpNode> expNodes;
    private ArrayList<Token> commas;

    public FuncRParamsNode(ArrayList<ExpNode> expNodes, ArrayList<Token> commas) {
        this.expNodes = expNodes;
        this.commas = commas;
    }

    public ArrayList<ExpNode> getExps() {
        return expNodes;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(expNodes.get(0).toString());
        if (commas != null) {
            int i;
            for (i = 0; i < commas.size(); i++) {
                sb.append(commas.get(i).toString());
                sb.append(expNodes.get(i + 1).toString());
            }
        }
        sb.append("<FuncRParams>\n");
        return sb.toString();
    }
}
