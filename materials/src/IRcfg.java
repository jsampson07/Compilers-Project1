import ir.*;
import java.util.List;
import java.util.ArrayList;

/*
I want my CFG to contain...
1) List of IRNodes
*/

public class IRcfg {
    List<IRNode> nodes;

    public IRcfg() {
        this.nodes = new ArrayList<>();
    }

    public void addToCFG(IRNode node) {
        this.nodes.add(node);
    }

    public void constructCFG() {
        //implement the logic to traverse the CFG and construct Nodes
    }
}