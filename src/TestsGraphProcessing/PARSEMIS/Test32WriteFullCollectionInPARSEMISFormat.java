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
package TestsGraphProcessing.PARSEMIS;

import Factory.OPMW.OPMWTemplate2GraphWings;
import IO.Formats.PARSEMIS.GraphCollectionWriterPARSEMIS;

/**
 * Test designed to write the graph collection in the parsemis format.
 * @author Daniel Garijo
 */
public class Test32WriteFullCollectionInPARSEMISFormat {
    public static int testNumber = 32;//not given a number until we complete the previous ones.
    public static boolean test(){
        try{
            System.out.println("\n\nExecuting test:"+testNumber+" Save collection in Parsemis format");
            OPMWTemplate2GraphWings tp = new OPMWTemplate2GraphWings("http://wind.isi.edu:8890/sparql");
            tp.transformDomainToGraph("TextAnalytics");
            GraphCollectionWriterPARSEMIS writer = new GraphCollectionWriterPARSEMIS();
//          writer.writeReducedGraphsToFile(tp.getGraphCollection(), "TestSaveCollectionAsReducedlGraphInFile");
            if (tp.getGraphCollection().getNumberOfSubGraphs()>1){
                writer.writeReducedGraphsToFile(tp.getGraphCollection(), "testSaveCollectionAsPARSEMISFile.lg");
                return true;
            }
            return false;
        }catch(Exception e){
            System.out.println("Test TestSaveCollectionAsPARSEMIS File case failed: "+ e.getMessage());
            return false;
        }
    }
    
    public static void main(String[] args){
        if(test())System.out.println("Test "+testNumber+" OK");
        else System.out.println("Test "+testNumber+" FAILED");
    }
}
