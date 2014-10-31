package com.joewandy.alignmentResearch.objectModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class HDPAnnotation {

	private Map<Feature, HDPAnnotationItem> annotations;
	
	public HDPAnnotation() {
		this.annotations = new HashMap<Feature, HDPAnnotationItem>();
	}
		
	public HDPAnnotationItem get(Feature key) {
		return annotations.get(key);
	}

	public void annotate(Feature f, String msg) {
		
		HDPAnnotationItem annots = this.annotations.get(f);

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

		this.annotations.put(f, annots);
	
	}
	
	public int size() {
		return this.annotations.size();
	}
			
}