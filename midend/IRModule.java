package midend;

import midend.values.Const.Function;
import midend.values.Const.GlobalVar;

import java.util.ArrayList;

public class IRModule {

    private static final IRModule irModule = new IRModule(); /*Module单例模式*/

    private ArrayList<Function> libFunctions; /*库函数*/
    private ArrayList<GlobalVar> globalVars; /*全局变量*/
    private ArrayList<Function> functions;   /*函数声明*/

    public IRModule() {
        this.libFunctions = new ArrayList<>();
        this.globalVars = new ArrayList<>();
        this.functions = new ArrayList<>();
    }

    public static IRModule getInstance() {
        return irModule;
    }
    public void addGlobalVar(GlobalVar globalVar) {
        this.globalVars.add(globalVar);
    }
    public ArrayList<GlobalVar> getGlobalVars() {
        return globalVars;
    }
    public void addFunction(Function function) {
        this.functions.add(function);
    }
    public ArrayList<Function> getFunctions() {
        return functions;
    }

    public void addLibFunction(Function libFunction) {
        this.libFunctions.add(libFunction);
    }
    public ArrayList<Function> getLibFunctions() {
        return libFunctions;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Function libFunction : libFunctions) {
            sb.append(libFunction.toString());
            sb.append("\n");
        }

        for (GlobalVar globalVar : globalVars) {
            sb.append(globalVar.toString());
            sb.append("\n");
        }
        for (Function function : functions) {
            sb.append(function.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
