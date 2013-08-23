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

package com.apporiented.algorithm.clustering.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;

public class DefaultClusteringAlgorithmTest {

	private double[][] distances;
	private String[] names;

	@Before
	public void setup() {
		distances = SampleClusterData.DISTANCES;
		names = SampleClusterData.NAMES;
	}

	@Test
	public void testClusteringAvgLink() {
		ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
		Cluster c = alg.performClustering(distances, names,
		        new AverageLinkageStrategy());
		Assert.assertNotNull(c);
	}

}
