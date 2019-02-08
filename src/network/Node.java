package network;
import java.util.*;
/**
 *
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
    
    public boolean updateRoutingTable(List<Integer> srcDistanceVector, List<Integer> srcRoutingVector, int srcId){
        boolean learnedNewPath = false;
        for(int i=1; i< distance_vector.size(); i++){
            if(i == id || i == srcId || srcDistanceVector.get(i) == -1)
                continue;
            int distanceFromNeighbour = srcDistanceVector.get(i) + distance_vector.get(srcId);
            //If the distance to reach a particular node from neighbour is less than the current distance
            //then update your distance vector and routing vector with shortest cost and outlink
            if(distanceFromNeighbour < distance_vector.get(i) || distance_vector.get(i) == -1){
                learnedNewPath = true;
                updateDistanceBuffer(i, distanceFromNeighbour);
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
        
        String result = "\n \t\t\t -- Node# " + id + ": Routing Table --\n\t\t\t Neighbours -> [";
        for(int x =0; x < list_of_neighbours.size(); x++){
            result += "" + (list_of_neighbours.get(x)) + ",";
        }
        result += "]\n\t\t   ----------------------------------------\n";
        
        for(int i=1; i<distance_vector.size(); i++){
            if(distance_vector.get(i) == -1){
            result+= "\t\t\t D: " + i + " || C: " + distance_vector.get(i)  + " || L: " + id+" -> "+routing_vector.get(i) + "\n";
            }
            else result+= "\t\t\t D: " + i + " || C: " + distance_vector.get(i)  + "  || L: " + id +" -> "+ routing_vector.get(i) + "\n"; 
        }
        result+= "\n";
        return result;
    }
    
    public int getID(){return id;}
}