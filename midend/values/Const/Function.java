package midend.values.Const;

import backend.Operands.Register;
import midend.types.FunctionType;
import midend.types.ValueType;
import midend.values.Argument;
import midend.values.BasicBlock;
import midend.values.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Function extends Const{

    private Boolean isLibFunction;
    private ArrayList<Argument> arguments = new ArrayList<>();
    private ArrayList<BasicBlock> basicBlocks; //该函数包含的basic blocks

    private ArrayList<Function> calledByFunction; // 自己被哪些函数调用
    private ArrayList<Function> callingFunction;  //每个函数维护自己调用过的函数

    private HashMap<Value, Register> value2Reg;

    public Function(String name, FunctionType functionType, Boolean isLibFunction) {
        super(name,functionType);
        this.isLibFunction = isLibFunction;
        this.basicBlocks = new ArrayList<>();
        this.calledByFunction = new ArrayList<>();
        this.callingFunction = new ArrayList<>();
        int cnt = 0;
        for(ValueType type: functionType.getParametersType()) {
            arguments.add(new Argument("%_" + cnt,type));
            cnt ++;
        }
        this.value2Reg = new HashMap<>();
    }

    public void setValue2Reg(HashMap<Value,Register> value2Reg) {
        this.value2Reg = value2Reg;
    }

    public HashMap<Value, Register> getValue2Reg() {
        return value2Reg;
    }

    public boolean isLibFunction() {
        return this.isLibFunction;
    }

    public ArrayList<Argument> getArguments() {
        return arguments;
    }

    public ArrayList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public void addBasicBlock(BasicBlock basicBlock) {
        this.basicBlocks.add(basicBlock);
    }

    public ArrayList<Function> getCalledByFunction() {
        return calledByFunction;
    }

    public ArrayList<Function> getCallingFunction() {
        return callingFunction;
    }

    public void addCalledByFunction(Function function) {
        this.calledByFunction.add(function);
    }

    public void addCallingFunction(Function function) {
        this.callingFunction.add(function);
    }

    @Override
    public String toString() {
        //define i32 @main() {
        // ...
        //}
        StringBuilder sb = new StringBuilder();
        if (isLibFunction) {
            //Lib Function
            sb.append("declare ");
            sb.append(((FunctionType)this.getValueType()).getReturnType().toString());
            sb.append(" ");
            sb.append("@"+this.getValueName());
            sb.append("(");
            for (int i = 0; i < arguments.size(); i++) {
                sb.append(arguments.get(i).getValueType());
                if (i != arguments.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append(")");
        } else{
            // Custom Function
            sb.append("define ");
            sb.append(((FunctionType)this.getValueType()).getReturnType().toString());
            sb.append(" ");
            sb.append("@"+this.getValueName());
            sb.append("(");

            for (int i = 0; i < arguments.size(); i++) {
                sb.append(arguments.get(i).getValueType());
                sb.append(" ");
                if (!isLibFunction) {
                    sb.append(arguments.get(i).getValueName());
                }
                if (i != arguments.size() - 1) {
                    sb.append(", ");
                }
            }
            sb.append(")");
            sb.append("{\n");
            for (BasicBlock basicBlock : basicBlocks) {
                sb.append(basicBlock.toString());
            }
            sb.append("}");
        }
        return sb.toString();
    }
}
