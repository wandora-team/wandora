package org.wandora.topicmap.undowrapper.tests;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import org.wandora.application.Wandora;
import org.wandora.application.contexts.Context;
import org.wandora.application.tools.AbstractWandoraTool;
import org.wandora.topicmap.Association;
import org.wandora.topicmap.Locator;
import org.wandora.topicmap.Topic;
import org.wandora.topicmap.TopicMap;
import org.wandora.topicmap.TopicMapException;
import org.wandora.topicmap.memory.TopicMapImpl;

/**
 *
 * @author olli
 */


public class TestRunner extends AbstractWandoraTool {
    
    private ArrayList<Test> tests;
    private Writer output;
    
    public TestRunner(){
        this.tests=new ArrayList<Test>();
        this.output=null;
    }
    
    public void addTest(Test test){
        this.tests.add(test);
    }
    
    public void setOutput(Writer out){
        this.output=out;
    }
    
    
    public void setupTests(int count){
        for(int i=0;i<count;i++){
            this.addTest(new RandomTest());
        }
    }
    
    public void runTests(){
        if(this.output==null) output=new PrintWriter(System.out);
        
        int passed=0;
        int failed=0;
        
        for(Test t : tests){
//        while(true){
//            Test t=new RandomTest();
            try{
                t.run();
                if(!t.isPassed()){
                    failed++;
                    this.output.write("FAILED ");
                }
                else {
                    this.output.write("PASSED ");
                    passed++;
                }
//                if(!t.isPassed()) {
                    this.output.write(t.getLabel()+"\n");
                    t.getMessages(output);
                    this.output.flush();
//                    break;
//                }
                
//                if((passed%100)==0) { this.output.write("PASSED "+passed+" tests\n"); this.output.flush(); }
//                if(!t.isPassed()) break;
                if(false) break;
                
            } catch(Exception e){
                try{
                    this.output.write("FAILED "+t.getLabel()+"\n");
                    PrintWriter pwriter=new PrintWriter(this.output);
                    e.printStackTrace(pwriter);
                    pwriter.flush();
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }
            }
        }

        try{
            if(failed==0) this.output.write("PASSED ALL "+passed+" tests\n");
            else {
                this.output.write("PASSED "+passed+" tests\nFAILED "+failed+" tests\n");
            }
            this.output.flush();
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
    
    
    
    @Override
    public String getName() {
        return "Undo/redo tests";
    }

    @Override
    public String getDescription() {
        return "Runs some undo and redo test cases";
    }
    
    public void execute(Wandora wandora, Context context) throws TopicMapException {
        this.setupTests(200);
        this.runTests();
    }

    
    
    public static void main(String args[]) throws Exception {
        TestRunner test=new TestRunner();
        test.setupTests(200);
        test.runTests();
    }
}
