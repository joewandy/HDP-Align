package com.joewandy.alignmentResearch.objectModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class HDPAnnotation<T> {

	private Map<T, HDPAnnotationItem> annotations;
	
	public HDPAnnotation() {
		this.annotations = new HashMap<T, HDPAnnotationItem>();
	}
		
	public HDPAnnotationItem get(T key) {
		return annotations.get(key);
	}

	public void annotate(T key, String msg) {
		
		HDPAnnotationItem annots = this.annotations.get(key);

		if (annots == null) {
			annots = new HDPAnnotationItem();
			annots.put(msg, 1);
		} else {
		
			assert(annots != null);
			Integer count = annots.get(msg);
			if (count != null) {
				annots.put(msg, count+1); 				
			} else {
				annots.put(msg, 1);
			}
			
		}

		this.annotations.put(key, annots);
	
	}
	
	public int size() {
		return this.annotations.size();
	}
			
}