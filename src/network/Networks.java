package network;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.JTextArea;

/**
 * Main class which contains normal/slow convergence logic, along-with updating/storing state of network and communicating with GUI.
 * @author Zain
 */
public class Networks {

    public static List<Iteration> iterations;
    public static List<Node> nodes; //Nodes for current Iteration/State
    public static int totalNodes;
    public static int totalIterations;
    public static boolean stopNetwork;
    public static Interface it;

    //Fields for Slow-Converge (Count to Infitinity)
    public static int crashIteration; //Iteration on which the node will be crashed
    public static int crashNode; //Node to Crash

    public static void setup() {
        iterations = new ArrayList<>();
        nodes = new ArrayList<>();
    }

    public static void bellmanFordAlgorithm(JTextArea screen) {
        it.writeToScreen(iterations.get(0).toString()); //Write the initializing Iteration to the screen
        boolean learnedNewPaths;
        boolean convergeEarly = false;
        while (!convergeEarly) {
            convergeEarly = true;
            for (int i = 1; i < nodes.size(); i++) {
                Node source = nodes.get(i); //Current Node
                List<Integer> sourceNeighbours = source.getList_of_neighbours(); //All Neighbours of Current Nodes
                for (int j = 0; j < sourceNeighbours.size(); j++) {
                    //Update each neighbours routing table
                    int neighbourId = sourceNeighbours.get(j);
                    learnedNewPaths = nodes.get(neighbourId).updateRoutingTable(source.getDistance_vector(), source.getRouting_vector(), source.getID(), false);
                    if (learnedNewPaths) {
                        convergeEarly = false; //It means that routing table of some node changed, so we can't converge and need to continue
                    }
                }
            }
            //Copy Buffer to Actual Vectors for each node (For Simultaneuosly transfer of routing tables)
            for (int i = 1; i < nodes.size(); i++) {
                nodes.get(i).bufferToActual();
            }
            Iteration itr = new Iteration(++totalIterations, nodes, totalNodes + 1);
            iterations.add(itr);
            it.writeToScreen(itr.toString());
        }
    }

    public static void slowConvergence(boolean splitHorizon, int stopIteration) {
        it.writeToScreen(iterations.get(0).toString());
        boolean learnedNewPaths;
        boolean convergeEarly = false;
        while (!convergeEarly) {
            convergeEarly = true;
            for (int i = 1; i < nodes.size(); i++) {
                Node source = nodes.get(i); //Current Node
                if (!(totalIterations + 1 == crashIteration && source.getID() == crashNode)) {
                    List<Integer> sourceNeighbours = source.getList_of_neighbours(); //All Neighbours of Current Nodes  
                    for (int j = 0; j < sourceNeighbours.size(); j++) {
                        //Update each neighbours routing table
                        int neighbourId = sourceNeighbours.get(j);
                        learnedNewPaths = nodes.get(neighbourId).updateRoutingTable(source.getDistance_vector(), source.getRouting_vector(), source.getID(), splitHorizon);
                        if (learnedNewPaths) {
                            convergeEarly = false; //It means that routing table of some node changed, so we can't converge and need to continue
                        }
                    }
                }
            }
            //Copy Buffer to Actual Vectors for each node (For Simultaneuosly transfer of routing tables)
            for (int i = 1; i < nodes.size(); i++) {
                nodes.get(i).bufferToActual();
            }
            if (totalIterations + 1 == crashIteration) {
                List<Integer> crashSourceNeighbours = nodes.get(crashNode).getList_of_neighbours(); //All Neighbours of crashed Node
                for (int x = 0; x < crashSourceNeighbours.size(); x++) {
                    nodes.get(crashSourceNeighbours.get(x)).neighbourCrash(crashNode); //Neighbour will update it's routing and distance vector with -1
                }
                nodes.get(crashNode).crashNode(); //Clearing the node's distance, routing vector alongwith neighbour list
                convergeEarly = false;
            }

            Iteration itr = new Iteration(++totalIterations, nodes, totalNodes + 1);
            iterations.add(itr);
            it.writeToScreen(itr.toString());
            if(!splitHorizon && totalIterations == stopIteration){
                convergeEarly = true;
            }
        }
    }
    //Function to insert a node in nodes List
    public static void addNode(int sourceNode, int destinationNode, int linkCost) {
        nodes.get(sourceNode).addNeighbours(destinationNode);
        nodes.get(destinationNode).addNeighbours(sourceNode); //Bi-directional

        nodes.get(sourceNode).updateDistanceVector(destinationNode, linkCost);
        nodes.get(sourceNode).updateRoutingVector(destinationNode, sourceNode);

        nodes.get(destinationNode).updateDistanceVector(sourceNode, linkCost);
        nodes.get(destinationNode).updateRoutingVector(sourceNode, destinationNode);
    }
    
    //Function to read input file in case of normal convergence
    public static String readNetworkFile(String filename) {
        String message = "";
        //File format should be
        //1. First line showing Total Number of Nodes
        //2. Consecutive lines follows the pattern: SourceNode -- LinkCost -- DestinationNode
        Path path = Paths.get(filename);
        Scanner scanner;
        try {
            scanner = new Scanner(path);
            nodes.clear();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.contains("--")) {
                    //It's the first line containing total number of nodes
                    totalNodes = Integer.valueOf(line);
                    //Initialize the nodes array
                    initializeNodeArray();
                } else {
                    String[] tokenizer = line.split("--");
                    int sourceNode = Integer.valueOf(tokenizer[0]);
                    int destinationNode = Integer.valueOf(tokenizer[2]);
                    int linkCost = Integer.valueOf(tokenizer[1]);

                    addNode(sourceNode, destinationNode, linkCost);
                }
            }
            //Clear Iterations and nodes from previous (normal/slow convergence run)
            iterations.clear();
            totalIterations = 0;
            //Initial Iteration -- Containing Initial State of the Network
            Iteration it = new Iteration(totalIterations, nodes, totalNodes + 1);
            iterations.add(it);
        } catch (IOException ex) {
            message = "File Not Found!";
        }
        return message;
    }

    //Function to read input file in case of slow convergence
    public static String readSNetworkFile(String filename) {
        String message = "";
        //File format should be
        //1. First line showing Total Number of Nodes
        //2. Consecutive lines follows the pattern: SourceNode -- LinkCost -- DestinationNode 
        //3. OR Iteration~3 which states that on 3rd Iteration crash the node given in the following line
        //4. Crash:1 means to crash Node1 in the network
        Path path = Paths.get(filename);
        Scanner scanner;
        try {
            scanner = new Scanner(path);
            nodes.clear();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] tokenizer;

                if (line.contains("--")) {
                    tokenizer = line.split("--");
                    int sourceNode = Integer.valueOf(tokenizer[0]);
                    int destinationNode = Integer.valueOf(tokenizer[2]);
                    int linkCost = Integer.valueOf(tokenizer[1]);

                    addNode(sourceNode, destinationNode, linkCost);
                } else if (line.contains("~")) {
                    tokenizer = line.split("~");
                    crashIteration = Integer.valueOf(tokenizer[1]);

                } else if (line.contains(":")) {

                    tokenizer = line.split(":");
                    crashNode = Integer.valueOf(tokenizer[1]);
                } else {
                    //It's the first line containing total number of nodes
                    totalNodes = Integer.valueOf(line);
                    //Initialize the nodes array
                    initializeNodeArray();
                }
            }
            //Clear Iterations from previous (normal/slow convergence run)
            iterations.clear();
            totalIterations = 0;
            //Initial Iteration -- Containing Initial State of the Network
            Iteration it = new Iteration(totalIterations, nodes, totalNodes + 1);
            iterations.add(it);
        } catch (IOException ex) {
            message = "File Not Found!";
        }
        return message;
    }

    public static String writeToFile(String filename) {
        String message = "";
        try {
            PrintWriter fout = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
            for (int i = 0; i < iterations.size(); i++) {
                String line = iterations.get(i).toString();
                fout.println(line);
            }
            fout.flush();
            fout.close();
        } catch (IOException ex) {
            message = "Couldn't write to "+ filename + ". Try Again!";
        }
        return message;

    }

    public static void initializeNodeArray() {
        boolean addNodes = false;
        if (nodes.isEmpty()) {
            addNodes = true;
        }
        for (int i = 0; i < totalNodes + 1; i++) {
            Node temp = new Node(i, totalNodes + 1);
            if (addNodes) {
                nodes.add(temp);
            } else {
                nodes.set(i, temp);
            }
        }
    }

   public static void printIteration(int itrNumber){
       it.writeToScreen(iterations.get(itrNumber).toString());
   }
    
    public static void main(String[] args) {
        it = new Interface();
    }
}
