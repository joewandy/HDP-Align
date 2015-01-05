package com.joewandy.alignmentResearch.alignmentMethod.custom.hdp;

import java.util.List;

import com.joewandy.alignmentResearch.objectModel.HDPMetabolite;
import com.rits.cloning.Cloner;

public class HDPResultsSample {

	private List<HDPMetabolite> metabolites;
	
	public HDPResultsSample(List<HDPMetabolite> metabolites) {
		Cloner cloner = new Cloner();
		this.metabolites = cloner.deepClone(metabolites);
	}

	public List<HDPMetabolite> getMetabolites() {
		return metabolites;
	}
	
}
