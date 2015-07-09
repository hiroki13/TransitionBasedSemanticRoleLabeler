/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticrolelabeler;

import feature.FeatureExtractor;
import io.RoleDict;
import io.Sentence;
import io.Token;
import java.util.ArrayList;
import learning.Classifier;

/**
 *
 * @author hiroki
 */
public class BaseParser extends Parser{
    
    public BaseParser(final Classifier c, final int weight_length, final int prune)
    {
        this.classifier = c;
        this.weight_length = weight_length;
        this.feature_extracter = new FeatureExtractor(weight_length);
        this.prune = prune;
    }
    
    @Override
    public void train(final ArrayList<Sentence> sentencelist)
    {
        correct = 0.0f;
        total = 0.0f;

        for (int i=0; i<sentencelist.size(); ++i) {
            final Sentence sentence = sentencelist.get(i);

            if (feature_extracter.g_cache.size() < i+1)                    
                feature_extracter.g_cache.add(new String[sentence.preds.length][sentence.size()][]);

            if (i%1000 == 0 && i != 0) System.out.print(String.format("%d ", i));                                

            decode(sentence);

            if (i==prune) break;
                        
        }
        
        System.out.println("\tCorrect: " + correct);                        
        System.out.println("\tTotal: " + total);                        
        System.out.println("\tAccuracy: " + correct/total);                        
    }
    
    private void decode(final Sentence sentence)
    {
        state = new State(sentence);

        for (int p=1; p<state.tokens.size(); ++p) {
            final Token prd = state.tokens.get(p);
            
            if (!"Y".equals(prd.fillpred)) continue;

            state.initiateList(p);
            transition(prd);
        }        
    }
    
    public void transition(final Token prd)
    {
        while (state.list1.size()>0 || state.list4.size()>0) {        
            final Token arg1 = state.tokens.get(state.list1.get(state.list1.size()-1));            
            final Token arg2 = state.tokens.get(state.list2.get(0));                

            final int[] p_feature = extractFeature(state.sentence, prd);            
            final int[] a1_feature = extractFeature(state.sentence, arg1, prd);            
            final int[] a2_feature = extractFeature(state.sentence, arg2, prd);
                        
            final int[] features1 = concatinateFeatures(p_feature, a1_feature);            
            final int[] features2 = concatinateFeatures(p_feature, a2_feature);
            
            final int best_action = bestAction(features1, features2);                        
            state.transition(best_action);            
        }
    }
    
    private int bestAction(final int[] features1, final int[] features2)
    {
        int best_action = -1;
        float best_score = -1000000.0f;
        
        for (int i=0; i<4; ++i) {
            final float score;
            
            if (i < 2) score = calcScore(features1, i);
            else score = calcScore(features2, i);
            
            if (score > best_score) {
                best_score = score;
                best_action = i;
            }
        }
        
        return best_action;
    }
    
    private float calcScore(final int[] feature, final int label)
    {
        return classifier.calcScore(feature, label);
    }
    
    private int[] concatinateFeatures(final int[] feature1, final int[] feature2)
    {
        final int[] features = new int[feature1.length+feature2.length];        
        System.arraycopy(feature1, 0, features, 0, feature1.length);        
        System.arraycopy(feature2, 0, features, feature1.length, feature2.length);
        return features;
    }
        
    @Override
    public void test(final ArrayList<Sentence> testsentencelist)
    {
        time = (long) 0.0;
        
        for (int i=0; i<testsentencelist.size(); i++){
            Sentence testsentence = testsentencelist.get(i);
            testsentence.initializePapred();
            
            final int[] preds = testsentence.preds;
            
            if (feature_extracter.g_cache.size() < i+1)
                feature_extracter.g_cache.add(new String[testsentence.preds.length][testsentence.size()][]);
            
            if (testsentence.preds.length == 0) continue;

            for (int prd_i=0; prd_i<preds.length; ++prd_i) {
                final Token pred = testsentence.tokens.get(preds[prd_i]);
                ArrayList<Integer> arguments = pred.arguments;
                
                for (int arg_i=0; arg_i<arguments.size(); ++arg_i) {
                    final Token arg = testsentence.tokens.get(arguments.get(arg_i));

                    long time1 = System.currentTimeMillis();
                    final int[] feature = feature_extracter.extractFirstOrdFeature(testsentence, prd_i, arg_i);
                    final int label = decode(pred, feature);
                    arg.apred[prd_i] = label;
                    long time2 = System.currentTimeMillis();                    
                    time += time2 - time1;
                }
                
            }
            
            if (i%100 == 0 && i != 0)
                System.out.print(String.format("%d ", i));            
        }

    }    

    
    @Override
    public void eval(final ArrayList<Sentence> testsentencelist, final ArrayList<Sentence> evalsentencelist)
    {
        correct = 0.0f;
        p_total = 0.0f;
        r_total = 0.0f;

        for (int i=0; i<evalsentencelist.size(); i++){
            final Sentence evalsentence = evalsentencelist.get(i);
            final Sentence testsentence = testsentencelist.get(i);
            final int[] preds = evalsentence.preds;
            
            if (evalsentence.preds.length == 0) continue;

            for (int prd_i=0; prd_i<preds.length; ++prd_i) {
                final Token o_pred = evalsentence.tokens.get(preds[prd_i]);
                final ArrayList<Integer> arguments = o_pred.arguments;
                r_total += arguments.size();

                for (int arg_i=0; arg_i<arguments.size(); ++arg_i) {
                    final Token o_arg = evalsentence.tokens.get(arguments.get(arg_i));
                    final Token arg = getArg(testsentence, prd_i, o_arg);
                    final int o_label = o_arg.apred[prd_i];
                    final int label;
                    
                    if (arg != null) label = arg.apred[prd_i];
                    else label = -1;

                    if (arg == null) continue;
                    
                    if (label > -1 && o_label == label) correct += 1.0;
                }

                final Token pred = testsentence.tokens.get(preds[prd_i]);
                p_total += pred.arguments.size();                
                
            }
        }

        float p = correct/p_total;
        float r = correct/r_total;
        System.out.println("\n\tTest Correct: " + correct);
        System.out.println("\tTest R_Total: " + r_total);
        System.out.println("\tTest P_Total: " + p_total);
        System.out.println("\tTest Precision: " + p);
        System.out.println("\tTest Recall: " + r);
        System.out.println("\tTest F1: " + (2*p*r)/(p+r));
        System.out.println("\tTest Speed: " + time);                
    }
    
    private Token getArg(final Sentence testsentence, final int prd_i, final Token o_arg)
    {
        final ArrayList<Integer> arguments = testsentence.tokens.get(testsentence.preds[prd_i]).arguments;
        for (int i=0; i<arguments.size(); ++i) {
            final Token arg = testsentence.tokens.get(arguments.get(i));
            
            if (arg.id == o_arg.id) return arg;
        }
        
        return null;
    }

    
    public int decode(final Token pred, final int[] feature)
    {
        final ArrayList<Integer> possible_roles = RoleDict.rolearray;
        int best_role = -1;
        float best_score = -1000000.0f;
        
        for (int i=0; i<possible_roles.size(); ++i) {
            final int role = possible_roles.get(i);
            final float score = calcScore(feature, role);
            
            if (score > best_score) {
                best_score = score;
                best_role = role;
            }
        }
        
        return best_role;
    }
    
    public int[] extractFeature(final Sentence sentence, final Token prd)
    {
        return feature_extracter.extractFeature(sentence, prd);
    }
            
    public int[] extractFeature(final Sentence sentence, final Token arg, final Token prd)
    {
        return feature_extracter.extractFeature(sentence, arg, prd);
    }
            
    public String[] instantiateFeature(final Sentence sentence, final Token prd)
    {
        return feature_extracter.instantiateACFeature(sentence, prd);
    }
            
    public String[] instantiateFeature(final Sentence sentence, final Token arg, final Token prd)
    {
        return feature_extracter.instantiateACFeature(sentence, arg, prd);
    }
            
    public boolean checkArguments(final Sentence sentence)
    {
        for (int j=0; j<sentence.preds.length; ++j) {        
            final Token pred = sentence.tokens.get(sentence.preds[j]);
            if (pred.arguments.isEmpty()) return true;
        }
      
        return false;
    }    
    
}
