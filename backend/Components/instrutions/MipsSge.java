package backend.Components.instrutions;

import backend.Operands.Register;

public class MipsSge extends MipsInstruction{
    private Register rd;
    private Register rs;
    private Register rt;
    public MipsSge(Register rd,Register rs,Register rt) {
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
    }

    @Override
    public String toString() {
        /*
        sge $d, $s, $t
        优化为：
        slt $d, $s, $t  // 如果 $s < $t，则 $d = 1
        xori $d, $d, 1  // 取反，如果 $s >= $t，则 $d = 1
         */
        //return "sge "+rd.toString()+", "+rs.toString()+", "+rt.toString();
        String slt =  "slt "+rd.toString()+", "+rs.toString()+", "+rt.toString();
        String xori = "xori " + rd.toString()+", "+rd.toString()+", "+"1";

        return slt + "\n" + xori;
    }
}