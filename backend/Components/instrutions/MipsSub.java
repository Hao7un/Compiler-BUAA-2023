package backend.Components.instrutions;

import backend.Operands.Register;

public class MipsSub extends MipsInstruction {

    private Register rs;
    private Register rt;
    private Register rd;
    public MipsSub(Register rd,Register rs, Register rt) {
        this.rs = rs;
        this.rt = rt;
        this.rd = rd;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("sub");
        sb.append(" ");
        sb.append(rd.toString());
        sb.append(", ");
        sb.append(rs.toString());
        sb.append(", ");
        sb.append(rt.toString());
        return sb.toString();
    }
}
