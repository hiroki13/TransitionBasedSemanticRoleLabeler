/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package feature;

import io.RoleDict;
import io.Sentence;
import io.Token;
import java.util.ArrayList;

/**
 *
 * @author hiroki
 */
public class FeatureExtractor {

    final int weight_size, role_size;
    int k;
    int total;
    public ArrayList<String[][][]> g_cache;
    public ArrayList<String[][][][][]> second_cache;
    public ArrayList<String[][]> pd_cache;    
    
    public FeatureExtractor(final int weight_size)
    {
        this.weight_size = weight_size;
        this.role_size = RoleDict.rolearray.size();
        this.g_cache = new ArrayList();
    }

    public int[] extractFeature(final Sentence sentence, final Token prd)
    {
        final String[] feature = instantiateACFeature(sentence, prd);
        final int[] encoded_feature = encodeFeature(feature);
        return encoded_feature;        
    }

    public int[] extractFeature(final Sentence sentence, final Token prd, final Token arg)
    {
        final String[] feature = instantiateACFeature(sentence, prd, arg);
        final int[] encoded_feature = encodeFeature(feature);
        return encoded_feature;        
    }

    public int[] extractAIFeature(final Sentence sentence, final int prd_i, final int arg_id)
    {
        final String[] feature = instantiateAIFeature(sentence, prd_i, arg_id);
        final int[] encoded_feature = encodeFeature(feature);
        return encoded_feature;        
    }    

    public int[] extractPDFeature(final Sentence sentence, final int prd_i)
    {
        final String[] feature = instantiatePDFeature(sentence, prd_i);
        final int[] encoded_feature = encodeFeature(feature);
        return encoded_feature;        
    }

    public String[] instantiateACFeature(final Sentence sentence, final Token prd)
    {
        k = 0;        
        String[] feature = new String[9];

        final ArrayList<Token> tokens = sentence.tokens;

        final Token pparent = tokens.get(prd.phead);            
        final String sense = prd.plemma + prd.pred;        
        final String subcat = prd.subcat;        
        final String childdepset = prd.childdepset;        
        final String childposset = prd.childposset;
                        
        feature[k++] = "PredW_" + prd.form;        
        feature[k++] = "PredPOS_" + prd.ppos;        
        feature[k++] = "PredLemma_" + prd.plemma;        
        feature[k++] = "PredLemmaSense_" + sense;        
        feature[k++] = "PredParentW_" + pparent.form;        
        feature[k++] = "PredParentPOS_" + pparent.ppos;        
        feature[k++] = "DepSubCat_" + subcat;        
        feature[k++] = "ChildDepSet_" + childdepset;        
        feature[k++] = "ChildPOSSet_" + childposset;                
        
        feature = conjoin(feature, prd.cpos);
        
        return feature;
    }
    
    public String[] instantiateACFeature(final Sentence sentence, final Token arg, final Token prd)
    {
        k = 0;        
        String[] feature = new String[12];

        final ArrayList<Token> tokens = sentence.tokens;
        final Token pparent = tokens.get(arg.phead);            
        
        feature[k++] = "ArgPOS_" + arg.ppos;        
        feature[k++] = "ArgLemma_" + arg.plemma;        
        feature[k++] = "ArgParentW_" + pparent.plemma;        
        feature[k++] = "ArgParentPOS_" + pparent.ppos;        
        feature[k++] = "ArgLchildPOS_" + arg.leftmostpos;        
        feature[k++] = "ArgLchildW_" + arg.leftmostw;        
        feature[k++] = "ArgRchildPOS_" + arg.rightmostpos;        
        feature[k++] = "ArgRchildW_" + arg.rightmostw;
        feature[k++] = "ArgLsibPOS_" + arg.leftsiblingpos;
        feature[k++] = "ArgLsibW_" + arg.leftsiblingw;
        
        feature[k++] = "ArgRsibPOS_" + arg.rightsiblingpos;
        feature[k++] = "ArgRsibW_" + arg.rightsiblingw;
        
        feature = conjoin(feature, arg.cpos);
        
        return feature;
    }
    
    public String[] instantiateAIFeature(final Sentence sentence, final int prd_i, final int arg_i)
    {
        k = 0;        

        final ArrayList<Token> tokens = sentence.tokens;
        String[][][] cache = g_cache.get(sentence.index);

        if (cache[prd_i][arg_i] != null) return cache[prd_i][arg_i];
        
        String[] feature = new String[27];

        final Token prd = tokens.get(sentence.preds[prd_i]);
        final Token pparent = tokens.get(prd.phead);            
        final String sense = prd.plemma + prd.pred;        
        final String subcat = prd.subcat;        
        final String childdepset = prd.childdepset;        
        final String childposset = prd.childposset;
                        
        final Token arg = tokens.get(arg_i);
        final String dep_r_path = sentence.dep_r_path[prd_i][arg.id];        
        final String dep_pos_path = sentence.dep_pos_path[prd_i][arg.id];        
        final String position = position(prd.id, arg.id);

        feature[k++] = "PredW_" + prd.form;        
        feature[k++] = "PredPOS_" + prd.ppos;        
        feature[k++] = "PredLemma_" + prd.plemma;        
        feature[k++] = "PredLemmaSense_" + sense;        
        feature[k++] = "PredParentW_" + pparent.form;        
        feature[k++] = "PredParentPOS_" + pparent.ppos;        
        feature[k++] = "DepSubCat_" + subcat;        
        feature[k++] = "ChildDepSet_" + childdepset;        
        feature[k++] = "ChildPOSSet_" + childposset;                
        feature[k++] = "ArdW_" + arg.form;        

        feature[k++] = "ArgPOS_" + arg.ppos;        
        feature[k++] = "ArgDeprel_" + arg.pdeprel;        
        feature[k++] = "APW_" + arg.form + prd.form;        
        feature[k++] = "APF_" + arg.plemma + prd.plemma;        
        feature[k++] = "APPL_" + arg.ppos + prd.plemma;        
        feature[k++] = "APPS_" + arg.plemma + sense;        
        feature[k++] = "DepRelPath_" + dep_r_path;        
        feature[k++] = "POSPath_" + dep_pos_path;        
        feature[k++] = "Position_" +  position;        
        feature[k++] = "Position+DepRelPath" +  position + dep_r_path;        
        
        feature[k++] = "Position+ArgW" +  position + arg.form;        
        feature[k++] = "ArgPOS+PredLemmaSense" +  arg.ppos + sense;        
        feature[k++] = "Position+PredLemmaSense" +  position + sense;        
        feature[k++] = "ArgW+PredLemmaSense" +  arg.form + sense;        
        feature[k++] = "ArgPOS+ArgW" +  arg.ppos + arg.form;        
        feature[k++] = "POSPath+PredLemmaSense" +  dep_pos_path + sense;        
        feature[k++] = "Position+ArgPOS" +  position + arg.ppos;

        feature = conjoin(feature, prd.cpos);

        if (cache[prd_i][arg_i] == null)        
            cache[prd_i][arg_i] = feature;
        
        return feature;
    }

    
    
    public String[] instantiatePDFeature(final Sentence sentence, final int prd_i)
    {
        k = 0;        
        String[] feature = new String[6];

        final ArrayList<Token> tokens = sentence.tokens;
        String[][] cache = pd_cache.get(sentence.index);

        if (cache[prd_i] != null) return cache[prd_i];
        
        final Token prd = tokens.get(sentence.preds[prd_i]);
        final Token pparent = tokens.get(prd.phead);            
        final String subcat = prd.subcat;        
                        
        feature[k++] = "PredW_" + prd.form;        
        feature[k++] = "PredPOS_" + prd.ppos;        
        feature[k++] = "PredDeprel_" + prd.pdeprel;        
        feature[k++] = "PredParentW_" + pparent.form;        
        feature[k++] = "PredParentPOS_" + pparent.ppos;        
        feature[k++] = "DepSubCat_" + subcat;        
        
        feature = conjoin(feature, prd.cpos);

        if (cache[prd_i] == null) cache[prd_i] = feature;
        
        return feature;
    }
        
    public int[] encodeFeature (final String[] feature)
    {
        final int[] encoded_feature = new int[feature.length];
        for(int i=0; i<feature.length; ++i)            
            encoded_feature[i] = (feature[i].hashCode() >>> 1) % weight_size;

        return encoded_feature;
    }
    
    private String position(final int prd, final int arg)
    {
        if (arg < prd) return "Before";
        else if (arg > prd) return "After";
        else return "On";
    }

    public String[] conjoin(final String[] feature, final String label)
    {
        final String[] new_feature = new String[feature.length];
        for (int i=0; i<new_feature.length; ++i)
            new_feature[i] = feature[i] + label;
        return new_feature;
    }
    
}
