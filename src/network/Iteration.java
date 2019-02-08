package network;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Zain
 */
public class Iteration {
    private List<Node> nodes; //State of the network for current Iteration
    private int number; //Iteration number
    
    public Iteration(int n, List<Node> state, int totalNumberNodes){
        this.number = n;
        this.nodes = new ArrayList<Node>();
        for(Node s: state){
           this.nodes.add(new Node(s));
        }
    }
    
    @Override
    public String toString(){
        String result = "\n \t\t\t /////////// Iteration# "+ number+" ///////////\n";
        result += "\t\t\t\t TotalNodes: "+ (nodes.size() - 1)+"\n";
        for (int i =1 ; i < nodes.size(); i++){
            result += nodes.get(i).toString();
        }
        return result;
    }
}
