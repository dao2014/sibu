package com.socialbusiness.dev.orangebusiness.util;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListUtil {

	public static List<Float> getFloatListFromMap(ArrayList<String> productIds, HashMap<String, Float> map) {
    	List<Float> list = new ArrayList<>();
    	for (String id : productIds) {
            if (!TextUtils.isEmpty(id)) {
                list.add(map.get(id));
            }
        }
		return list;
    }
    
    public static List<Integer> getIntegerListFromMap(ArrayList<String> productIds, HashMap<String, Integer> map) {
    	List<Integer> list = new ArrayList<>();
        for (String id : productIds) {
            if (!TextUtils.isEmpty(id)) {
                list.add(map.get(id));
            }
        }
    	return list;
    }
}
