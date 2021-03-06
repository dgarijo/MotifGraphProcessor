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
package TestsGraphProcessing.SUBDUE;

import Factory.OPMW.OPMWTemplate2GraphWings;
import IO.Formats.SUBDUE.GraphCollectionWriterSUBDUE;

/**
 * Test designed to save a collection as a full graph file. If the test does not
 * fail, the collection is retrieved and saved properly.
 * @author Daniel Garijo
 */
public class Test09SaveCollectionAsFullGraphInFile {
    public static int testNumber = 9;
    public static boolean test(){
        System.out.println("\n\nExecuting test:"+testNumber+" Saving a collection as a full graph file.");
        try{
            OPMWTemplate2GraphWings tp = new OPMWTemplate2GraphWings("http://wind.isi.edu:8890/sparql");
            tp.transformDomainToGraph("TextAnalytics");
            GraphCollectionWriterSUBDUE writer = new GraphCollectionWriterSUBDUE();
            if (tp.getGraphCollection().getNumberOfSubGraphs()>1){
                writer.writeFullGraphsToFile(tp.getGraphCollection(), "TestSaveCollectionAsFullGraphInFile");
                return true;
            }
            return false;
        }catch(Exception e){
            System.out.println("Test TestSaveCollectionAsFullGraphInFile case failed: "+ e.getMessage());
            return false;
        }
    }
    
    public static void main(String[] args){
        if(test())System.out.println("Test "+testNumber+" OK");
        else System.out.println("Test "+testNumber+" FAILED");
    }
    
}
