package com.joewandy.alignmentResearch.alignmentMethod.custom.hdp;

import java.io.Serializable;
import java.util.List;

import com.joewandy.alignmentResearch.model.HDPMetabolite;
import com.rits.cloning.Cloner;

public class HDPResultsSample implements Serializable {

	private static final long serialVersionUID = 3896163882135099326L;
	
	private List<HDPMetabolite> metabolites;
	
	// dummy constructor for jackson
	public HDPResultsSample() {

	}
	
	public HDPResultsSample(List<HDPMetabolite> metabolites) {
		Cloner cloner = new Cloner();
		this.metabolites = cloner.deepClone(metabolites);
	}

	public List<HDPMetabolite> getMetabolites() {
		return metabolites;
	}
	
}
