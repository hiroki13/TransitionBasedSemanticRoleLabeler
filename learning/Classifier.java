/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package learning;

/**
 *
 * @author hiroki
 */
public class Classifier {

    public float[][] weight;
    public float[][] aweight;
    public float t = 1.0f;

    public Classifier() {}
    
    public float calcScore(final int[] feature, final int tag)
    {
        return 0.0f;
    }
    
    public void updateWeights(final int o_label, final int p_label, final int[] feature){}

    public void updateWeights(final int o_label, final int p_label, final int[] o_feature, final int[] feature){}
    
}
