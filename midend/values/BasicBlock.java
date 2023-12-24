package midend.values;

import midend.types.LabelType;
import midend.values.Const.Function;
import midend.values.Instructions.IRInstruction;

import java.util.ArrayList;
import java.util.HashSet;

public class BasicBlock extends Value{
    private ArrayList<IRInstruction> IRInstructions;
    private Function parentFunction; // Basic Block所处的函数

    /*与数据流图有关的前驱和后继基本块*/
    private ArrayList<BasicBlock> predecessors;
    private ArrayList<BasicBlock> successors;

    private HashSet<BasicBlock> idoms;  //直接支配的基本块

    /*活跃变量分析*/
    private HashSet<Value> defs;
    private HashSet<Value> uses;
    private HashSet<Value> in;
    private HashSet<Value> out;

    public BasicBlock(String valueName, Function parentFunction) {
        super(valueName, new LabelType());
        this.parentFunction = parentFunction;
        this.IRInstructions = new ArrayList<>();
        this.predecessors = new ArrayList<>();
        this.successors = new ArrayList<>();
        this.in = new HashSet<>();
        this.out = new HashSet<>();
        this.defs = new HashSet<>();
        this.uses = new HashSet<>();
    }

    public Function getParentFunction(){
        return parentFunction;
    }
    public ArrayList<BasicBlock> getPredecessors() {
        return predecessors;
    }

    public ArrayList<BasicBlock> getSuccessors() {
        return successors;
    }

    public void setPredecessors(ArrayList<BasicBlock> predecessors) {
        this.predecessors = predecessors;
    }

    public void setSuccessors(ArrayList<BasicBlock> successors) {
        this.successors = successors;
    }

    public void addPredecessor(BasicBlock predecessor) {
        this.predecessors.add(predecessor);
    }

    public void addSuccessor(BasicBlock successor) {
        this.successors.add(successor);
    }

    public HashSet<Value> getDefs() {
        return defs;
    }

    public HashSet<Value> getUses() {
        return uses;
    }

    public void setDefs(HashSet<Value> defs) {
        this.defs = defs;
    }

    public void setUses(HashSet<Value> uses) {
        this.uses = uses;
    }

    public void  setIn(HashSet<Value> in) {
        this.in = in;
    }

    public void setOut(HashSet<Value> out) {
        this.out = out;
    }

    public void setIdoms(HashSet<BasicBlock> idoms) {
        this.idoms = idoms;
    }

    public HashSet<BasicBlock> getIdoms() {
        return idoms;
    }

    public HashSet<Value> getIn() {
        return in;
    }

    public HashSet<Value> getOut() {
        return out;
    }

    public void removePredecessor(BasicBlock predecessor) {
        assert predecessors.contains(predecessor);
        this.predecessors.remove(predecessor);
    }

    public void removeSuccessor(BasicBlock successor) {
        assert successors.contains(successor);
        this.successors.remove(successor);
    }

    public void addInstruction(IRInstruction IRInstruction) {
        this.IRInstructions.add(IRInstruction);
    }

    public ArrayList<IRInstruction> getInstructions() {
        return IRInstructions;
    }

    public void insertAtEntry(IRInstruction instruction) {
        ArrayList<IRInstruction> newInstrs = new ArrayList<>();
        newInstrs.add(instruction);
        newInstrs.addAll(IRInstructions);
        this.IRInstructions = newInstrs;
    }
    public IRInstruction getLastInstr() {
        int length = IRInstructions.size();
        assert  length >=1;

        return IRInstructions.get(length-1);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getValueName()).append(":\n");
        for (IRInstruction IRInstruction : IRInstructions) {
            sb.append("\t");
            sb.append(IRInstruction.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

}


