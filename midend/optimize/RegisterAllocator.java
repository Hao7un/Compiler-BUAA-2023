package midend.optimize;

import backend.Operands.Register;
import midend.IRModule;
import midend.values.Argument;
import midend.values.BasicBlock;
import midend.values.Const.Function;
import midend.values.Const.GlobalVar;
import midend.values.Instructions.IRInstruction;
import midend.values.Instructions.others.Phi;
import midend.values.Value;

import java.util.*;

public class RegisterAllocator {
    private final IRModule module;

    private ArrayList<Register> registerPool;     // 可以用来分配的全局寄存器
    private LinkedHashMap<Value, Register> value2Reg;   // value -> 寄存器，最终存到function中传到后端
    private LinkedHashMap<Register,Value> reg2Value;    // 寄存器 -> Value

    public RegisterAllocator(IRModule module) {
        this.module = module;
        /*为registerPool初始化*/
        this.registerPool = new ArrayList<>();
        for (int i = 2; i < 10; i++) {  // $t寄存器
            registerPool.add(new Register("$t" + i));
        }
        for (int i = 0; i < 8; i++) {   // $s寄存器
            registerPool.add(new Register("$s" + i));
        }
    }

    public void run() {
        for (Function function : module.getFunctions()) {
            initialize();
            liveAnalysis(function);

            allocateRegForBasicBlock(function.getBasicBlocks().get(0));
            function.setValue2Reg(value2Reg);
            //print(function);
        }
    }

    public void print(Function function) {
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            System.out.println(basicBlock.getValueName()+":");

            System.out.print("def:");
            for (Value value : basicBlock.getDefs()) {
                System.out.print(value.getValueName()+" ");
            }
            System.out.print("\n");

            System.out.print("use:");
            for (Value value : basicBlock.getUses()) {
                System.out.print(value.getValueName()+" ");
            }
            System.out.print("\n");

            System.out.print("in:");
            for (Value value : basicBlock.getIn()) {
                System.out.print(value.getValueName()+" ");
            }
            System.out.print("\n");

            System.out.print("out:");
            for (Value value : basicBlock.getOut()) {
                System.out.print(value.getValueName()+" ");
            }
            System.out.print("\n");
        }
    }

    private void initialize() {
        this.value2Reg = new LinkedHashMap<>();
        this.reg2Value = new LinkedHashMap<>();
        for (Register register : registerPool) {
            reg2Value.put(register,null);
        }
    }

    /*活跃变量分析*/
    private void liveAnalysis(Function function) {

        /*计算def和use*/
        for (BasicBlock basicBlock : function.getBasicBlocks()) {
            HashSet<Value> uses = new HashSet<>();
            HashSet<Value> defs = new HashSet<>();
            for (IRInstruction instr : basicBlock.getInstructions()) {
                HashSet<Value> use = instr.getUseValue();
                Value def = instr.getDefValue();
                for (Value value : use) {
                    if (defs.contains(value)) {
                        continue;
                    }
                    if (value instanceof IRInstruction || value instanceof Argument || value instanceof GlobalVar) {
                        uses.add(value);
                    }
                }
                if (def != null && !uses.contains(def)) {
                    defs.add(def);
                }
            }
            basicBlock.setDefs(defs);
            basicBlock.setUses(uses);
        }

        /*计算out和in*/
        boolean changed;
        do {
            changed = false;
            for (BasicBlock basicBlock : function.getBasicBlocks()) {
                HashSet<Value> newOut = new HashSet<>();
                for (BasicBlock successor : basicBlock.getSuccessors()) {
                    newOut.addAll(successor.getIn());
                }

                HashSet<Value> newIn = new HashSet<>(newOut);
                newIn.removeAll(basicBlock.getDefs());
                newIn.addAll(basicBlock.getUses());

                if (!newIn.equals(basicBlock.getIn()) || !newOut.equals(basicBlock.getOut())) {
                    changed = true;
                    basicBlock.setIn(newIn);
                    basicBlock.setOut(newOut);
                }
            }
        } while (changed);
    }

    /*
     * 1. 变量在基本块中最后一次使用，并且out集合中也不存在该变量，remove该变量
     * 2. 分配完当前基本块后，递归分配下一个基本块。如果下个基本块的in集合里不包含某个寄存器正在映射的变量，
     *    可以暂时释放该寄存器，处理完下个基本块后恢复。
     * */

    private boolean isLastUsed(Value value , IRInstruction instr , BasicBlock basicBlock) {
        // 判断这个instr中的这个value，是不是在basic block中最后一次被使用
        int idx = basicBlock.getInstructions().indexOf(instr);
        for (int i = idx + 1; i < basicBlock.getInstructions().size(); i++) {
            if (basicBlock.getInstructions().get(i).getUseValue().contains(value)) {
                return false;
            }
        }
        return true;
    }

    private void allocateRegForBasicBlock(BasicBlock basicBlock) {
        /*为基本块分配寄存器*/
        HashSet<Value> defInBlock = new HashSet<>();
        HashSet<Value> killValues = new HashSet<>();

        for (IRInstruction instr : basicBlock.getInstructions()) {
            /*处理use*/
            if (!(instr instanceof  Phi)) {
                HashSet<Value> useValue = instr.getUseValue();
                for (Value value : useValue) {
                    if (isLastUsed(value,instr,basicBlock) && !basicBlock.getOut().contains(value)) {   //可以删除,后面的基本块用不上了
                        if (value2Reg.containsKey(value)) { //已经分配了寄存器才需要删除
                            Register reg = value2Reg.get(value);
                            reg2Value.put(reg,null);
                            killValues.add(value);   //该基本块最后一次使用，递归回去之后需要恢复
                        }
                    }
                }
            }

            /*处理Def*/
            Value defValue = instr.getDefValue();
            if (defValue != null) {
                defInBlock.add(defValue);
                Register allocReg = allocateRegForValue();
                if (allocReg != null) {
                    value2Reg.put(defValue, allocReg);
                    reg2Value.put(allocReg, defValue);
                }
            }
        }

        /*遍历直接支配的basic blocks*/
        HashSet<BasicBlock> idoms = basicBlock.getIdoms();
        for (BasicBlock idom : idoms) {
            HashSet<Value> in = idom.getIn();
            HashMap<Register, Value> temp_reg2Value = new HashMap<>(reg2Value);
            for (Register register : reg2Value.keySet()) {
                Value value = reg2Value.get(register);
                if (value == null) {
                    continue;
                }
                if (!in.contains(value)) {  // in集合中不包含
                    reg2Value.put(register,null); //释放寄存器
                }
            }
            allocateRegForBasicBlock(idom);
            reg2Value.clear();
            reg2Value.putAll(temp_reg2Value);
        }

        /* 该基本块中def的变量，递归回去之后一定不存在，所以可以进行释放*/
        for (Value value : defInBlock) {
            if (value2Reg.containsKey(value)) {
                reg2Value.put(value2Reg.get(value),null);
            }
        }

        //恢复被删除的
        for (Value value : killValues) {
            if (!defInBlock.contains(value) && reg2Value.get(value2Reg.get(value)) == null) {
                reg2Value.put(value2Reg.get(value), value);
            }
        }
    }

    private Register allocateRegForValue() {
        for (Register register : reg2Value.keySet()) {
            if (reg2Value.get(register) == null) {
                return register;
            }
        }
        return null;
    }
}
