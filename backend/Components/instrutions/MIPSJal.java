package backend.Components.instrutions;

import backend.Operands.Label;

public class MIPSJal extends MipsInstruction{
    private Label targetLabel;
    public MIPSJal(Label targetLabel) {
        this.targetLabel = targetLabel;
    }

    @Override
    public String toString() {
        return "jal "+targetLabel.toString();
    }
}
