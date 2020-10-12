package com.youtube.hempfest.clans.util;

import java.util.Comparator;
import java.util.Map;

public class HighestValue implements Comparator<String> {
	 
    Map<String,Double> base;
    public HighestValue(Map<String,Double> base){
        this.base = base;
    }
 
    // Note: this comparator imposes orderings that are inconsistent with equals.   
    public int compare(String a,String b){
        // sorting from high to low
        if(base.get(a) > base.get(b)){
            return -1;
        }
        if(base.get(a) < base.get(b)){
            return 1;
        }
        // entries with the same values are sorted alphabetically
        return a.compareTo(b);
    }
    }

