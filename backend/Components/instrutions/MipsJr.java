package backend.Components.instrutions;

import backend.Operands.Register;

public class MipsJr extends MipsInstruction{
    Register rs;
    public MipsJr(Register rs) {
        this.rs = rs;
    }

    @Override
    public String toString() {
        return "jr " + rs.toString();
    }
}
