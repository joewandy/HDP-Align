package com.joewandy.alignmentResearch.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A class to pretty print map See
 * http://stackoverflow.com/questions/10120273/pretty-print-a-map-in-java
 */
public class PrettyPrintMap<K, V> {
	
	private Map<K, V> map;

	public PrettyPrintMap(Map<K, V> map) {
		this.map = map;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		Iterator<Entry<K, V>> iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			sb.append("\t\t");
			Entry<K, V> entry = iter.next();
			sb.append(entry.getKey());
			sb.append(" = ");
			sb.append(entry.getValue());
			if (iter.hasNext()) {
				sb.append("\n");
			}
		}
		return sb.toString();

	}

}