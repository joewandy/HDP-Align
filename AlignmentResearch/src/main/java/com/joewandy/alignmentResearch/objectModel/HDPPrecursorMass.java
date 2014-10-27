package com.joewandy.alignmentResearch.objectModel;

public class HDPPrecursorMass implements Comparable<HDPPrecursorMass> {

	private double mass;
	private double ppm;
	private int count;
	
	public HDPPrecursorMass(double mass, double ppm) {
		this.mass = mass;
		this.ppm = ppm;
		this.count = 1;
	}

	public double getMass() {
		return mass;
	}
	
	public boolean withinTolerance(double toCheck) {
		double delta = PPM(mass, ppm);
		// 3 times the window to match the gaussian distribution used in the model
		double upper = mass + (delta*3); 
		double lower = mass - (delta*3);
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
		
	@Override
	public String toString() {
		return "HDPPrecursorMass [mass=" + mass + ", count=" + count + "]";
	}

	private double PPM(double mass, double q) {
		return q * (0.000001*mass);
	}

	public int compareTo(HDPPrecursorMass o) {
		return -Integer.compare(this.getCount(), o.getCount());
	}
	
}
