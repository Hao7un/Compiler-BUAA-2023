package IR.values.Const;

import IR.types.FunctionType;
import IR.types.ValueType;
import IR.values.Argument;
import IR.values.BasicBlock;
import IR.values.Value;

import java.util.ArrayList;

public class Function extends Const{

    private Boolean isLibFunction;
    private ArrayList<Argument> arguments = new ArrayList<>();
    private ArrayList<BasicBlock> basicBlocks; //该函数包含的basic blocks

    public Function(String name, FunctionType functionType, Boolean isLibFunction) {
        super(name,functionType);
        this.isLibFunction = isLibFunction;
        this.basicBlocks = new ArrayList<>();

        int cnt = 0;
        for(ValueType type: functionType.getParametersType()) {
            arguments.add(new Argument("%_" + cnt,type));
            cnt ++;
        }
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
