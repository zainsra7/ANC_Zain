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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Zain
 */
public class Networks {

    public static List<Iteration> iterations;
    public static List<Node> nodes; //Nodes for current Iteration/State
    public static int totalNodes;
    public static int totalIterations;

    //Fields for Slow-Converge (Count to Infitinity)
    public static int crashIteration; //Iteration on which the node will be crashed
    public static int crashNode; //Node to Crash

    public static void bellmanFordAlgorithm(boolean normalConvergence, boolean splitHorizon) {
        boolean learnedNewPaths = false;
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
            //System.out.println(itr.toString());
            iterations.add(itr);
        }
    }

    public static void slowConvergence(boolean splitHorizon) {
        boolean learnedNewPaths = false;
        boolean convergeEarly = false;
        while (!convergeEarly) {
            convergeEarly = true;
            for (int i = 1; i < nodes.size(); i++) {
                Node source = nodes.get(i); //Current Node
               if(!(totalIterations + 1 == crashIteration && source.getID() == crashNode)){ 
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
                  for(int x=0; x< crashSourceNeighbours.size(); x++){
                    nodes.get(crashSourceNeighbours.get(x)).neighbourCrash(crashNode); //Neighbour will update it's routing and distance vector with -1
                  }
                   nodes.get(crashNode).crashNode(); //Clearing the node's distance, routing vector alongwith neighbour list
                    convergeEarly = false;
                }
            
            Iteration itr = new Iteration(++totalIterations, nodes, totalNodes + 1);
            //System.out.println(itr.toString());
            iterations.add(itr);
                                for (int i = 0;  totalIterations == 10 && i < iterations.size(); i++) {
                        System.out.println(iterations.get(i).toString());
                    }
            
        }
    }

    public static void main(String[] args) {
        iterations = new ArrayList<>();
        nodes = new ArrayList<>();
        /*
        Read File--
        TotalNumberOfNodes: 6
        NodeA -- Distance/Cost -- NodeB
        1 -- 3 -- 2 //It means that Node1 is connected to Node 2 and the cost of the bidirectional - link (n1-n2) is 3
        1 -- 6 -- 3
        1 -- 1 -- 5
        2 -- 1 -- 4
        2 -- 3 -- 5
        3 -- 3 -- 5
        3 -- 1 -- 6
        4 -- 1 -- 5
        5 -- 2 -- 6
         */

        //Display Menu and check for options
        menuDisplay();
        int choice = -1; //User choice from menu
        Scanner reader = new Scanner(System.in);
        boolean run = true;

        while (run) {
            choice = reader.nextInt();
            switch (choice) {
                case 1:
                    System.out.println("Enter the Filename: ");
                    String filename = reader.next();
                    readNetworkFile(filename);

                    bellmanFordAlgorithm(true, false);

                    //Printing all iterations after convergence
                    for (int i = 0; i < iterations.size(); i++) {
                        System.out.println(iterations.get(i).toString());
                    }
                    break;
                case 2:
                    System.out.println("Enter the Filename: ");
                    filename = reader.next();
                    readSNetworkFile(filename);
                    slowConvergence(true);
                    //Printing all iterations after convergence
                    for (int i = 0; i < iterations.size(); i++) {
                        System.out.println(iterations.get(i).toString());
                    }
                    break;
                case 3:
                    System.out.println("Enter the Filename: ");
                    filename = reader.next();
                    readSNetworkFile(filename);
                    slowConvergence(false);
                    break;
                case 4:
                    if (iterations.isEmpty()) {
                        System.out.println("Create a Network first using Option 1 or 2");
                    } else {
                        int iteration_num = -1;
                        do {
                            System.out.println("Total# of Iterations: [0-" + (iterations.size() - 1) + "]\nEnter Valid Iteration#: ");
                            iteration_num = reader.nextInt();
                        } while (iteration_num < 0 || iteration_num >= iterations.size());
                        System.out.println(iterations.get(iteration_num).toString());
                    }
                    break;
                case 5:
                    if (iterations.isEmpty()) {
                        System.out.println("Create a Network first using Option 1 or 2");
                    } else {
                        System.out.println("Enter the Filename: ");
                        filename = reader.next();
                        writeToFile(filename);
                    }
                    break;
                case 6:
                    run = false;
                    break;
            }
            if (run != false) {
                menuDisplay();
            }
        }

        System.out.println("Thank you for using this program! @zainsra.com");
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

    public static void menuDisplay() {
        System.out.println("-- Welcome to Zain's Network --\n\n Please choose one of the options to proceed (1-5): ");
        System.out.println("\t\t 1. Normal Convergence ");
        System.out.println("\t\t 2. Slow Convergence with Split");
        System.out.println("\t\t 3. Slow Convergence without Split");
        System.out.println("\t\t 4. Check an Iteration");
        System.out.println("\t\t 5. Output Network State to File");
        System.out.println("\t\t 6. Exit");
    }

    //Function to read input file in case of normal convergence
    public static void readNetworkFile(String filename) {

        //File format should be
        //1. First line showing Total Number of Nodes
        //2. Consecutive lines follows the pattern: SourceNode -- LinkCost -- DestinationNode
        Path path = Paths.get(filename);
        Scanner scanner;
        try {
            scanner = new Scanner(path);
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
            //Clear Iterations from previous (normal/slow convergence run)
            iterations.clear();
            totalIterations = 0;
            //Initial Iteration -- Containing Initial State of the Network
            Iteration it = new Iteration(totalIterations, nodes, totalNodes + 1);
            iterations.add(it);
        } catch (IOException ex) {
            Logger.getLogger(Networks.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Function to read input file in case of slow convergence
    public static void readSNetworkFile(String filename) {

        //File format should be
        //1. First line showing Total Number of Nodes
        //2. Consecutive lines follows the pattern: SourceNode -- LinkCost -- DestinationNode 
        //3. OR Iteration~3 which states that on 3rd Iteration crash the node given in the following line
        //4. Crash:1 means to crash Node1 in the network
        Path path = Paths.get(filename);
        Scanner scanner;
        try {
            scanner = new Scanner(path);
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
            Logger.getLogger(Networks.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void writeToFile(String filename) {
        try {
            PrintWriter fout = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
            for (int i = 0; i < iterations.size(); i++) {
                String line = iterations.get(i).toString();
                fout.println(line);
            }
            fout.flush();
            fout.close();
        } catch (IOException ex) {
            Logger.getLogger(Networks.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void initializeNodeArray() {
        for (int i = 0; i < totalNodes + 1; i++) {
            Node temp = new Node(i, totalNodes + 1);
            nodes.add(temp);
        }
    }
}
