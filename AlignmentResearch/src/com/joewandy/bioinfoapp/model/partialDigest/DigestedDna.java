package com.joewandy.bioinfoapp.model.partialDigest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.joewandy.bioinfoapp.model.core.Dna;

/**
 * DigestedDna represents a linear sequence of DNA molecules that has been
 * digested. A digested DNA sequence may have a list of restriction sites, which
 * are the occurrences of specific sequence that can be cut by a restriction
 * enzymes.
 * 
 * @author joewandy
 * 
 */
public class DigestedDna extends Dna {

	private List<RestrictionSite> restrictionSites;

	public DigestedDna(String dnaString) {
		super(dnaString);
		this.restrictionSites = new ArrayList<RestrictionSite>();
	}

	public void addRestrictionSite(RestrictionSite site) {
		this.restrictionSites.add(site);
	}

	public void addRestrictionSite(int site) {
		this.restrictionSites.add(new RestrictionSite(site));
	}

	public List<RestrictionSite> getRestrictionSites() {
		return this.restrictionSites;
	}

	public int getRestrictionSitesNo() {
		if (this.restrictionSites == null) {
			return 0;
		}
		return this.restrictionSites.size();
	}

	public List<RestrictionFragment> getPartialDigestFragments() {

		// Sort restriction sites as array
		RestrictionSite[] rs = new RestrictionSite[this.getRestrictionSitesNo()];
		rs = this.restrictionSites.toArray(rs);
		Arrays.sort(rs);

		// Compute partial digest for each pairwise distance of
		// restriction sites
		List<RestrictionFragment> partialDigestList = new ArrayList<RestrictionFragment>();
		for (int i = 0; i < rs.length - 1; i++) {
			for (int j = i + 1; j < rs.length; j++) {
				int distance = rs[j].getLocation() - rs[i].getLocation();
				partialDigestList.add(new RestrictionFragment(distance));
			}
		}

		// return the result as a sorted list
		Collections.sort(partialDigestList);
		return partialDigestList;

	}

	@Override
	public String toString() {
		return "DnaSequence [dnaString=" + super.getSequenceString() + "]";
	}

}
