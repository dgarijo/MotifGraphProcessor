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
package MainGraphProcessingScripts.PARSEMIS;

import Factory.Loni.LoniTemplate2Graph;
import IO.Exception.CollectionWriterException;
import IO.Formats.PARSEMIS.CollectionWriterPARSEMIS;
import Static.Configuration;
import java.io.File;

/**
 * Script to transform LONI templates to Parsemis
 * @author Daniel Garijo
 */
public class STEP1aLONITemplates2Parsemis {
    public static void main(String [] args) throws CollectionWriterException{
        System.out.println("\n Starting script for writing LONI collection");
        String loniDatasetFolder = "LONI_dataset\\";
        File f = new File(loniDatasetFolder);
        LoniTemplate2Graph test = new LoniTemplate2Graph(loniDatasetFolder);
        if(f.isDirectory()){
            File[] files = f.listFiles();
            for(int i=0;i<files.length;i++){
                test.transformToGraph(files[i].getName());
            }
            CollectionWriterPARSEMIS writer = new CollectionWriterPARSEMIS();
            writer.writeReducedGraphsToFile(test.getGraphCollection(),Configuration.getPARSEMISInputPath()+ "LoniFullDataset.lg", null);
        }
    }
    
}
