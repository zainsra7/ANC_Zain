package network;
import java.util.*;
/**
 * Node class containing its id, routing table and list of neighbours.
 * @author Zain
 */
public class Node {
   private List<Integer> distance_vector; // [0] is for n0 and it will store the distance from current node to n0. Distance 1 means one hop, 0 means the current node and negative means the node is crashed.
   private List<Integer> routing_vector; // [0] is for n0 and it will store the link to get to n0 e.g previous node id as in case of n0 - n1 - n2, for routing vector of n0, [2] will contain 1 as the previous node to reach n2 is n1.
   private int id; // 0,1,2 etc 
   private List<Integer> list_of_neighbours; // list of all the neighbour of current node
   
   //Following fields are used as a buffer and any update to routing table is stored int that buffer
   //and the actual distance and routing vector of a node is updated after each iteration
   private List<Integer> buffer_distance;
   private List<Integer> buffer_routing;

    public Node(int id, int totalNumberOfNodes) {
        this.id = id;
        distance_vector = new ArrayList<Integer>();
        routing_vector = new ArrayList<Integer>();
        list_of_neighbours = new ArrayList<Integer>();
        buffer_distance = new ArrayList<Integer>();
        buffer_routing = new ArrayList<Integer>();
        
        //Initializing Node's Routing Table (Distance Vector and Routing Vector)
        for(int i=0; i<totalNumberOfNodes; i++){
            if(i == id){
                distance_vector.add(0);
                routing_vector.add(id);
                buffer_distance.add(0);
                buffer_routing.add(id);
            }
            else {
                distance_vector.add(-1); // -1 means infinity
                routing_vector.add(-1);
                buffer_distance.add(-1);
                buffer_routing.add(-1);
            }
        }
    }
    public Node(Node s){
         this.id = s.getID();
         this.distance_vector = new ArrayList<Integer>();
         this.routing_vector = new ArrayList<Integer>();
         this.list_of_neighbours = new ArrayList<Integer>();
         for(int i=0; i<s.getDistance_vector().size(); i++){
             this.distance_vector.add(s.getDistance_vector().get(i));
             this.routing_vector.add(s.getRouting_vector().get(i)); 
         }
         for(int x=0; x<s.getList_of_neighbours().size(); x++){
             this.addNeighbours(s.getList_of_neighbours().get(x));
         }
    }
    public void addNeighbours(int id){
        this.list_of_neighbours.add(id);
    }
    public void updateDistanceVector(int destination, int cost){
        this.distance_vector.set(destination, cost);
    }
    public void updateRoutingVector(int destination, int previousNode){
        this.routing_vector.set(destination, previousNode);
    }
    public void updateDistanceBuffer(int destination, int cost){
        this.buffer_distance.set(destination, cost);
    }
    public void updateRoutingBuffer(int destination, int previousNode){
        this.buffer_routing.set(destination, previousNode);
    }
    
    public boolean updateRoutingTable(List<Integer> srcDistanceVector, List<Integer> srcRoutingVector, int srcId, boolean splitHorizon){
        boolean learnedNewPath = false;
        for(int i=1; i< distance_vector.size(); i++){
            //Let's say i is Node 1 and Node 3 is "this" and Node 2 is the neighbour sending its "srcDistanceVector" and "srcRoutingVector".
            
            //If SplitHorizon is on and the cost/link to reach Node 1 is -1 for Node 2, and Node 3 learned about Node 1 from Node 2 then update Node 3 cost to reach
            //Node 1 as -1
            //So if a new path comes to reach Node 1, then Node 3 will be able to update its routing table
            if(splitHorizon && srcDistanceVector.get(i) == -1 && srcRoutingVector.get(i) == -1 && routing_vector.get(i) == srcId){
                updateRoutingVector(i, -1);
                updateDistanceVector(i, -1);
                continue;
            }
            //1.Skip this iteration if current Node to process is Node3 or Node2. Because we already know the cost/link to these nodes. OR
            //2.Skip this iteration if the cost to reach Node 1 is -1 for Node 2 (and we know from above "if" that Node 3 didn't learn the path to Node 1 from Node 2) OR
            //3.Skip this iteration if splitHorizon is on and Node 2 learned about Node 1 from Node 3. (This is done to stop count to infinity problem)
            if(i == id || i == srcId || srcDistanceVector.get(i) == -1 || (splitHorizon && srcRoutingVector.get(i) == id))
                continue;
            
            //Cost to Node 1 = Cost to Node 1 from Node 2 + Cost to Node 2 from Node 3
            int distanceFromNeighbour = srcDistanceVector.get(i) + distance_vector.get(srcId);
            
            //Update Node 3 routing table if:
            //1. Cost to Node 1 is less than already known Cost OR
            //2. Node 3 doesn't know about the cost to Node 1 yet (-1)
            //3. Node 3 learned about the cost to Node 1 from Node 2 AND it's an updated cost value than already known

            if(distanceFromNeighbour < distance_vector.get(i) || distance_vector.get(i) == -1 || (routing_vector.get(i) == srcId && distanceFromNeighbour != distance_vector.get(i))){ //I added the last routing_vector.get(i) == srcId so that if you get a new value from neighbour just update it then
                learnedNewPath = true;
                updateDistanceBuffer(i, distanceFromNeighbour);
                //If link to reach Node 2 from Node 3 is Node 3 i.e closest neighbour.
                if(routing_vector.get(srcId) == id)
                    updateRoutingBuffer(i, srcId); // 2->4 for the node between 2 and 6, closest node to the current
                else updateRoutingBuffer(i, routing_vector.get(srcId));
            }
        }
        return learnedNewPath;
    }

    public List<Integer> getList_of_neighbours() {
        return list_of_neighbours;
    }

    public List<Integer> getDistance_vector() {
        return distance_vector;
    }

    public void setDistance_vector(List<Integer> distance_vector) {
        this.distance_vector = distance_vector;
    }

    public List<Integer> getRouting_vector() {
        return routing_vector;
    }

    public void setRouting_vector(List<Integer> routing_vector) {
        this.routing_vector = routing_vector;
    }

    public void setList_of_neighbours(List<Integer> list_of_neighbours) {
        this.list_of_neighbours = list_of_neighbours;
    }
    
    //Crash (this) node, meaning to clear it's distance,routing vector alongwith list of neighbours
    public void crashNode(){
         distance_vector.clear();
         routing_vector.clear();
         list_of_neighbours.clear();
    }
    //inform the nrighbour (this) of your crash
    public void neighbourCrash(int neighbourId){
        distance_vector.set(neighbourId, -1);
        routing_vector.set(neighbourId, -1);
    }
    
    public void bufferToActual(){
        //It's an O(n) operation, where n is the number of nodes
        //Transfer data from buffer to actual and also clear buffer
        for(int i=1; i<distance_vector.size(); i++){
            if(!(buffer_distance.get(i) == -1 && buffer_routing.get(i) == -1)){
                updateDistanceVector(i, buffer_distance.get(i));
                updateRoutingVector(i, buffer_routing.get(i));
                
                buffer_distance.set(i, -1);
                buffer_routing.set (i, -1);
            }
        }
    }
   @Override
    public String toString(){
        
        String result = "\n \t       -- Node# " + id + ": Routing Table --\n\t       Neighbours -> [";
        for(int x =0; x < list_of_neighbours.size(); x++){
            result += "" + (list_of_neighbours.get(x)) + ",";
        }
        result += "]\n\t      -------------------------------\n";
        
        for(int i=1; i<distance_vector.size(); i++){
            if(distance_vector.get(i) == -1){
            result+= "\t       D: " + i + " || C: " + distance_vector.get(i)  + " || L: " + id+" -> "+routing_vector.get(i) + "\n";
            }
            else result+= "\t       D: " + i + " || C: " + distance_vector.get(i)  + "  || L: " + id +" -> "+ routing_vector.get(i) + "\n"; 
        }
        result+= "\n";
        return result;
    }
    
    public int getID(){return id;}
}