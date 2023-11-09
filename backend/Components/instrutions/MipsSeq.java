package backend.Components.instrutions;

import backend.Operands.Register;

public class MipsSeq extends MipsInstruction{
    private Register rd;
    private Register rs;
    private Register rt;
    public MipsSeq(Register rd,Register rs,Register rt) {
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
    }

    @Override
    public String toString() {
        return "seq "+rd.toString()+", "+rs.toString()+", "+rt.toString();
    }
}