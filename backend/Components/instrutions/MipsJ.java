package backend.Components.instrutions;

import backend.Operands.Label;

public class MipsJ extends MipsInstruction{
    private final Label label;
    public MipsJ(Label label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "j "+label.toString();
    }
}
