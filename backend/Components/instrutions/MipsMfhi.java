package backend.Components.instrutions;

import backend.Operands.Register;

public class MipsMfhi extends MipsInstruction{
    private Register rd;
    public MipsMfhi(Register rd) {
        this.rd = rd;
    }

    @Override
    public String toString() {
        return "mfhi " +rd.toString();
    }
}

