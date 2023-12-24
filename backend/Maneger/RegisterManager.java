package backend.Maneger;

import backend.MipsGenerator;
import backend.Operands.Register;
import midend.values.Argument;
import midend.values.BasicBlock;
import midend.values.Const.Function;
import midend.values.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RegisterManager {
    /*该类负责衔接后端翻译与中端的RegAllocator结果*/
    private HashMap<Value, Register> value2Reg;
    private ArrayList<Register> registerPool;

    public RegisterManager() {
        value2Reg = new HashMap<>();
        registerPool = new ArrayList<>();
        for (int i = 1; i < 4; i++) {  // $a寄存器
            registerPool.add(new Register("$a" + i));
        }
        for (int i = 2; i < 10; i++) {  // $t寄存器
            registerPool.add(new Register("$t" + i));
        }
        for (int i = 0; i < 8; i++) {   // $s寄存器
            registerPool.add(new Register("$s" + i));
        }
    }

    public ArrayList<Register> getAllocRegs() {

        BasicBlock basicBlock = MipsGenerator.getLlvmBasicBlock();
        HashSet<Value> use = basicBlock.getUses();
        HashSet<Value> def = basicBlock.getDefs();
        HashSet<Value> out = basicBlock.getOut();

        HashSet<Value> usefulValue = use;
        usefulValue.addAll(def);
        usefulValue.addAll(out);

        HashSet<Register> temp = new HashSet<>();
        for (Value value : usefulValue) {
            Register reg = value2Reg.get(value);
            if (reg!=null) {
                temp.add(reg);
            }
        }
        return new ArrayList<>(temp);
    }


    public void setFunctionReg(Function function) {
        /*为一个函数进行寄存器分配
        * 1. 中端已经分配的
        * 2. 函数参数 - > $a1 - $a3 , $a0留给库函数
        * */
        this.value2Reg = function.getValue2Reg();
        ArrayList<Argument> arguments = function.getArguments();
        int allocateNumber = Math.min(arguments.size(),3);
        for (int i = 0; i < allocateNumber; i++) {
            value2Reg.put(arguments.get(i),new Register("$a" + (i+1)));
        }
    }

    public boolean hasAllocReg(Value value) {
        return this.value2Reg.containsKey(value);
    }

    public Register getRegisterOfValue(Value value) {
        return value2Reg.getOrDefault(value,null);
    }
}
