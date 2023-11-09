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

    public void print() throws IOException {
        expNodes.get(0).print();
        if (commas != null) {
            int i;
            for (i = 0; i < commas.size(); i++) {
                InOututils.write(commas.get(i).toString(),"output.txt");
                expNodes.get(i + 1).print();
            }
        }
        InOututils.write("<FuncRParams>","output.txt");
    }
}
