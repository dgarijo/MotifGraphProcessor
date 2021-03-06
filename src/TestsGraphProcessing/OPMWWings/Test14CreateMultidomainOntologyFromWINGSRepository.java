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
package TestsGraphProcessing.OPMWWings;

import Factory.Inference.CreateMultidomainTaxonomyWings;

/**
 * Test to create a multidomain taxonomy from all Wings domains and create and
 * merge it. The URL of the endpoint is needed.
 * @author Daniel Garijo
 */
public class Test14CreateMultidomainOntologyFromWINGSRepository {
    public static int testNumber = 14;
    public static boolean test(){
        System.out.println("\n\nExecuting test:"+testNumber+" create a multidomain taxonomy and print it to a file");
        try{
            CreateMultidomainTaxonomyWings c = new CreateMultidomainTaxonomyWings("http://wind.isi.edu:8890/sparql");
            c.saveMultiDomainOntologyToFile("testMultiDomainTaxonomy.owl");
            return true;
        }catch(Exception e){
            System.out.println("Error in test. Exception: "+e.getMessage());
            return false;
        }
    }
    
    public static void main(String[] args){
        if(test())System.out.println("Test "+testNumber+" OK");
        else System.out.println("Test "+testNumber+" FAILED");
    }
    
}
