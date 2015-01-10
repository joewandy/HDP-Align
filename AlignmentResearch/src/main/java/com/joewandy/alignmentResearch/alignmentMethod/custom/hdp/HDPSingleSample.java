package com.joewandy.alignmentResearch.alignmentMethod.custom.hdp;

import java.io.Serializable;
import java.util.List;

import com.joewandy.alignmentResearch.model.HDPMetabolite;
import com.rits.cloning.Cloner;

public class HDPSingleSample implements Serializable {

	private static final long serialVersionUID = 3896163882135099326L;
	
	private List<HDPMetabolite> metabolites;
	
	// dummy constructor for jackson
	public HDPSingleSample() {

	}
	
	public HDPSingleSample(List<HDPMetabolite> metabolites) {
		Cloner cloner = new Cloner();
		this.metabolites = cloner.deepClone(metabolites);
	}

	public List<HDPMetabolite> getMetabolites() {
		return metabolites;
	}

	@Override
	public String toString() {
		return "HDPSingleSample [metabolites=" + metabolites + "]";
	}
	
}
