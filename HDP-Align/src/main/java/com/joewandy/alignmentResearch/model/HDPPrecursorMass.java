package com.joewandy.alignmentResearch.model;

import java.util.HashSet;
import java.util.Set;

import peakml.chemistry.Molecule;

import com.joewandy.mzmatch.query.CompoundQuery;

public class HDPPrecursorMass implements Comparable<HDPPrecursorMass> {

	private double mass;
	private double ppm;
	private int count;
	private Set<Molecule> molecules;
	private CompoundQuery dbQuery;
	private Set<String> messages;
	
	public HDPPrecursorMass(double mass, double ppm, CompoundQuery dbQuery) {
		this.mass = mass;
		this.ppm = ppm;
		this.count = 1;
		this.dbQuery = dbQuery;
		this.messages = new HashSet<String>();
	}

	// copy constructor
	public HDPPrecursorMass(HDPPrecursorMass another) {
		this.mass = another.mass;
		this.ppm = another.ppm;
		this.count = another.count;
		this.molecules = new HashSet<Molecule>(another.molecules);
		this.dbQuery = another.dbQuery;
		this.messages = new HashSet<String>(another.messages);
	}

	public double getMass() {
		return mass;
	}
	
	public boolean withinTolerance(double toCheck) {
		double delta = PPM(mass, ppm);
		double upper = mass + delta; 
		double lower = mass - delta;
		if (lower < toCheck && toCheck < upper) {
			return true;
		} else {
			return false;
		}				
	}
		
	public int getCount() {
		return count;
	}
	
	public void incrementCount() {
		count++;
	}
	
	/**
	 * Returns the molecules from database that matches this precursor's mass within tolerance
	 * @return Molecules within tolerance from the DB
	 */
	public Set<Molecule> initMolecules() {
		// if no molecules yet, then search DB for the first time
		if (molecules == null) {
			try {
				molecules = dbQuery.findCompoundsByMass(mass, ppm*3, 0);
			} catch (Exception e) {
				molecules = new HashSet<Molecule>();
			}
		}
		return molecules;
	}
		
	@Override
	public String toString() {
		String msg = "HDPPrecursorMass [mass=" + mass + ", freq=" + count + ", databaseMolecules=" + molecules.size() + " { ";
		for (Molecule mol : molecules) {
			msg += mol.getName() + ", ";
		}
		msg += " }";
		return msg;
	}

	private double PPM(double mass, double q) {
		return q * (0.000001*mass);
	}

	public int compareTo(HDPPrecursorMass o) {
		return -Integer.compare(this.getCount(), o.getCount());
	}

	public double getPpm() {
		return ppm;
	}

	public void setPpm(double ppm) {
		this.ppm = ppm;
	}

	public Set<Molecule> getMolecules() {
		return molecules;
	}

	public void setMolecules(Set<Molecule> molecules) {
		this.molecules = molecules;
	}

	public CompoundQuery getDbQuery() {
		return dbQuery;
	}

	public void setDbQuery(CompoundQuery dbQuery) {
		this.dbQuery = dbQuery;
	}

	public Set<String> getMessages() {
		return messages;
	}

	public void setMessages(Set<String> messages) {
		this.messages = messages;
	}
	
	public void addMessage(String message) {
		this.messages.add(message);
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
}
