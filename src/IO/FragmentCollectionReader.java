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
package IO;
import DataStructures.Fragment;
import DataStructures.Graph;
import IO.Exception.FragmentReaderException;
import Static.GeneralConstants;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Abstract class to read fragments from the results produced any algorithm
 * to find them among a dataset (like SUBDUE).
 * @author Daniel Garijo
 */
public abstract class FragmentCollectionReader {
    //will be initialized on the subclasses
    protected HashMap<String,Fragment> finalResults;
    protected String resultFile;
    
    /**
     * Method to process the result file with the fragments and transform them 
     * into the hashmap
     * @param graphFile 
     */
    protected abstract void processResultFile(String graphFile)throws FragmentReaderException;
    
    /**
     * Method to process the resultFile and the occurrencesFile together.
     * @return 
     */
    public abstract HashMap<String,Fragment> getFragmentCatalogAsHashMap()throws FragmentReaderException;
    
    /**
     * Method to obtain the fragment results as a hashmap
     * @return
     */
    public ArrayList<Fragment> getFragmentCatalogAsAnArrayList() throws FragmentReaderException{
        if(finalResults==null || finalResults.isEmpty()){
           finalResults =  getFragmentCatalogAsHashMap();
        }
        //transform the hashmap to an arrayList of fragments and return it.
        return new ArrayList<Fragment>(finalResults.values());
        
    }
    
    /**
     * Method to determine whether a structure is multi step or not.
     * 
     * Note: we can not do comparisons with the type directly because we do not
     * know which format will the URIs have (e.g., we can not ask for the number /dc/
     * as in Wings.
     * 
     * @param URIs
     * @param nodes
     * @param adjacencyMatrix
     * @return 
     */
    protected boolean isMultiStepStructure(Graph g, ArrayList includedSubstructures) {
        /*a structure is meaningful IF:
         * 1- It has at least two steps: 
         *      if the adjacency matrix has 2 inform depedencies
         * 2- It has a step but has a dependency on a graph with a step
         * 3- It has no steps but links two structures with at least one step
         */
        //if we only have one URI, we are done: it's not meaningful
        if(g.getNumberOfNodes()<=1)return false;
        //if the graph is not reduced, we reduce it
        g.putReducedNodesInAdjacencyMatrix();
        int numberOfDependencies = getNumberOfInformDependencies(g.getAdjacencyMatrix());
        numberOfDependencies += getNumberOfDependenciesFromIncludedStructures(includedSubstructures);
//        System.out.println("number of dependencies: "+numberOfDependencies);
        return (numberOfDependencies>0);        
    }
    
    /**
     * Method to retrieve the number of inform dependencies in the current 
     * fragment. 
     * @param matrix adjacency matrix
     * @return number of inform dependencies
     */
    protected int getNumberOfInformDependencies(String[][] matrix){
        int dependencies = 0;
        if(matrix==null)return 0;
        for(int i = 0; i< matrix[0].length;i++){
            for(int j = 0; j<matrix[0].length;j++){
                if((matrix[i][j]!=null)&&(matrix[i][j].equals(GeneralConstants.INFORM_DEPENDENCY)))
                    dependencies ++;
            }
        }
        return dependencies;
    }
    
    /**
     * Methos that returns the number of dependencies of its included substructures.
     * @param substructureResults
     * @return number of dependencies of its included substructures
     */
    protected int getNumberOfDependenciesFromIncludedStructures(ArrayList<Fragment> substructureResults){
        int dependencies = 0;
        for(int i=0; i<substructureResults.size();i++){
            //for each dependency, we check if it has other dependencies.
            Fragment currentR = substructureResults.get(i);
            ArrayList<Fragment> structureDependencyList = currentR.getListOfIncludedIDs();
            if(!structureDependencyList.isEmpty()){
                dependencies+=getNumberOfDependenciesFromIncludedStructures(structureDependencyList);
            }
            dependencies+=getNumberOfInformDependencies(currentR.getDependencyGraph().getAdjacencyMatrix());
        }
        
        return dependencies;
    }
    
}
