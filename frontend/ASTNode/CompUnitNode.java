package frontend.ASTNode;

import utils.InOututils;

import java.io.IOException;
import java.util.ArrayList;

public class CompUnitNode {
    // CompUnit -> {Decl} {FuncDef} MainFuncDef
    private ArrayList<DeclNode> declNodes;
    private ArrayList<FuncDefNode> funcDefNodes;
    private MainFuncDefNode mainFuncDefNode;

    public CompUnitNode(ArrayList<DeclNode> declNodes, ArrayList<FuncDefNode> funcDefNodes, MainFuncDefNode mainFuncDefNode) {
        this.declNodes = declNodes;
        this.funcDefNodes = funcDefNodes;
        this.mainFuncDefNode = mainFuncDefNode;
    }

    public ArrayList<DeclNode> getDeclNodes() {
        return declNodes;
    }

    public ArrayList<FuncDefNode> getFuncDefNodes() {
        return funcDefNodes;
    }

    public MainFuncDefNode getMainFuncDefNode() {
        return mainFuncDefNode;
    }

    public String toString() {
        StringBuilder sb =  new StringBuilder();
        for (DeclNode declNode : declNodes) {
            sb.append(declNode.toString());
        }
        for (FuncDefNode funcDefNode : funcDefNodes) {
            sb.append(funcDefNode.toString());
        }
        sb.append(mainFuncDefNode.toString());
        sb.append("<CompUnit>\n");
        return sb.toString();
    }
}
