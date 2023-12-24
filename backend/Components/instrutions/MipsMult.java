package backend.Components.instrutions;

import backend.Operands.Imm;
import backend.Operands.Register;

public class MipsMult extends MipsInstruction {

    private Register rs;
    private Register rt;

    public MipsMult(Register rs, Register rt) {
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

        StringBuilder sb = new StringBuilder();
        sb.append("mult");
        sb.append(" ");
        sb.append(rs.toString());
        sb.append(", ");
        sb.append(rt.toString());
        return sb.toString();
    }
}
