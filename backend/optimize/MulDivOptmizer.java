package backend.optimize;

import backend.Components.MipsBasicBlock;
import backend.Components.instrutions.*;
import backend.Operands.Imm;
import backend.Operands.Register;
import midend.values.Const.ConstInteger;
import midend.values.Instructions.IRInstruction;
import midend.values.Value;

import java.math.BigInteger;

public class MulDivOptmizer {

    public MulDivOptmizer() {

    }

    private static int shift;
    private static long multiplier;

    private static void chooseMultiplier(BigInteger divisor) {
        shift = 0;
        while (divisor.compareTo(BigInteger.ONE.shiftLeft(shift)) > 0) {
            shift++;
        }
        long low = BigInteger.ONE.shiftLeft(32 + shift).divide(divisor).longValue();
        long high = BigInteger.ONE.shiftLeft(32 + shift).add(BigInteger.ONE.shiftLeft(1 + shift)).divide(divisor).longValue();

        while ( (low >> 1) < (high >> 1) && shift > 0) {
            high >>= 1;
            low >>= 1;
            shift--;
        }
        multiplier = high;
    }

    public static boolean optimizeMod(IRInstruction instr, Register src,Register dst, MipsBasicBlock mipsBasicBlock) {
        Value operand1 = instr.getOperand(0);
        Value operand2 = instr.getOperand(1);
        if (!(operand2 instanceof ConstInteger)) {
            return false;
        }
        int divisor = Integer.parseInt(operand2.getValueName());    // 原本的divisor
        int abs = divisor >= 0 ? divisor : -divisor;                // 取绝对值后

        if (is2Power(abs)) {
            // 可以用移位进行处理
            MipsSra sra = new MipsSra(src,new Register("$k0"),new Imm(31));
            mipsBasicBlock.addInstruction(sra);

            int l = getCLZ(abs);
            MipsSrl srl = new MipsSrl(new Register("$k0"),new Register("$k0"),
                    new Imm(32-l));
            mipsBasicBlock.addInstruction(srl);

            MipsAddu addu = new MipsAddu(new Register("$k1"),src,new Register("$k0"));
            mipsBasicBlock.addInstruction(addu);

            sra = new MipsSra(new Register("$k1"),new Register("$k1"),new Imm(l));
            mipsBasicBlock.addInstruction(sra);

            MipsSll sll =  new MipsSll(new Register("$k1"),new Register("$k1"),new Imm(l));
            mipsBasicBlock.addInstruction(sll);

            MipsSubu subu = new MipsSubu(dst,src,new Register("$k1"));
            mipsBasicBlock.addInstruction(subu);
        } else {
            chooseMultiplier(BigInteger.valueOf(divisor).abs());
            // 将mod转换为乘法
            if (multiplier < Integer.MAX_VALUE) {
                MipsLi li = new MipsLi(new Register("$k0"),new Imm((int)multiplier));
                mipsBasicBlock.addInstruction(li);

                MipsMult mult = new MipsMult(new Register("$k0"),src);
                mipsBasicBlock.addInstruction(mult);

                MipsMfhi mfhi = new MipsMfhi(new Register("$k0"));
                mipsBasicBlock.addInstruction(mfhi);

                MipsSra sra = new MipsSra(new Register("$k0"),new Register("$k1"),new Imm(shift));
                mipsBasicBlock.addInstruction(sra);
            } else {
                MipsLi li = new MipsLi(new Register("$k0"),new Imm((int)(multiplier - (1L << 32))));
                mipsBasicBlock.addInstruction(li);

                MipsMult mult = new MipsMult(new Register("$k0"),src);
                mipsBasicBlock.addInstruction(mult);

                MipsMfhi mfhi = new MipsMfhi(new Register("$k0"));
                mipsBasicBlock.addInstruction(mfhi);

                MipsAddu addu = new MipsAddu(new Register("$k1"),src,new Register("$k0"));
                mipsBasicBlock.addInstruction(addu);

                MipsSra sra = new MipsSra(new Register("$k1"),new Register("$k1"),new Imm(shift));
                mipsBasicBlock.addInstruction(sra);
            }
            MipsSlt slt = new MipsSlt(new Register("$k0"),src,new Register("$k0"));
            mipsBasicBlock.addInstruction(slt);

            MipsAddu addu = new MipsAddu(new Register("$k1"),new Register("$k1"),new Register("$k0"));
            mipsBasicBlock.addInstruction(addu);

            MipsLi li = new MipsLi(new Register("$k0"),new Imm(abs));
            mipsBasicBlock.addInstruction(li);

            MipsMult mult = new MipsMult(new Register("$k0"),new Register("$k1"));
            mipsBasicBlock.addInstruction(mult);

            MipsMflo mflo = new MipsMflo(new Register("$k0"));
            mipsBasicBlock.addInstruction(mflo);

            MipsSubu subu = new MipsSubu(dst,src,new Register("$k0"));
            mipsBasicBlock.addInstruction(subu);
        }
        return true;
    }

    public static boolean optimizeDiv(IRInstruction instr, Register src,Register dst, MipsBasicBlock mipsBasicBlock) {
        Value operand1 = instr.getOperand(0);
        Value operand2 = instr.getOperand(1);
        if (!(operand2 instanceof ConstInteger)) {
            return false;
        }
        int divisor = Integer.parseInt(operand2.getValueName());    // 原本的divisor
        int abs = divisor >= 0 ? divisor : -divisor;                // 取绝对值后

        if (is2Power(abs)) {
            // 可以用移位进行处理
            MipsSra sra = new MipsSra(src,new Register("$k0"),new Imm(31));
            mipsBasicBlock.addInstruction(sra);

            int l = getCLZ(abs);
            MipsSrl srl = new MipsSrl(new Register("$k0"),new Register("$k0"),
                    new Imm(32-l));
             mipsBasicBlock.addInstruction(srl);

             MipsAddu addu = new MipsAddu(new Register("$k1"),src,new Register("$k0"));
             mipsBasicBlock.addInstruction(addu);

             sra = new MipsSra(new Register("$k1"),dst,new Imm(l));
             mipsBasicBlock.addInstruction(sra);
        } else {
            chooseMultiplier(BigInteger.valueOf(divisor).abs());
            // 将除法转换为乘法
            if (multiplier < Integer.MAX_VALUE) {
                MipsLi li = new MipsLi(new Register("$k0"),new Imm((int)multiplier));
                mipsBasicBlock.addInstruction(li);

                MipsMult mult = new MipsMult(new Register("$k0"),src);
                mipsBasicBlock.addInstruction(mult);

                MipsMfhi mfhi = new MipsMfhi(new Register("$k0"));
                mipsBasicBlock.addInstruction(mfhi);

                MipsSra sra = new MipsSra(new Register("$k0"),new Register("$k1"),new Imm(shift));
                mipsBasicBlock.addInstruction(sra);
            } else {
                MipsLi li = new MipsLi(new Register("$k0"),new Imm((int)(multiplier - (1L << 32))));
                mipsBasicBlock.addInstruction(li);

                MipsMult mult = new MipsMult(new Register("$k0"),src);
                mipsBasicBlock.addInstruction(mult);

                MipsMfhi mfhi = new MipsMfhi(new Register("$k0"));
                mipsBasicBlock.addInstruction(mfhi);

                MipsAddu addu = new MipsAddu(new Register("$k1"),src,new Register("$k0"));
                mipsBasicBlock.addInstruction(addu);

                MipsSra sra = new MipsSra(new Register("$k1"),new Register("$k1"),new Imm(shift));
                mipsBasicBlock.addInstruction(sra);
            }
            MipsSlt slt = new MipsSlt(new Register("$k0"),src,new Register("$0"));
            mipsBasicBlock.addInstruction(slt);

            MipsAddu addu = new MipsAddu(dst,new Register("$k1"),new Register("$k0"));
            mipsBasicBlock.addInstruction(addu);
        }

        if (divisor < 0) {
            MipsSubu subu = new MipsSubu(dst,new Register("$0"),dst);
            mipsBasicBlock.addInstruction(subu);
        }

        return true;
    }


    public static boolean optimizeMul(IRInstruction instr, Register src ,Register dst,MipsBasicBlock mipsBasicBlock) {
        boolean hasConst = false;
        Value operand1 = instr.getOperand(0);
        Value operand2 = instr.getOperand(1);

        int number = 0;
        if (operand1 instanceof ConstInteger) {
            hasConst = true;
            number = Integer.parseInt(operand1.getValueName());
        } else if (operand2 instanceof ConstInteger) {
            hasConst = true;
            number = Integer.parseInt(operand2.getValueName());
        }

        if (hasConst) { // 有一个操作数是常数
            // src number 的 寄存器
            // dst 存储结果的寄存器

            int abs = number > 0 ? number : -number;
            boolean isNeg = number < 0;

            if (is2Power(abs)) {
                int power = getCLZ(abs);
                MipsSll sll = new MipsSll(src,dst,new Imm(power));
                mipsBasicBlock.addInstruction(sll);

                if (isNeg) {
                    MipsSubu subu = new MipsSubu(dst,new Register("$0"),dst);
                    mipsBasicBlock.addInstruction(subu);
                }
                return true;
            } else if (is2Power(abs + 1)) {
                int power = getCLZ(abs + 1);
                MipsSll sll = new MipsSll(src,new Register("$a0"),new Imm(power));
                mipsBasicBlock.addInstruction(sll);
                if (isNeg) {
                    MipsSubu subu = new MipsSubu(dst,src,new Register("$a0"));
                    mipsBasicBlock.addInstruction(subu);
                } else {
                    MipsSubu subu = new MipsSubu(dst,new Register("$a0"),src);
                    mipsBasicBlock.addInstruction(subu);
                }
                return true;
            } else if (is2Power(abs - 1)) {
                int power = getCLZ(abs - 1);
                MipsSll sll = new MipsSll(src,new Register("$a0"),new Imm(power));
                mipsBasicBlock.addInstruction(sll);

                MipsAddu addu = new MipsAddu(dst,new Register("$a0"),src);
                mipsBasicBlock.addInstruction(addu);
                if (isNeg) {
                    MipsSubu subu = new MipsSubu(dst,new Register("$0"),dst);
                    mipsBasicBlock.addInstruction(subu);
                }
                return true;
            } else if (!isNeg){
                if (is2Power(abs - 2)) {
                    int power = getCLZ(abs - 2);
                    MipsSll sll = new MipsSll(src,new Register("$a0"),new Imm(power));
                    mipsBasicBlock.addInstruction(sll);

                    MipsAddu addu = new MipsAddu(new Register("$a0"),new Register("$a0"),src);
                    mipsBasicBlock.addInstruction(addu);

                    addu = new MipsAddu(dst,new Register("$a0"),src);
                    mipsBasicBlock.addInstruction(addu);

                    return true;
                } else if (is2Power(abs + 2)) {
                    int power = getCLZ(abs + 2);
                    MipsSll sll = new MipsSll(src,new Register("$a0"),new Imm(power));
                    mipsBasicBlock.addInstruction(sll);

                    MipsSubu subu = new MipsSubu(new Register("$a0"),new Register("$a0"),src);
                    mipsBasicBlock.addInstruction(subu);

                    subu = new MipsSubu(dst,new Register("$a0"),src);
                    mipsBasicBlock.addInstruction(subu);

                    return true;
                }
            }
        }
        return false;
    }

    public static Boolean is2Power(int n) {
        return (n & (n - 1)) == 0;
    }

    public static int getCLZ(int number) {
        int power = 0;
        while (number > 1) {
            number >>= 1;
            power++;
        }
        return power;
    }

}
