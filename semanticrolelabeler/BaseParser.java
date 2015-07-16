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
        this.label_size = RoleDict.rolearray.size();
    }
    
    @Override
    public void train(final ArrayList<Sentence> sentencelist)
    {
        correct = 0.0f;
        r_total = 0.0f;
        p_total = 0.0f;

        for (int i=0; i<sentencelist.size(); ++i) {
            final Sentence sentence = sentencelist.get(i);
            
            if (sentence.preds.length == 0) continue;

//            if (feature_extracter.g_cache.size() < i+1)                    
//                feature_extracter.g_cache.add(new String[sentence.preds.length][sentence.size()][]);

            if (i%1000 == 0 && i != 0) System.out.print(String.format("%d ", i));                                

            final State state = decode(sentence);
            update(sentence.o_state, state);
            check(sentence.o_state, state);

            if (i==prune) break;
                        
        }

        final float recall = correct/r_total;
        final float precision = correct/p_total;
        final float f = (2*recall*precision) / (recall+precision);
        
        System.out.println("\tF1: " + f + "\tPrecision: " + precision + "\tRecall: " + recall);                        
    }
    
    private void check(final State o_state, final State state)
    {
        for (int i=0; i<o_state.A.length; ++i) {
            final int[] tmp_o_args = o_state.A[i];
            final int[] tmp_args = state.A[i];

            for (int j=0; j<tmp_o_args.length; ++j) {
                if (tmp_o_args[j] == tmp_args[j] && tmp_o_args[j] != 0) correct += 1;
                if (tmp_o_args[j] != 0) r_total += 1;
                if (tmp_args[j] != 0) p_total += 1;
            }
        }
    }
    
    private State decode(final Sentence sentence)
    {
        final State state = new State(sentence);
        final int[] prd_ids = sentence.preds;

        for (int prd_i=0; prd_i<sentence.preds.length; ++prd_i) {
            final Token prd = state.tokens.get(prd_ids[prd_i]);
            
            state.initiateList(prd.id, prd_i);
            transition(state, prd);
        }
        
        return state;
    }

    public void transition(final State state, final Token prd)
    {
        // 0:noArcL, 1:ArcL, 2:noArcR, 3:ArcR
        while (!state.list1.isEmpty()) {
            final Token arg = state.tokens.get(state.list1.get(state.list1.size()-1));
            final int[] feature = extractFeature(state.sentence, prd, arg, "L");                                    
            final int best_action = bestAction(feature);

            state.actions.add(best_action);            
            state.features.add(feature);
            state.transitionL(best_action);            
        }

        while (!state.list4.isEmpty()) {
            final Token arg = state.tokens.get(state.list4.get(0));                
            final int[] feature = extractFeature(state.sentence, prd, arg, "R");
            final int best_action = bestAction(feature);

            state.actions.add(best_action);
            state.features.add(feature);
            state.transitionR(best_action);            
        }
    
    }
    
    private int bestAction(final int[] features)
    {
        int best_action = -1;
        float best_score = -1000000.0f;
        
        for (int i=0; i<label_size; ++i) {
            final float score = calcScore(features, i);
            
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
/*        
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
*/
    
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

    public int[] extractFeature(final Sentence sentence, final Token prd)
    {
        return feature_extracter.extractFeature(sentence, prd);
    }
            
    public int[] extractFeature(final Sentence sentence, final Token prd, final Token arg, final String direction)
    {
        return feature_extracter.extractFeature(sentence, prd, arg, direction);
    }
            
    public boolean checkArguments(final Sentence sentence)
    {
        for (int j=0; j<sentence.preds.length; ++j) {        
            final Token pred = sentence.tokens.get(sentence.preds[j]);
            if (pred.arguments.isEmpty()) return true;
        }
      
        return false;
    }
    
    @Override
    public State getOracleState(final Sentence sentence)
    {
        final State o_state = new State(sentence);

        for (int prd_i=0; prd_i<o_state.sentence.preds.length; ++prd_i) {
            final Token prd = o_state.tokens.get(sentence.preds[prd_i]);
            
            o_state.initiateList(prd.id, prd_i);
            oracleTransition(sentence.o_graph[prd_i], o_state, prd);
        }        
        
        return o_state;
    }

    private void oracleTransition(final int[] oracle_args, final State o_state, final Token prd)
    {
        while (!o_state.list1.isEmpty()) {
            final Token arg = o_state.tokens.get(o_state.list1.get(o_state.list1.size()-1));
            final int[] feature = extractFeature(o_state.sentence, prd, arg, "L");                                    
            final int action = oracle_args[arg.id];
            
            o_state.actions.add(action);            
            o_state.features.add(feature);
            o_state.transitionL(action);            
        }

        while (!o_state.list4.isEmpty()) {
            final Token arg = o_state.tokens.get(o_state.list4.get(0));                
            final int[] feature = extractFeature(o_state.sentence, prd, arg, "R");
            final int action = oracle_args[arg.id];

            o_state.actions.add(action);
            o_state.features.add(feature);
            o_state.transitionR(action);            
        }
    }
    
    public void update(final State o_state, final State state)
    {
        for (int i=0; i<o_state.actions.size(); ++i) {
            final int o_label = o_state.actions.get(i);
            final int label = state.actions.get(i);
            final int[] o_feature = o_state.features.get(i);
            final int[] feature = state.features.get(i);
            
            if (o_label != label)
                classifier.updateWeights(o_label, label, o_feature, feature);
        }
        
        classifier.t += 1;
    }
}
