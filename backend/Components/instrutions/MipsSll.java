package backend.Components.instrutions;

import backend.Operands.Imm;
import backend.Operands.Register;

public class MipsSll extends MipsInstruction {

    private Register rs;
    private Register rt;
    private Imm imm;
    public MipsSll(Register rs, Register rt , Imm imm) {
        this.rs = rs;
        this.rt = rt;
        this.imm = imm;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("sll");
        sb.append(" ");
        sb.append(rt.toString());
        sb.append(", ");
        sb.append(rs.toString());
        sb.append(", ");
        sb.append(imm.toString());
        return sb.toString();
    }
}