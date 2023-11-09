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

    public void print() throws IOException {
        funcFParamNodes.get(0).print();
        if (commas != null && commas.size() != 0) {
            int i;
            for (i = 0; i < commas.size(); i++) {
                InOututils.write(commas.get(i).toString(),"output.txt");
                funcFParamNodes.get(i + 1).print();
            }
        }
        InOututils.write("<FuncFParams>","output.txt");
    }
}
