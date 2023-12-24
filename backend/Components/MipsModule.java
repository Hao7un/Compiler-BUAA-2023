package backend.Components;


import backend.Components.instrutions.MipsJ;
import backend.Operands.Label;

import java.util.ArrayList;

public class MipsModule {
    /*单例模式*/
    private static final MipsModule mipsModule = new MipsModule();

    private ArrayList<MipsGlobalVariable> globalVariables;
    private ArrayList<MipsFunction> functions;
    public MipsModule() {
        this.globalVariables = new ArrayList<>();
        this.functions = new ArrayList<>();
    }

    public ArrayList<MipsFunction> getFunctions() {
        return functions;
    }

    public static MipsModule getInstance() {
        return mipsModule;
    }

    public void addFunction(MipsFunction mipsFunction) {
        this.functions.add(mipsFunction);
    }
    public void addGlobalVariable(MipsGlobalVariable mipsGlobalVariable) {
        this.globalVariables.add(mipsGlobalVariable);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        /*Global Variable*/
        sb.append(".data\n");
        for (MipsGlobalVariable mipsGlobalVariable : globalVariables) {
            sb.append(mipsGlobalVariable.toString());
            sb.append("\n");
        }

        sb.append(".text\n");
        /*Jump To Main Function*/
        sb.append("j main\n\n");

        /*Function*/
        for (MipsFunction mipsFunction : functions) {
            sb.append(mipsFunction.toString());
            sb.append("\n");
        }


        return sb.toString();
    }
}
