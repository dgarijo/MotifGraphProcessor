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
import DataStructures.Fragment;
import IO.Exception.FragmentReaderException;
import IO.FragmentCollectionReader;
import Static.GeneralConstants;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class to Read the substructures found by SUBDUE, along with their occurrences.
 * Note that the graph file and the occurrences file are different files.
 * The output is a list : structure ID, number of occurrences, graph of dependences
 * between processes (a subdue graph), list of structure IDs included in the current
 * one (dependences)
 * @author Daniel Garijo
 */
public class FragmentCollectionReaderSUBDUE extends FragmentCollectionReader {
    private String occurrencesFile;


    /**
     * Main constructor method. Initialized the fragment hashmap.
     * The key is the substructure name (e.g., SUB_1 and the value is the 
     * fragment itself
     */
    public FragmentCollectionReaderSUBDUE(String resultFile,String occurrencesFile) {
        this.occurrencesFile = occurrencesFile;
        this.resultFile = resultFile;
        this.finalResults = new HashMap<String, Fragment>();
    }
    
    
    /**
     * Method that given a file with subdue graphs, processes it and represents
     * it in the form of "final result"
     * @param graphFile 
     */
    @Override
    protected void processResultFile(String graphFile)throws FragmentReaderException{
        try{
            FileInputStream fstream = new FileInputStream(graphFile);            
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            int i = 0;
            ArrayList<String> URIs = null; 
            HashMap<String, GraphNode> nodes = null;
            String[][] adjacencyMatrix = null;
            String [] currLine;
            ArrayList<Fragment> includedSubStructures = null;
            while ((strLine = br.readLine()) != null)   {
                if(strLine.equals("S")){
                    //new structure, we store the previous one
                    if(URIs!=null){
                        i++;
                        Graph auxg = new Graph(URIs, nodes, adjacencyMatrix, GeneralConstants.SUBDUE_SUB_+i);
                        Fragment currentStructure = new Fragment(GeneralConstants.SUBDUE_SUB_+i, 0,auxg,includedSubStructures,isMultiStepStructure(auxg, includedSubStructures));
                        finalResults.put(GeneralConstants.SUBDUE_SUB_+i, currentStructure) ;                        
                    }
                    //we reinicialize all the structures
                    URIs = new ArrayList<String>();
                    nodes = new HashMap<String, GraphNode>();
                    adjacencyMatrix = null; // we can only create it once we have all the nodes
                    includedSubStructures = new ArrayList<Fragment>();
                }
                else{
                    //record vertex and edges.
                    if (strLine.startsWith("v")){
                        //add vertex to the uris and hashmap. Since we dont know
                        //URIs (we just work with the types), we use ids.
                        //we can't use the types as uris, otherwise the hashmap
                        //would be messed up (more than one node is of the same
                        //type
                        currLine = strLine.split(" ");
                        String id = currLine[1].trim();
                        String type = currLine[2].trim();
                        //System.out.println("v "+id+",type "+type);
                        URIs.add(id);
                        //if it is a substructure, save it
                        if (type.contains(GeneralConstants.SUBDUE_SUB_)){
                            //&&!includedSubStructures.contains(finalResults.get(type))
                            //I commented the sentence above because a substructure may contain
                            //2 pointers to the same substructure. In that case
                            //we must take it into consideration for its size.
                            includedSubStructures.add(finalResults.get(type));                            
                        }
                        nodes.put(id, new GraphNode(id, type, Integer.parseInt(id)));
                    }else if (strLine.startsWith("d")){
                        //add dependences.
                        if(adjacencyMatrix == null){
                            adjacencyMatrix = new String[URIs.size()+1][URIs.size()+1];//we ignore the [0][0] lines
                        }
                        //retrieve positions: d row column relation
                        currLine = strLine.split(" ");
                        adjacencyMatrix[Integer.parseInt(currLine[1])][Integer.parseInt(currLine[2])] = currLine[3];
                        //System.out.println(nodes.get(currLine[1]).getType()+" "+currLine[3]+" "+nodes.get(currLine[2]).getType());
                                                
                    }
                    
                }

            }
            //the last result must be added as well.
            if(URIs!=null){
                i++;
                Graph auxg = new Graph(URIs, nodes, adjacencyMatrix, GeneralConstants.SUBDUE_SUB_+i);
                Fragment currentStructure = new Fragment(GeneralConstants.SUBDUE_SUB_+i, 0,auxg,includedSubStructures,this.isMultiStepStructure(auxg, includedSubStructures));
                finalResults.put(GeneralConstants.SUBDUE_SUB_+i, currentStructure) ;                        
            }
            in.close();                
        }catch(Exception e){
            System.err.println("Error while reading the file "+e.getMessage());
            throw new FragmentReaderException("Error while reading the file "+e.getMessage(), e);
        }
        
    }
    
    /**
     * Method that given a file it looks for the occurrences of each substructure.
     * The file identifies substructures like this: SUB_n -> number
     * @param occFile 
     */
    private void processOccurrencesFile(String occFile) throws FragmentReaderException{
        FileInputStream fstream =null;
        DataInputStream in = null;
        try{
            fstream = new FileInputStream(occFile);            
            in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;            
            while ((strLine = br.readLine()) != null)   {
                String[] idAndOccurrence = strLine.split("->");
                String id = idAndOccurrence[0].trim();
                int number = Integer.parseInt(idAndOccurrence[1].trim());
                if(!finalResults.containsKey(id)){                    
                    finalResults.put(id, new Fragment());
                }
                finalResults.get(id).setNumberOfOccurrences(number);
            }
            in.close();
            fstream.close();
        }catch(Exception e){
            System.err.println("Error while processing the file "+e.getMessage());
            throw new FragmentReaderException("Error while reading the file "+e.getMessage(), e);
        }
    }

    @Override
    public HashMap<String, Fragment> getFragmentCatalogAsHashMap() throws FragmentReaderException {
        if(resultFile==null || occurrencesFile==null)
            throw  new FragmentReaderException("Result file or occurrences file not provided");
        processResultFile(resultFile);
        processOccurrencesFile(occurrencesFile);
        return finalResults;
    }
    

}
