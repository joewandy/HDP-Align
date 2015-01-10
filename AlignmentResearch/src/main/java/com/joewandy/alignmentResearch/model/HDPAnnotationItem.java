package com.joewandy.alignmentResearch.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.joewandy.alignmentResearch.util.PrettyPrintMap;

public class HDPAnnotationItem {
	
	private Map<String, Integer> annotationItem;
	
	public HDPAnnotationItem() {
		this.annotationItem = new HashMap<String, Integer>();
	}
	
	public void put(String msg, int count) {
		annotationItem.put(msg, count);
	}
	
	public Integer get(String msg) {
		return annotationItem.get(msg);
	}

	public Set<Entry<String, Integer>> entrySet() {
		return annotationItem.entrySet();
	}

	@Override
	public String toString() {
		PrettyPrintMap<String, Integer> msg = new PrettyPrintMap<>(annotationItem);
		return "\tHDPAnnotationItem [\n" + msg + "\n\t]";
	}
	
}