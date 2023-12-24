package backend.Components.instrutions;

import backend.Operands.Imm;
import backend.Operands.Register;

public class MipsMulu extends MipsInstruction {

    private Register rs;
    private Register rt;
    private Register rd;
    private Imm imm;
    public MipsMulu(Register rd, Register rs, Register rt) {
        this.rs = rs;
        this.rt = rt;
        this.rd = rd;
    }

    public MipsMulu(Register rd, Register rs, Imm imm) {
        this.rs = rs;
        this.imm = imm;
        this.rd = rd;
    }

    public Imm getImm() {
        return imm;
    }

    public Register getRd() {
        return rd;
    }

    public Register getRs() {
        return rs;
    }

    public Register getRt() {
        return rt;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("mulu");
        sb.append(" ");
        sb.append(rd.toString());
        sb.append(", ");
        sb.append(rs.toString());
        sb.append(", ");
        if(rt != null) {
            sb.append(rt.toString());
        } else {
            sb.append(imm.toString());
        }
        return sb.toString();
    }
}
