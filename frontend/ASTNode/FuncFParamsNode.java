package frontend.ASTNode;

import frontend.tokens.Token;
import utils.InOututils;

import java.io.IOException;
import java.util.ArrayList;

public class FuncFParamsNode {
    // FuncFParams → FuncFParam { ',' FuncFParam }
    private ArrayList<FuncFParamNode> funcFParamNodes;
    private ArrayList<Token> commas;
    public FuncFParamsNode(ArrayList<FuncFParamNode> funcFParamNodes, ArrayList<Token> commas) {
        this.funcFParamNodes = funcFParamNodes;
        this.commas = commas;
    }

    public ArrayList<FuncFParamNode> getFuncFParams() {
        return funcFParamNodes;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(funcFParamNodes.get(0).toString());
        if (commas != null && !commas.isEmpty()) {
            int i;
            for (i = 0; i < commas.size(); i++) {
                sb.append(commas.get(i).toString());
                sb.append(funcFParamNodes.get(i + 1).toString());
            }
        }
        sb.append("<FuncFParams>\n");

        return sb.toString();
    }
}
