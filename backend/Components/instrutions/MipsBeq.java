package backend.Components.instrutions;

import backend.Operands.Label;
import backend.Operands.Register;

import javax.swing.text.LabelView;

public class MipsBeq extends MipsInstruction{
    private Register rs;
    private Register rt;
    private Label label;

    public MipsBeq(Register rs, Register rt, Label label) {
        this.rs = rs;
        this.rt = rt;
        this.label = label;
    }

    @Override
    public String toString() {
        return "beq " + rs.toString()+", " + rt.toString()+", "+label.toString();
    }
}
