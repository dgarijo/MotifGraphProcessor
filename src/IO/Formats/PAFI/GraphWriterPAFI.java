/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * Copyright 2012-2013 Ontology Engineering Group, Universidad Politécnica de Madrid, Spain
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package IO.Formats.PAFI;

import DataStructures.Fragment;
import DataStructures.Graph;
import DataStructures.GraphNode.GraphNode;
import IO.GraphWriter;
import Static.GeneralConstants;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Class that extends the abstract writer to write the output in the SUBDUE format.
 * Example: v 1 NameVertex
 *          v 2 NameVertex2
 *          u 1 2 wasInformedBy (1 wasInformedBy 2)
 * 
 * PAFI requires the nodes to be in ascendent order and that the id of the first
 * vertex is smaller than the id of the second vertex
 * 
 * @author Daniel Garijo
 */
public class GraphWriterPAFI extends GraphWriter{
    
    private void writeGraph(Graph g, BufferedWriter out, int nodeCount, HashMap replacements) throws IOException {
        Iterator<String> it = g.getURIs().iterator();
        HashMap<String,GraphNode> nodes = g.getNodes();
        String[][] adjacencyMatrix = g.getAdjacencyMatrix();
//        nodeCount = -1;//PAFI must start in 0 always.
        if(replacements==null){
            while (it.hasNext()){
                    String currentURI = it.next();
                    GraphNode currNode = (GraphNode) nodes.get(currentURI);
//                    System.out.println("v "+(currNode.getNumberInGraph()+nodeCount)+" "+currNode.getType());
                    out.write("v "+(currNode.getNumberInGraph()+nodeCount)+" "+currNode.getType());//this.getCodeForNode(currNode.getType(), codes));
                    out.newLine();
            }
        }else{
            while (it.hasNext()){
                String currentURI = it.next();
                GraphNode currNode = (GraphNode) nodes.get(currentURI);
                String nodeType = currNode.getType();
                //the hashMap may NOT have the node for replacement (leave the one it has already)
                if(replacements.containsKey(currNode.getType())){
                    nodeType = (String) replacements.get(nodeType);
                }
//                System.out.println("v "+(currNode.getNumberInGraph()+nodeCount)+" "+nodeType);
                out.write("v "+(currNode.getNumberInGraph()+nodeCount)+" "+nodeType);//this.getCodeForNode(nodeType, codes));
                out.newLine();
            }
        }
        //write down the dependencies (adjacency matrix)
        //PAFI does not care about the direction, so if (i,j) or (j,i) has a dependency,
        //it has to be added as i,j        
        for (int i = 0; i<adjacencyMatrix.length;i++){
            for(int j = i; j<adjacencyMatrix.length; j++){
                if(checkDependency(adjacencyMatrix, i, j)||checkDependency(adjacencyMatrix, j, i)){
                    //all the graphs are acyclic, so we are unlikely to write the dependency twice.
                    out.write("u "+(i+nodeCount)+" "+(j+nodeCount)+" "+GeneralConstants.INFORM_DEPENDENCY);
                    out.newLine();
                }
            }                
        }  
    }
    /**
     * Each transaction starts with t.
     * Nodes come in ascendent order
     * u <id1> <id2> <id1> must be <id2>
     */
    
    @Override
    public void writeReducedGraphToFile(Graph g, BufferedWriter out, int nodeCount, HashMap replacements) throws IOException {
        out.write("t # "+g.getName());
        out.newLine();
        System.out.println("%Writing: "+g.getName());
        //retrieve the reduced graph
        g.putReducedNodesInAdjacencyMatrix();
        this.writeGraph(g, out, nodeCount, replacements);      
    }
    
     //write reducedFraphToFile with the 3 ones will result in the call to the specialization
    public void writeFragmentToFile(Fragment f, BufferedWriter out, int nodeCount, HashMap replacements) throws IOException {
        out.write("t # "+f.getStructureID()+", "+f.getNumberOfOccurrences());
        out.newLine();
        System.out.println("%Writing: "+f.getStructureID());
        writeGraph(f.getDependencyGraph(), out, nodeCount, replacements);
    }
    
    private boolean checkDependency(String[][] matrix,int i,int j){
        if(matrix[i][j]!=null && 
                matrix[i][j].equals(GeneralConstants.INFORM_DEPENDENCY)){
            return true;
        }
        return false;
    }
    
//    public static void main(String[] args){
//        try {
////            GraphWriterPAFIFormat g = new GraphWriterPAFIFormat();
////            g.writeReducedGraphToFile(null, null, 0);
//            OPMWTemplate2Graph test = new OPMWTemplate2Graph("http://wind.isi.edu:8890/sparql");
//            test.transformToGraph("http://www.opmw.org/export/resource/WorkflowTemplate/FEATUREGENERATION");
//            test.transformToGraph("http://www.opmw.org/export/resource/WorkflowTemplate/DOCUMENTCLASSIFICATION_SINGLE_");
//            Graph template = test.getGraphCollection().getGraphs().get(0);
//            GraphWriterPAFIFormat g = new GraphWriterPAFIFormat();
//            FileWriter fstream = new FileWriter("PAFI_TESTS");
//            BufferedWriter out = new BufferedWriter(fstream);
//            g.writeReducedGraphToFile(template, out, 0, null);
//            template = test.getGraphCollection().getGraphs().get(1);
//            g.writeReducedGraphToFile(template, out, 0, null);
//            out.close();
////            g.writeReducedGraphToFile(null, null, 0, null);
//        } catch (IOException ex) {
//            System.out.println("Error");
//        }
//    }
    
}
