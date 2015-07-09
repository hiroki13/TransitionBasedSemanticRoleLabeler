/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import main.Mode;


/**
 *
 * @author hiroki
 */
public class ParameterChecker {

    final Mode mode;
    final OptionParser optionparser;
    
    public ParameterChecker(Mode mode)
    {
        this.mode = mode;
        this.optionparser = mode.optionparser;
    }
    
    public void check()
    {
        final String modeselect = mode.modeselect;
        
        if ("train".equals(modeselect)) {
            setTrainFile();
            setTestFile();
            setEvalFile();
            setOutputFile();
        }

        else if ("test".equals(modeselect)) {
            setTestFile();
            setModelFile();
            setOutputFile();
        }

        else if ("statistics".equals(modeselect)) {
            setTrainFile();
            setTestFile();
            setEvalFile();
        }

        else System.out.println("Enter -mode X");        
 
    }
    
    public void setTrainFile()
    {
        if(mode.train) mode.trainfile = optionparser.getString("train");
        else {
            System.out.println("Enter -train filename");
            System.exit(0);
        }
    }
    
    public void setTestFile()
    {
        if(mode.test) mode.testfile = optionparser.getString("test");
        else {
            System.out.println("Enter -test filename");
            System.exit(0);
        }
    }

    public void setEvalFile()
    {
        if(mode.eval) mode.evalfile = optionparser.getString("eval");
    }
    
    public void setOutputFile()
    {
        if(mode.output) mode.outfile = optionparser.getString("output");
    }
    
    public void setModelFile()
    {
        if(mode.model) mode.modelfile = optionparser.getString("model");
        else {
            System.out.println("Enter -model filename");
            System.exit(0);
        }        
    }

}
