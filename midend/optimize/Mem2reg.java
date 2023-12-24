package midend.optimize;

import midend.IRBuilder;
import midend.IRModule;
import midend.types.IntegerType;
import midend.types.PointerType;
import midend.values.BasicBlock;
import midend.values.Const.Function;
import midend.values.Instructions.IRInstruction;
import midend.values.Instructions.mem.Alloca;
import midend.values.Instructions.mem.Load;
import midend.values.Instructions.mem.Store;
import midend.values.Instructions.others.Phi;
import midend.values.UndefinedValue;
import midend.values.Value;

import java.util.*;

public class Mem2reg {
    /*Build SSA*/

    private final CFGAnalysis cfgAnalysis;      // cfg information
    private final IRModule module;
    private LinkedHashMap<BasicBlock,ArrayList<BasicBlock>> dfMap = new LinkedHashMap<>();         // 支配边界
    private LinkedHashMap<BasicBlock,ArrayList<BasicBlock>> prevsMap = new LinkedHashMap<>();
    private LinkedHashMap<BasicBlock,ArrayList<BasicBlock>> nextsMap = new LinkedHashMap<>();
    private LinkedHashMap<BasicBlock,ArrayList<BasicBlock>> domTree = new LinkedHashMap<>();      // 支配树->DFS

    private LinkedHashMap<Phi, Alloca> newPhis = new LinkedHashMap<>();                            // 记录Phi指令到Alloca的映射
    private LinkedHashMap<Alloca, ArrayList<BasicBlock>> allocaDefs = new LinkedHashMap<>();       // 记录一个alloca被哪些基本块使用
    private ArrayList<Alloca> allocas = new ArrayList<>();                             // 记录所有alloca
    private LinkedHashMap<Alloca,Stack<Value>> incomingValues;

    public Mem2reg(CFGAnalysis cfgAnalysis, IRModule module) {
        this.cfgAnalysis = cfgAnalysis;
        this.module = module;
    }

    public void run() {
        for (Function function : module.getFunctions()) {
            newPhis = new LinkedHashMap<>();
            allocaDefs = new LinkedHashMap<>();
            allocas = new ArrayList<>();
            incomingValues = new LinkedHashMap<>();
            prevsMap = cfgAnalysis.getPrevsMap().get(function);
            nextsMap = cfgAnalysis.getNextsMap().get(function);
            domTree  = cfgAnalysis.getDomTree().get(function);
            dfMap = cfgAnalysis.getDfMap().get(function);
            initialize(function);
            insertPhi();
            rename(function.getBasicBlocks().get(0),incomingValues);
        }
    }

    private void initialize(Function function) {
        ArrayList<BasicBlock> basicBlocks = function.getBasicBlocks();
        /*初始化Alloca*/
        for (BasicBlock basicBlock : basicBlocks) {
            for (IRInstruction instruction : basicBlock.getInstructions()) {
                if (instruction instanceof Alloca
                        && ((PointerType)instruction.getValueType()).getPointedType() instanceof IntegerType) {
                    allocaDefs.put((Alloca) instruction,new ArrayList<>());
                    allocas.add((Alloca) instruction);
                    incomingValues.put((Alloca) instruction,new Stack<>());       // set up a new stack
                    incomingValues.get((Alloca) instruction).push(new UndefinedValue());  //初始化为undefinedValue
                }
            }
        }

        /*初始化Store*/
        for (BasicBlock basicBlock : basicBlocks) {
            for (IRInstruction instruction : basicBlock.getInstructions()) {
                if (instruction instanceof Store) {
                    Value pointer = instruction.getOperand(1);
                    if (pointer instanceof Alloca && allocaDefs.containsKey(pointer)) {
                        allocaDefs.get(pointer).add(basicBlock);
                    }
                }
            }
        }
    }

    private void insertPhi() {
        for (Alloca alloca : allocaDefs.keySet()) {
            HashSet<BasicBlock> F = new HashSet<>();                                 //Set of basic blocks where phi is added
            LinkedList<BasicBlock> W = new LinkedList<>(allocaDefs.get(alloca));   //set of basic blocks that contain definition of v
            while(!W.isEmpty()) {
                BasicBlock X = W.pop();        //remove a basic block from W
                ArrayList<BasicBlock> dfs = dfMap.get(X);
                for (BasicBlock Y : dfs) {
                    if (!F.contains(Y)) {
                        Phi phi = IRBuilder.buildPhi(prevsMap.get(Y),Y);
                        Y.insertAtEntry(phi);
                        newPhis.put(phi,alloca);
                        F.add(Y);
                        if(!allocaDefs.get(alloca).contains(Y)) {
                            W.add(Y);
                        }
                    }
                }
            }
        }
    }

    private void rename(BasicBlock basicBlock , HashMap<Alloca,Stack<Value>> oldIncomingValues) {
        HashMap<Alloca, Stack<Value>> newIncomingValues = new HashMap<>();
        /*COPY:old Incoming Values To New Incoming Values*/
        for (Map.Entry<Alloca, Stack<Value>> entry : oldIncomingValues.entrySet()) {
            Alloca key = entry.getKey();
            Stack<Value> valueCopy = new Stack<>();
            for (Value value : entry.getValue()) {
                valueCopy.push(value);
            }
            newIncomingValues.put(key, valueCopy);
        }

        Iterator<IRInstruction> iterator = basicBlock.getInstructions().iterator();
        while (iterator.hasNext()) {
            IRInstruction instr = iterator.next();
            if (instr instanceof Alloca && allocas.contains(instr)) {      // remove instr from basic block
                iterator.remove();
            } else if (instr instanceof Load) {
                if (instr.getOperand(0) instanceof Alloca && allocas.contains((Alloca) instr.getOperand(0))) {
                    Value newValue = newIncomingValues.get((Alloca) instr.getOperand(0)).peek(); //new value
                    instr.replaceAllUseWithValue(newValue);   // replace all use of instr with newValue
                    iterator.remove();          // remove instr from bb
                }
            } else if (instr instanceof Store) {
                if (instr.getOperand(1) instanceof Alloca &&  allocas.contains((Alloca) instr.getOperand(1))) {
                    Value value = instr.getOperand(0);
                    newIncomingValues.get((Alloca) instr.getOperand(1)).push(value);
                    iterator.remove();          // remove instr from bb
                }
            } else if (instr instanceof Phi) {
                if (newPhis.containsKey(instr)) {
                    newIncomingValues.get(newPhis.get(instr)).push(instr);
                }
            }
        }

        /*update phi in the successors of basic blocks*/
        for (BasicBlock successor : nextsMap.get(basicBlock)) {
            for (IRInstruction instr1 : successor.getInstructions()) {
                if (instr1 instanceof Phi phi) {
                    Alloca alloca = newPhis.get(phi);
                    Value value = newIncomingValues.get(alloca).peek();
                    phi.addPhiOperand(value, basicBlock);
                }
            }
        }

        /*DFS*/
        for (BasicBlock idom : domTree.get(basicBlock)) {
            rename(idom,newIncomingValues);
        }
    }
}
