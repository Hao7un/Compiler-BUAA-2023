package backend.Components.instrutions;

import backend.Operands.Register;

public class MipsMflo extends MipsInstruction{
    private Register rd;
    public MipsMflo(Register rd) {
        this.rd = rd;
    }

    @Override
    public String toString() {
        return "mflo " +rd.toString();
    }
}
