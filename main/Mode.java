/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.IOException;
import java.util.ArrayList;
import predicatedisambiguator.PredicateDisambiguator;
import semanticrolelabeler.AccuracyChecker;
import io.FrameDict;
import io.OptionParser;
import io.ParameterChecker;
import io.Reader;
import io.RoleDict;
import io.Sentence;
import semanticrolelabeler.Trainer;

/**
 *
 * @author hiroki
 */
public class Mode {
    
    public OptionParser optionparser;
    public String modeselect, trainfile, testfile, evalfile, outfile, modelfile;
    public boolean train, test, eval, output, model, pd, ai, ac, core;
    public int iteration, weight_length, prune;
    public ArrayList<Sentence> trainsentence, testsentence, evalsentence;
    
    
    Mode(String[] args) throws Exception
    {
        optionparser = new OptionParser(args);
        
        if (optionparser.isExsist("mode")) modeselect = optionparser.getString("mode");
        else {
            System.out.println("Enter -mode train/test/statistics");
            System.exit(0);
        }
        
    }

    public void setParameter()
    {
        pd = optionparser.isExsist("pd");
        ai = optionparser.isExsist("ai");
        ac = optionparser.isExsist("ac");
        train = optionparser.isExsist("train");
        test = optionparser.isExsist("test");
        eval = optionparser.isExsist("eval");
        output = optionparser.isExsist("output");
        model = optionparser.isExsist("model");
        RoleDict.core = optionparser.isExsist("core");
        weight_length = optionparser.getInt("weight", 1000);
        iteration = optionparser.getInt("iter", 10);
        prune = optionparser.getInt("prune", 100000);
    }    
    
    public void execute() throws Exception
    {
        setParameter();
        ParameterChecker p_checker = new ParameterChecker(this);
        p_checker.check();
        System.out.println("\nSemantic Role Labeling START");        

        if ("train".equals(modeselect)) {
            System.out.println("\nFiles Loaded...");

            if (RoleDict.core && !ac) RoleDict.add("NULL");
            
            trainsentence = Reader.read(trainfile, false);
            testsentence = Reader.read(testfile, true);
            evalsentence = Reader.readEval(evalfile);
            
            System.out.println(String.format("Train Sents: %d\nTest Sents: %d", trainsentence.size(), testsentence.size()));
            System.out.println("Framedict: " + FrameDict.framedict.size());
            System.out.println("Roles: " + RoleDict.roledict.size());
            
//            if (pd) predicateDisambiguation();            
            if (ac) argumentClassification();            
        }        
    }
    
    private void predicateDisambiguation()
    {
        System.out.println("\nPredicate Disambiguator Learning START");        
        PredicateDisambiguator pd = new PredicateDisambiguator(weight_length);

        for (int i=0; i<iteration; ++i) {        
            System.out.println("\nIteration: " + (i+1));            
            pd.train(trainsentence);            
            System.out.println();                            
            AccuracyChecker checker = new AccuracyChecker();            
            checker.testPD(testsentence, evalsentence, pd);
            checker = null;
        }          

        weight_length = weight_length * 100;        
    }
    
    private void argumentClassification() throws IOException
    {
        System.out.println("\nArgument Classifier Learning START");
        final Trainer trainer = new Trainer(trainsentence, weight_length, prune);
        trainer.setOracleState();

        for (int i=0; i<iteration; ++i) {        
            System.out.println("\nIteration: " + (i+1));            
            trainer.train();            
            System.out.println();            

            AccuracyChecker checker = new AccuracyChecker();
            checker.test(testsentence, evalsentence, trainer.parser);

            if (i==iteration-1 && output) checker.outputAC(testsentence, outfile);                                    

            checker = null;
        }        
    }
    
    
}
