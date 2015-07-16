/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package semanticrolelabeler;

import io.RoleDict;
import io.Sentence;
import io.Token;
import java.util.ArrayList;

/**
 *
 * @author hiroki
 */
public class State {
    
    final Sentence sentence;
    final ArrayList<Token> tokens;

    int p, prd_i;
    ArrayList<Integer> list1;
    ArrayList<Integer> list2;
    ArrayList<Integer> list3;
    ArrayList<Integer> list4;
    final int[][] A;
    
    final ArrayList<int[]> features;
    final ArrayList<Integer> actions;
    
    public State(final Sentence sent)
    {
        sentence = sent;
        tokens = sentence.tokens;
        A = new int[sentence.preds.length][sentence.size()];
        features = new ArrayList();
        actions = new ArrayList();
    }
    
    public void initiateList(final int prd_id, final int p_i)
    {
        p = prd_id;
        prd_i = p_i;
        list1 = initiateListL(p);        
        list2 = new ArrayList();        
        list3 = new ArrayList();        
        list4 = initiateListR(p);        
    }
    
    private ArrayList initiateListL(final int i)
    {
        final ArrayList<Integer> list = new ArrayList();
        for (int j=1; j<i; ++j) list.add(j);
        return list;
    }
    
    private ArrayList initiateListR(final int i)
    {
        final ArrayList<Integer> list = new ArrayList();
        for (int j=i+1; j<tokens.size(); ++j) list.add(j);
        return list;
    }
    
    public void transition(final int action)
    {
        if (action == 0) noArcL();
        else if (action == 1) ArcL();
        else if (action == 2) noArcR();
        else ArcR();
    }
    
    public void transitionL(final int action)
    {
        if (action == 0) noArcL();
        else ArcL();
    }
    
    public void transitionR(final int action)
    {
        if (action == 0) noArcR();
        else ArcR();
    }
    
    private void noArcL()
    {
        list2.add(list1.remove(list1.size()-1));
    }

    private void ArcL()
    {
        final int arg = list1.remove(list1.size()-1);
        A[prd_i][arg] = 1;
        list2.add(arg);
    }
    
    private void noArcR()
    {
        list3.add(list4.remove(0));
    }

    private void ArcR()
    {
        final int arg = list4.remove(0);
        A[prd_i][arg] = 1;
        list3.add(arg);
    }
    
}
