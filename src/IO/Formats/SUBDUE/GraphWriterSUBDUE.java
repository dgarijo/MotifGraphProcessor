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
package IO.Formats.SUBDUE;

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
 *          d 1 2 used (1 used 2)
 * @author Daniel Garijo
 */
public class GraphWriterSUBDUE extends GraphWriter{
    
    /**
    ** @@TO DO: unify both writtings. The nodes are written in a very similar way for both
    */

            
    @Override
    public void writeFullGraphToFile(Graph g, BufferedWriter out, int nodeCount, HashMap replacements)throws IOException{
        out.write("%Processing: "+g.getName());
        out.newLine();
        System.out.println("%Writing: "+g.getName());
        //write down the vertex
        Iterator<String> it = g.getURIs().iterator();
        HashMap<String,GraphNode> nodes = g.getNodes();
        String[][] adjacencyMatrix = g.getAdjacencyMatrix();
        if(replacements==null){
            while (it.hasNext()){
                    String currentURI = it.next();
                    GraphNode currNode = (GraphNode) nodes.get(currentURI);
//                    System.out.println("v "+(currNode.getNumberInGraph()+nodeCount)+" "+currNode.getType());
                    out.write("v "+(currNode.getNumberInGraph()+nodeCount)+" "+currNode.getType());
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
                out.write("v "+(currNode.getNumberInGraph()+nodeCount)+" "+nodeType);
                out.newLine();
            }
        }
        
        //write down the dependencies (adjacency matrix)
        for (int i = 0; i<adjacencyMatrix.length;i++){
                for(int j = 0; j<adjacencyMatrix.length; j++){
                    if(adjacencyMatrix[i][j]!=null) {
//                        System.out.println("d "+(i+nodeCount)+" "+(j+nodeCount)+" "+adjacencyMatrix[i][j]);
                        out.write("d "+(i+nodeCount)+" "+(j+nodeCount)+" "+adjacencyMatrix[i][j]);
                        out.newLine();
                    }
                }
            }
    
    }
    
    @Override
    public void writeReducedGraphToFile(Graph g, BufferedWriter out, int nodeCount, HashMap replacements)throws IOException{
        out.write("%Processing: "+g.getName());
        out.newLine();
        System.out.println("%Writing: "+g.getName());
        Iterator<String> it = g.getURIs().iterator();
        HashMap<String,GraphNode> nodes = g.getNodes();
        String[][] adjacencyMatrix = g.getAdjacencyMatrix();
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
        //retrieve the reduced graph
        if(g.getAdjacencyMatrix()!=null){
            g.putReducedNodesInAdjacencyMatrix();
            //write down the dependencies (adjacency matrix)
            for (int i = 0; i<adjacencyMatrix.length;i++){
                    for(int j = 0; j<adjacencyMatrix.length; j++){
                        if(adjacencyMatrix[i][j]!=null) {
                            //we filter the relationships with the reduced notation (informedBy)
                            if(adjacencyMatrix[i][j].equals(GeneralConstants.INFORM_DEPENDENCY)){
    //                            System.out.println("d "+(i+nodeCount)+" "+(j+nodeCount)+" "+adjacencyMatrix[i][j]);
                                out.write("d "+(i+nodeCount)+" "+(j+nodeCount)+" "+adjacencyMatrix[i][j]);
                                out.newLine();
                            }
                        }
                    }
                }
        }
    }
}
