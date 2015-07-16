/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package semanticrolelabeler;

import io.RoleDict;
import io.Sentence;
import java.util.ArrayList;
import learning.MultiClassPerceptron;

/**
 *
 * @author hiroki
 */
final public class Trainer {
    final public ArrayList<Sentence> sentencelist;
    final public Parser parser;

    public Trainer(final ArrayList<Sentence> sentencelist, final int weight_length, final int prune)
    {
        this.sentencelist = sentencelist;
        this.parser = new BaseParser(new MultiClassPerceptron(RoleDict.rolearray.size(), weight_length), weight_length, prune);
    }
    
    public void train()
    {
        this.parser.train(sentencelist);
    }
    
    public void setOracleState()
    {
        for (int i=0; i<sentencelist.size(); ++i) {
            final Sentence sentence = sentencelist.get(i);
            sentence.o_state = parser.getOracleState(sentence);                    
        }
    }

}
