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
package PostProcessing.Formats.SUBDUE;
import DataStructures.Fragment;
import DataStructures.GraphNode.GraphNode;
import PostProcessing.FragmentToSPARQLQuery;
import Static.GeneralConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class retrieves fragment bindings from a repository of TEMPLATES. 
 * Basically, what this does is to transform a fragment into a SPARQL query,
 * which retrieves the fragment itself. The result is an arrayList of hashmaps.
 * Each hashmap will have a variable and its URI binding. Each hashmap represents
 * a binding of the current fragment.
 * @author Daniel Garijo
 */
public class FragmentToSPARQLQueryTemplateSUBDUE extends FragmentToSPARQLQuery {
    int currentVariable;//counter to determine which binding are we dealing with
    HashMap<String, String> bindingsToVariables;//hashmap to know to which 
    //variable we referr to.
    
    /**
     * Default Constructor
     */
    public FragmentToSPARQLQueryTemplateSUBDUE() {
        currentVariable = 0;        
    }

    /**
     * attribute currentVariable getter
     * @return the current variable number
     */
    public int getCurrentVariable() {
        return currentVariable;
    }
    
    /**
     * Each time a new binding is created, we need to increment the variable.
     * @return increments the current variable and returns it.
     */
    private int getNextVariable(){
        currentVariable++;
        return currentVariable;
    }
    
    /**
     * Method that resets the current variable to 0
     */
    private void resetCurrentVariable(){
        currentVariable = 0;
    }
    
    /**
     * Method that returns the variable to URIs bindings given a fragment.
     * For example, URI 1 corresponds to var ?x1.
     * @param f Fragment of which we want to know the bindings
     */
    private void getBindingsForVariables(Fragment f){
        bindingsToVariables = createBindingsForVariables(f);                        
    }
    
    /**
     * Recursive method to determine which variable are assigned to which URIs.
     * @param f input fragment
     * @return Hashmap with the bindings (URI/varname)
     */
    private HashMap<String,String> createBindingsForVariables(Fragment f){
        ArrayList <String> uris = f.getDependencyGraph().getURIs();
        HashMap<String,GraphNode> nodes = f.getDependencyGraph().getNodes();
        Iterator it = uris.iterator();
        HashMap<String,String> bindingForVars = new HashMap<String, String>();
        while(it.hasNext()){
            //get type. If SUB, get hashmap and copy it. Other wise just add
            //the types to the current hashmap
            String currentURI = (String)it.next();
            String currentType = nodes.get(currentURI).getType();
            if(currentType.contains("SUB_")){
                HashMap<String,String> aux = createBindingsForVariables(getPointerToSubstructure(currentType, f));
                Iterator<String> it2 = aux.keySet().iterator();
                while(it2.hasNext()){
                    String nextKey = it2.next();
                    bindingForVars.put(nextKey, aux.get(nextKey));
                }
            }else{
                bindingForVars.put(f.getStructureID()+"_"+currentURI, "?x"+getNextVariable());
            }
        }
        return bindingForVars;        
    }
    
       
       
    @Override
    public String createQueryFromFragment(Fragment f, String templateURI){
        int numberOfVariables = f.getSize();
        this.getBindingsForVariables(f);
        String query="SELECT distinct ";
        for(int i=1;i<=numberOfVariables;i++){
            query+="?x"+i+" ";
        }
        query+="WHERE{\n";
        query+=transformDependencyMatrixToQuery(f, templateURI);
        query+=addFilterForVariables(numberOfVariables);
        query+="}";
        resetCurrentVariable();
        return query;
    }
    
    /**
     * Method to add a filter for each pair of variables with the same type.
     * We know they ought to have ad ifferent binding, so the filter is necessary
     * @return the String with the filter over the variables.
     */
    private String addFilterForVariables(int numberOfVars){
//        add the method only for variables with the same type.
        String filterForVariables = "FILTER (";
        for(int i = 1; i<=numberOfVars;i++){
            for(int j = i+1; j<=numberOfVars; j++){
                filterForVariables+="?x"+i+" != ?x"+j;
                if(i+1!=numberOfVars || j!=numberOfVars){
                    filterForVariables+=" && ";                            
                }
            }            
        }
        //FILTER (?x6 != ?x1 && ?x6!= ?x2 && ?x6!=?x3 && ?x6!=?x4 && ?x6!=?x5
        filterForVariables += ")";
        return filterForVariables;
    }
    
    
    /**
     * Method that given a fragment and a template, transforms the dependency
     * matrix to an appropriate SPARQL query, in order to see whether it has 
     * been found or not.
     * @param f candidate fragment
     * @param templateURI URI of the template where we want to see if f was 
     * found
     * @return the SPARQL query to test the previous goal.
     */
    private String transformDependencyMatrixToQuery(Fragment f, String templateURI){
        String[][] adjMatrix = f.getDependencyGraph().getAdjacencyMatrix();
        HashMap<String,GraphNode> graphNodes = f.getDependencyGraph().getNodes();
        ArrayList<String> graphURIs = f.getDependencyGraph().getURIs();
        ArrayList <String> uris = f.getDependencyGraph().getURIs();
        String innerQuery="";
        int currentVarNumber;        
        for (int i=1;i< adjMatrix.length;i++){
            for(int j=1;j<adjMatrix.length;j++){
                if(adjMatrix[i][j]!=null && adjMatrix[i][j].equals(GeneralConstants.INFORM_DEPENDENCY)){ 
                    String currentTypeI = graphNodes.get(graphURIs.get(i-1)).getType();
                    String currentTypeJ = graphNodes.get(graphURIs.get(j-1)).getType();
                    currentVarNumber = getNextVariable();
                    if(currentTypeI.contains("SUB_")&&(currentTypeJ.contains("SUB_"))){                        
                        //2 bucles for con las vars como se ha hecho abajo
                        Fragment substructureI = getPointerToSubstructure(currentTypeI, f);        
                        Fragment substructureJ = getPointerToSubstructure(currentTypeJ, f);
                        innerQuery+= transformDependencyMatrixToQuery(substructureI, templateURI);
                        innerQuery+= transformDependencyMatrixToQuery(substructureJ, templateURI);
                        
                        ArrayList<String> auxListI = getListOfURIsToConnectFragment(substructureI);
                        ArrayList<String> auxListJ = getListOfURIsToConnectFragment(substructureJ);
                        Iterator<String> itI = auxListI.iterator();                        
                        while(itI.hasNext()){
                            Iterator<String> itJ = auxListJ.iterator();
                            innerQuery+="{\n"                                      
                                      +bindingsToVariables.get(itI.next())+" <http://www.opmw.org/ontology/uses> ?use"+currentVarNumber+".\n"
                                      +"?use"+currentVarNumber+" <http://www.opmw.org/ontology/isGeneratedBy> "+bindingsToVariables.get(itJ.next())+".\n";
                            innerQuery+="}\n";
                            while(itJ.hasNext()){
                                innerQuery+="UNION\n{\n"                                      
                                      +bindingsToVariables.get(itI.next())+" <http://www.opmw.org/ontology/uses> ?use"+currentVarNumber+".\n"
                                      +"?use"+currentVarNumber+" <http://www.opmw.org/ontology/isGeneratedBy> "+bindingsToVariables.get(itJ.next())+".\n";
                                innerQuery+="}\n";
                            }
                        }
                        
                    }else 
                    if (currentTypeI.contains("SUB_")){         
                        Fragment subStructure = getPointerToSubstructure(currentTypeJ, f);        
                        innerQuery+= transformDependencyMatrixToQuery(subStructure, templateURI);
                        innerQuery+= addStep(bindingsToVariables.get(f.getStructureID()+"_"+uris.get(j-1)), currentTypeJ, templateURI);
                        innerQuery+= "?gen"+currentVarNumber+" <http://www.opmw.org/ontology/isGeneratedBy> "+bindingsToVariables.get(f.getStructureID()+"_"+uris.get(j-1))+".\n";
                        
                        ArrayList<String> auxList = getListOfURIsToConnectFragment(subStructure);
                        Iterator<String> it = auxList.iterator();
                        if(it.hasNext()){
                        //for every possible processor in the subfragment, we ask if it is connected
                        //to the current variable. The first UNION is outside, so we process it first.
                            innerQuery+="{\n"+bindingsToVariables.get(it.next())+" <http://www.opmw.org/ontology/uses> ?use"+currentVarNumber+".\n";
                            innerQuery+="}\n";
                            while(it.hasNext()){
                                innerQuery+="UNION\n{\n";
                                innerQuery+=bindingsToVariables.get(it.next())+" <http://www.opmw.org/ontology/uses> ?use"+currentVarNumber+".\n";
                                innerQuery+="}\n";
                            }
                        }
                    }else
                    if (currentTypeJ.contains("SUB_")){
                        Fragment subStructure = getPointerToSubstructure(currentTypeJ, f);        
//                        int oldVarNumber = currentVarNumber+1;
                        innerQuery+= transformDependencyMatrixToQuery(subStructure, templateURI);
                        innerQuery+= addStep(bindingsToVariables.get(f.getStructureID()+"_"+uris.get(i-1)), currentTypeI, templateURI);
                        innerQuery+= bindingsToVariables.get(f.getStructureID()+"_"+uris.get(i-1))+" <http://www.opmw.org/ontology/uses> ?use"+currentVarNumber+".\n";
                        
                        ArrayList<String> auxList = getListOfURIsToConnectFragment(subStructure);
                        Iterator<String> it = auxList.iterator();
                        if(it.hasNext()){
                        //for every possible processor in the subfragment, we ask if it is connected
                        //to the current variable. The first UNION is outside, so we process it first.
                            innerQuery+="{\n?use"+currentVarNumber+" <http://www.opmw.org/ontology/isGeneratedBy> "+bindingsToVariables.get(it.next())+".\n";
                            innerQuery+="}\n";
                            while(it.hasNext()){
                                innerQuery+="UNION\n{\n";
                                innerQuery+="?use"+currentVarNumber+" <http://www.opmw.org/ontology/isGeneratedBy> "+bindingsToVariables.get(it.next())+".\n";
                                innerQuery+="}\n";
                            }
                        }
                    }else{
                        innerQuery+=addStep(bindingsToVariables.get(f.getStructureID()+"_"+uris.get(i-1)),currentTypeI, templateURI);
                        innerQuery+=bindingsToVariables.get(f.getStructureID()+"_"+uris.get(i-1)) +" <http://www.opmw.org/ontology/uses> ?use"+currentVarNumber+".\n";
                        innerQuery+="?use"+currentVarNumber+" <http://www.opmw.org/ontology/isGeneratedBy> "+bindingsToVariables.get(f.getStructureID()+"_"+uris.get(j-1))+".\n";                        
                        innerQuery+=addStep(bindingsToVariables.get(f.getStructureID()+"_"+uris.get(j-1)),currentTypeJ, templateURI);
                    }
                }
            }
        }
        return innerQuery;
    }
    
    /**
     * Method that given a fragment, it returns an ArrayList with the URIs
     * of all the steps included on it. It does NOT return SUBs, it will return 
     * the URIs of the processes included in the SUB (to which the connection
     * of a fragment has to be tried to)
     * @param f
     * @return 
     */
    private ArrayList<String> getListOfURIsToConnectFragment(Fragment f){
        ArrayList <String> uris = f.getDependencyGraph().getURIs();
        HashMap<String,GraphNode> nodes = f.getDependencyGraph().getNodes();
        Iterator it = uris.iterator();
        ArrayList<String> uriList= new ArrayList<String>();
        while(it.hasNext()){
            //get type. If SUB, get hashmap and copy it. Other wise just add
            //the types to the current hashmap
            String currentURI = (String)it.next();
            String currentType = nodes.get(currentURI).getType();
            if(currentType.contains("SUB_")){
                ArrayList<String> aux = getListOfURIsToConnectFragment(getPointerToSubstructure(currentType, f));
                Iterator<String> it2 = aux.iterator();
                //copy the values from the substructure within
                while(it2.hasNext()){
                    uriList.add(it2.next());
                }
            }else{
                uriList.add(f.getStructureID()+"_"+currentURI);
            }
        }
        return uriList;
    }
    
    /**
     * Method to add a type to the query.
     * @param varNumber
     * @param type
     * @param templateURI
     * @return 
     */
    private String addStep(String varNumber, String type, String templateURI){
        return varNumber+" a <"+type+">.\n"
               +varNumber+" <http://www.opmw.org/ontology/isStepOfTemplate> <"+templateURI+">.\n";
    }
    
    /**
     * Given the name of a substructure, this method restrieves it from a 
     * fragment. This is used for searching in fragments linked by others.
     * @param substructure the name of the substructure you are looking into,
     * e.g., SUB_1
     * @param fr fragment on which we are going to search the substructure
     * @return pointer to the substructure object
     */
    private Fragment getPointerToSubstructure(String substructure,  Fragment fr){        
        Fragment structureToExplore = null;
        boolean found = false;
        //get the substructure on which we are going to perform the search
        Iterator<Fragment> it = fr.getListOfIncludedIDs().iterator();
        while(!found && it.hasNext()){
            structureToExplore = (Fragment)it.next();
            if(substructure.equals(structureToExplore.getStructureID())){
                found = true;
            }
        }
        return structureToExplore;
    }
    
    
}