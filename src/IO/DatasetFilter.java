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

import DataStructures.Graph;
import DataStructures.GraphCollection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Class that given a GraphCollection, it removes duplicate graphs.
 * @author Daniel Garijo
 */
public class DatasetFilter {
    
    public static GraphCollection removeDuplicates(GraphCollection g){
        GraphCollection filteredCollection = new GraphCollection();
        Iterator<Graph> it = g.getGraphs().iterator();
        boolean repeated;
        while (it.hasNext()){
            Graph currentG = it.next();
            Iterator<Graph> it2 = filteredCollection.getGraphs().iterator();
            repeated = false;
            while(it2.hasNext()&&!repeated){
                Graph graphToCompare = it2.next();
                if(currentG.equalsGraph(graphToCompare)){
                    System.out.println(currentG.getName()+ " is equal to "+ graphToCompare.getName());
                    repeated = true;
                }
            }
            if(!repeated){
                filteredCollection.addSubGraph(currentG);
            }
        }
        return(filteredCollection);
    }
    
}
