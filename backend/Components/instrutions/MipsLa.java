package backend.Components.instrutions;

import backend.Operands.Label;
import backend.Operands.Register;

public class MipsLa extends MipsInstruction{
    private Register rs;

    private String name;

    public MipsLa(Register rs,String name) {
        this.rs = rs;
        this.name = name;
    }

    @Override
    public String toString() {
        return "la "+rs.toString()+", " + name;
    }
}
