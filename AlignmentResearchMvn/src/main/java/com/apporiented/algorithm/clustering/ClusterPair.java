/*******************************************************************************
 * Copyright 2013 Lars Behnke
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.apporiented.algorithm.clustering;

public class ClusterPair implements Comparable<ClusterPair> {

	private Cluster lCluster;
	private Cluster rCluster;
	private Double linkageDistance;

	public Cluster getlCluster() {
		return lCluster;
	}

	public void setlCluster(Cluster lCluster) {
		this.lCluster = lCluster;
	}

	public Cluster getrCluster() {
		return rCluster;
	}

	public void setrCluster(Cluster rCluster) {
		this.rCluster = rCluster;
	}

	public Double getLinkageDistance() {
		return linkageDistance;
	}

	public void setLinkageDistance(Double distance) {
		this.linkageDistance = distance;
	}

	
	public int compareTo(ClusterPair o) {
		int result;
		if (o == null || o.getLinkageDistance() == null) {
			result = -1;
		} else if (getLinkageDistance() == null) {
			result = 1;
		} else {
			result = getLinkageDistance().compareTo(o.getLinkageDistance());
		}

		return result;
	}

	public Cluster agglomerate(String name) {
		if (name == null) {
			StringBuilder sb = new StringBuilder();
			if (lCluster != null) {
				sb.append(lCluster.getName());
			}
			if (rCluster != null) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(rCluster.getName());
			}
			name = sb.toString();
		}
		Cluster cluster = new Cluster(name);
		cluster.setDistance(getLinkageDistance());
		cluster.addChild(lCluster);
		cluster.addChild(rCluster);
		lCluster.setParent(cluster);
		rCluster.setParent(cluster);
		return cluster;
	}

	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (lCluster != null) {
			sb.append(lCluster.getName());
		}
		if (rCluster != null) {
			if (sb.length() > 0) {
				sb.append(" + ");
			}
			sb.append(rCluster.getName());
		}
		sb.append(" : " + linkageDistance);
		return sb.toString();
	}

}
