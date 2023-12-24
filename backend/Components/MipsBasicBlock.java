package backend.Components;

import backend.Components.instrutions.MipsInstruction;

import java.util.ArrayList;

public class MipsBasicBlock {
    private String name;
    private ArrayList<MipsInstruction> instructions;

    public MipsBasicBlock(String name) {
        this.name = name;
        this.instructions = new ArrayList<>();
    }

    public ArrayList<MipsInstruction> getInstructions() {
        return instructions;
    }

    public void addInstruction(MipsInstruction mipsInstruction) {
        this.instructions.add(mipsInstruction);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.name);
        sb.append(":\n");

        for (MipsInstruction mipsInstruction : instructions) {
            sb.append("\t");
            sb.append(mipsInstruction.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
