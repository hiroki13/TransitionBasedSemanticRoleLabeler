/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author hiroki
 */
public class RoleDict {

    static public ArrayList<String> roledict = new ArrayList();
    static public ArrayList<Integer> rolearray = new ArrayList();
    static public HashMap<String, Integer> biroledict = new HashMap();
    static public boolean core;
    
    static public void add(String role)
    {
        if (core && !"A0".equals(role) && !"A1".equals(role)) return;

        if (roledict.contains(role)) return;
        roledict.add(role);
        rolearray.add(roledict.indexOf(role));
    }

    static public int addAndGet(String role)
    {
        if (core && !"A0".equals(role) && !"A1".equals(role)) return -1;

        if (!roledict.contains(role)) {
            roledict.add(role);
            rolearray.add(roledict.indexOf(role));
        }

        return roledict.indexOf(role);
    }
    
    static public int size()
    {
        return roledict.size();
    }
    
}
