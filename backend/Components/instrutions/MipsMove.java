package backend.Components.instrutions;

import backend.Operands.Register;

public class MipsMove extends MipsInstruction {
    private Register rs;
    private Register rt;

    public MipsMove(Register rt , Register rs) {
        this.rs = rs;
        this.rt = rt;
    }

    public Register getRs() {
        return rs;
    }

    public Register getRt() {
        return rt;
    }
    @Override
    public String toString() {
        return "move "+rt.toString() + ", " + rs.toString();
    }
}
