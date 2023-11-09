package backend.Components.instrutions;

import backend.Operands.Imm;
import backend.Operands.Register;

public class MipsMul extends MipsInstruction {

    private Register rs;
    private Register rt;
    private Register rd;
    private Imm imm;
    public MipsMul(Register rd,Register rs, Register rt) {
        this.rs = rs;
        this.rt = rt;
        this.rd = rd;
    }

    public MipsMul(Register rd,Register rs, Imm imm) {
        this.rs = rs;
        this.imm = imm;
        this.rd = rd;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("mul");
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
