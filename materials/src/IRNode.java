import ir.*;
import ir.datatype.*;
import ir.operand.*;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class IRNode {

    public static class Definition {
        public String varName;
        public int lineNum; //uniquely identifies every instruction (all on diff. lines)

        //I want to store varName and lineNumber as each entry in a set
        public Definition(String varName, int lineNum) {
            this.varName = varName;
            this.lineNum = lineNum;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Definition other = (Definition) obj;
            return lineNum == other.lineNum && varName.equals(other.varName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lineNum, varName);
        }
    }
    //Each node is a list of instrucs and has predecessors/successors
    //Every node/block also has its own GEN, KILL, etc sets
        //these sets use HashSet, and use varName and lineNumber to ensure uniqueness
    List<IRInstruction> instructions;
    List<IRNode> predecessors;
    List<IRNode> successors;
    Set<Definition> GEN = new HashSet<>();
    Set<Definition> KILL = new HashSet<>();
    Set<Definition> IN = new HashSet<>();
    Set<Definition> OUT = new HashSet<>();


    // If we initialize without any parameters, just leave everything empty
    public IRNode() {}

    public IRNode(List<IRInstruction> instructions, List<IRNode> predecessors, List<IRNode> successors) {
        this.instructions = instructions;
        this.predecessors = predecessors;
        this.successors = successors;
    }

    public void addToGen(String vN, int lN) {
        this.GEN.add(new Definition(vN, lN));
    }
    public void addToKill(String vN, int lN) {
        this.KILL.add(new Definition(vN, lN));
    }
    public void addToIn(String vN, int lN) {
        this.IN.add(new Definition(vN, lN));
    }
    public void addToOut(String vN, int lN) {
        this.OUT.add(new Definition(vN, lN));
    }
}