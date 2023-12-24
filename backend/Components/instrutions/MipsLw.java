package backend.Components.instrutions;

import backend.Operands.Imm;
import backend.Operands.Register;

public class MipsLw extends MipsInstruction{

    private Register rs; //base
    private Register rt;
    private Imm offset;

    public MipsLw(Register rs,Register rt,Imm offset) {
        this.rs = rs;
        this.rt = rt;
        this.offset = offset;
    }

    @Override
    public String toString() {
        // lw $1, 10($2)
        return "lw" +" " +rt.toString() + ", " + offset.toString()+"("+rs.toString()+")";
    }
}
