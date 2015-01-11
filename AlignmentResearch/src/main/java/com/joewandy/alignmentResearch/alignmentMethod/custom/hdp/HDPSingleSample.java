package com.joewandy.alignmentResearch.alignmentMethod.custom.hdp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.model.HDPMetabolite;

public class HDPSingleSample implements Serializable {

	private static final long serialVersionUID = 3896163882135099326L;
	
	private List<HDPMetabolite> metabolites;
	
	// dummy constructor for jackson
	public HDPSingleSample() {

	}
	
	public HDPSingleSample(List<HDPMetabolite> metabolites) {
//		Cloner cloner = new Cloner();
//		this.metabolites = cloner.deepClone(metabolites);
		this.metabolites = new ArrayList<HDPMetabolite>();
		for (HDPMetabolite met : metabolites) {
			HDPMetabolite copy = new HDPMetabolite(met);
			this.metabolites.add(copy);
		}
	}

	public List<HDPMetabolite> getMetabolites() {
		return metabolites;
	}

	@Override
	public String toString() {
		return "HDPSingleSample [metabolites=" + metabolites + "]";
	}
	
}
