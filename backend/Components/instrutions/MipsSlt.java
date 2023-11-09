package backend.Components.instrutions;

import backend.Operands.Register;


public class MipsSlt extends MipsInstruction{
    private Register rd;
    private Register rs;
    private Register rt;
    public MipsSlt(Register rd,Register rs,Register rt) {
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
    }

    @Override
    public String toString() {
        return "slt "+rd.toString()+", "+rs.toString()+", "+rt.toString();
    }
}
