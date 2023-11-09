package IR.values;

import IR.types.LabelType;
import IR.values.Const.Function;
import IR.values.Instructions.IRInstruction;

import java.util.ArrayList;

public class BasicBlock extends Value{
    private ArrayList<IRInstruction> IRInstructions;
    private Function parentFunction; // Basic Block所处的函数

    public BasicBlock(String valueName, Function parentFunction) {
        super(valueName, new LabelType());
        this.parentFunction = parentFunction;
        this.IRInstructions = new ArrayList<>();
    }

    public void addInstruction(IRInstruction IRInstruction) {
        this.IRInstructions.add(IRInstruction);
    }

    public ArrayList<IRInstruction> getInstructions() {
        return IRInstructions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getValueName()).append(":\n");
        for (IRInstruction IRInstruction : IRInstructions) {
            sb.append("\t");
            sb.append(IRInstruction.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}


