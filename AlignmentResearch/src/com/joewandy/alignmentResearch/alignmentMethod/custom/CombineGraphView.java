package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.apache.commons.collections15.Transformer;

import peakml.Annotation;
import peakml.IPeak;

import com.joewandy.alignmentResearch.objectModel.AlignmentEdge;
import com.joewandy.alignmentResearch.objectModel.AlignmentExpResult;
import com.joewandy.alignmentResearch.objectModel.AlignmentPair;
import com.joewandy.alignmentResearch.objectModel.AlignmentVertex;
import com.joewandy.alignmentResearch.objectModel.Feature;

import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.filters.FilterUtils;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.io.PajekNetWriter;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public class CombineGraphView {

	public static final int TOP_N_EDGES_FOR_GRAPH = Integer.MAX_VALUE;
	private static final double WEIGHT_THRESHOLD = 20;
	private static final double VERTEX_SIZE_THRESHOLD = 30;

	public static final String LAYOUT_KK = "kk";
	public static final String LAYOUT_FR = "fr";
	public static final String LAYOUT_ISOM = "isom";
	public static final String LAYOUT_SPRING = "spring";
	public static final String LAYOUT_CIRCLE = "circle";

	private UndirectedGraph<AlignmentVertex, AlignmentEdge> alignmentGraph;
	private UndirectedGraph<AlignmentVertex, AlignmentEdge> prevAlignmentGraph;
	private Map<AlignmentVertex, Integer> degreeScores;

	private VisualizationViewer<AlignmentVertex, AlignmentEdge> viewer;
	
	private DefaultModalGraphMouse<AlignmentVertex, AlignmentEdge> gm;
	private AlignmentVertex pickedVertex;
	private AlignmentEdge pickedEdge;
	private Map<AlignmentEdge, AlignmentEdge> incidentEdges;
	private Map<AlignmentVertex, AlignmentVertex> incidentVertices;
	
	private JButton inspectBtn;
	private JButton clearBtn;	
	private JButton isolateBtn;
	private JTextArea output;
	private JFrame frame;
	private JPanel jp;
	private GraphZoomScrollPane scrollPane;
	
	private Map<Integer, Color> colorMap;
	
	private boolean isolating;
	private double clusterThreshold;

	public CombineGraphView(List<AlignmentEdge> edgeList, boolean multi, int dataFileCount, double clusterThreshold) {

		// TODO: use factories for vertex & edges here
		UndirectedGraph<AlignmentVertex, AlignmentEdge> alignmentGraph = null;

		if (multi) {

			// can contain parallel edges, no weight in the edge
			alignmentGraph = new UndirectedSparseMultigraph<AlignmentVertex, AlignmentEdge>();
			final int limit = Math.min(edgeList.size(),
					TOP_N_EDGES_FOR_GRAPH);
			for (int i = 0; i < limit; i++) {
				AlignmentEdge e = edgeList.get(i);
				alignmentGraph.addVertex(e.getLeft());
				alignmentGraph.addVertex(e.getRight());
				alignmentGraph.addEdge(e, e.getLeft(), e.getRight());
			}

		} else {

			// cannot contain parallel edges, edge has weight
			alignmentGraph = new UndirectedSparseGraph<AlignmentVertex, AlignmentEdge>();
			final int limit = Math.min(edgeList.size(),
					TOP_N_EDGES_FOR_GRAPH);
			for (int i = 0; i < limit; i++) {
				AlignmentEdge e = edgeList.get(i);
				// TODO: doesn't work, why ??!!
				// if (alignmentGraph.containsEdge(e)) {
				AlignmentEdge existing = alignmentGraph.findEdge(
						e.getLeft(), e.getRight());
				if (existing != null) {
					existing.addAlignmentPair(e.getAlignmentPairs());
					existing.updateAlignmentPairWeight();
				} else {				
					alignmentGraph.addVertex(e.getLeft());
					alignmentGraph.addVertex(e.getRight());
					alignmentGraph.addEdge(e, e.getLeft(), e.getRight());
				}
			}

		}
		
		// System.out.println("alignmentGraph.getEdgeCount() = " + alignmentGraph.getEdgeCount(EdgeType.UNDIRECTED));
		// System.out.println("alignmentGraph.getVertexCount() = " + alignmentGraph.getVertexCount());

		this.colorMap = new HashMap<Integer, Color>();
		Random rand = new Random();
		for (int i = 0; i < dataFileCount; i++) {
			float r = rand.nextFloat();
			float g = rand.nextFloat();
			float b = rand.nextFloat();
			Color randomColor = new Color(r, g, b, 0.5f);
			colorMap.put(i, randomColor);
		}
		
		this.alignmentGraph = alignmentGraph;
		this.degreeScores = new HashMap<AlignmentVertex, Integer>();
		this.pickedVertex = null;
		this.incidentEdges = new HashMap<AlignmentEdge, AlignmentEdge>();
		this.incidentVertices = new HashMap<AlignmentVertex, AlignmentVertex>();
		this.jp = new JPanel();
		this.frame = new JFrame();
		
		this.output = new JTextArea();	
		this.output.setEditable(false);
		this.output.setFont(new Font("Courier", Font.PLAIN, 10));
		
		this.clusterThreshold = clusterThreshold;
		
	}

	public CombineGraphView(UndirectedGraph<AlignmentVertex, AlignmentEdge> alignmentGraph, int clusterThreshold) {

		this.alignmentGraph = alignmentGraph;
		this.degreeScores = new HashMap<AlignmentVertex, Integer>();
		this.pickedVertex = null;
		this.incidentEdges = new HashMap<AlignmentEdge, AlignmentEdge>();
		this.incidentVertices = new HashMap<AlignmentVertex, AlignmentVertex>();
		this.jp = new JPanel();
		this.frame = new JFrame();
		this.output = new JTextArea(100, 30);					
		this.clusterThreshold = clusterThreshold;
		
	}
	
	public Graph<AlignmentVertex, AlignmentEdge> getAlignmentGraph() {
		return alignmentGraph;
	}

	public List<AlignmentEdge> getEdges() {
		List<AlignmentEdge> edges = new ArrayList<AlignmentEdge>(alignmentGraph.getEdges());
		return edges;
	}	
	
	public List<AlignmentEdge> clusterEdges() {

		int numEdgesToRemove = (int) (this.alignmentGraph.getEdgeCount() * this.clusterThreshold);
		System.out.println("Running edge betweeness clustering with numEdgesToRemove=" + numEdgesToRemove);
		
		EdgeBetweennessClusterer<AlignmentVertex, AlignmentEdge> clusterer = 
				new EdgeBetweennessClusterer<AlignmentVertex, AlignmentEdge>(numEdgesToRemove);
		Set<Set<AlignmentVertex>> clusterSet = clusterer.transform(this.alignmentGraph);
		List<AlignmentEdge> edges = clusterer.getEdgesRemoved();
		
		System.out.println(clusterSet.size() + " edges clustered");
		System.out.println(edges.size() + " edges removed");
		
		return edges;
		
	}

	public AlignmentExpResult computeStatistics() {

		AlignmentExpResult expResult = new AlignmentExpResult();

		// compute degree distribution
		DegreeScorer<AlignmentVertex> degreeScorer = new DegreeScorer<AlignmentVertex>(
				alignmentGraph);
		for (AlignmentVertex v : alignmentGraph.getVertices()) {
//			int score = 0;
//			for (AlignmentEdge e : alignmentGraph.getIncidentEdges(v)) {
//				score += e.getWeight();
//			}
//			expResult.increaseDegree(score);
			int score = degreeScorer.getVertexScore(v);
			expResult.increaseDegree(score);
			degreeScores.put(v, score);
		}

		// compute edge weight distribution
		for (AlignmentEdge e : alignmentGraph.getEdges()) {
			expResult.increaseEdgeWeight(e.getWeight());
			expResult.increaseIntensity(e.getWeight(), e.getIntensitySumSquareError());
			expResult.increaseGroupSize(e.getWeight(), e.getTotalGroupSize());
			expResult.addAlignmentPairs(e.getAlignmentPairs());
			expResult.addAlignmentEdge(e);
		}
		
		// List<AlignmentEdge> removedEdges = this.clusterEdges();
		// expResult.addRemovedEdges(removedEdges);
		
		Set<Feature> allFeatures = new HashSet<Feature>();
		for (AlignmentPair pair : expResult.getAlignmentPairs()) {
			Feature f1 = pair.getFeature1();
			allFeatures.add(f1);
			Feature f2 = pair.getFeature2();
			allFeatures.add(f2);
		}
		expResult.addFeatures(allFeatures);
						
		return expResult;

	}

	public void visualiseGraph(String title, String msg, int width, int height,
			Graph<AlignmentVertex, AlignmentEdge> myGraph, String layoutStr) {

		// Layout implements the graph drawing logic
		Layout<AlignmentVertex, AlignmentEdge> layout = initLayout(width,
				height, myGraph, layoutStr);

		// VisualizationServer actually displays the graph
		viewer = new VisualizationViewer<AlignmentVertex, AlignmentEdge>(layout);
		viewer.setPreferredSize(new Dimension(width, height));
		viewer.setBackground(Color.white);

		// Transformer maps the vertex number to a vertex property
		setTransformers();

		// Create the top-level components
		jp.setLayout(new BorderLayout());
		scrollPane = new GraphZoomScrollPane(viewer);
		jp.add(scrollPane);

		// set vertex and edge listeners when picked
		setPickedListeners();

		gm = new DefaultModalGraphMouse<AlignmentVertex, AlignmentEdge>();
		viewer.setGraphMouse(gm);

		addOtherControls(jp, gm);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(jp);
		frame.pack();
		frame.setVisible(true);
		frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		output.append(msg + "\n");

	}

	public void refreshGraph(String title, int width, int height,
			Graph<AlignmentVertex, AlignmentEdge> myGraph, String layoutStr) {

		// Layout implements the graph drawing logic
		Layout<AlignmentVertex, AlignmentEdge> layout = initLayout(width,
				height, myGraph, layoutStr);

		// VisualizationServer actually displays the graph
		viewer = new VisualizationViewer<AlignmentVertex, AlignmentEdge>(layout);
		viewer.setPreferredSize(new Dimension(width, height));
		viewer.setBackground(Color.white);

		// Transformer maps the vertex number to a vertex property
		setTransformers();

		// Create the top-level components
		jp.setLayout(new BorderLayout());
		jp.removeAll();
		GraphZoomScrollPane newPane = new GraphZoomScrollPane(viewer);
		jp.add(newPane);

		// set vertex and edge listeners when picked
		setPickedListeners();

		gm = new DefaultModalGraphMouse<AlignmentVertex, AlignmentEdge>();
		viewer.setGraphMouse(gm);
		
		addOtherControls(jp, gm);
		
		frame.pack();
		
	}
	
	private void setPickedListeners() {
		
		final PickedState<AlignmentVertex> pickedVertexState = viewer
				.getPickedVertexState();
		final PickedState<AlignmentEdge> pickedEdgeState = viewer
				.getPickedEdgeState();
		
		pickedVertexState.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				Object subject = e.getItem();
				if (subject instanceof AlignmentVertex) {
					AlignmentVertex vertex = (AlignmentVertex) subject;
					if (pickedVertexState.isPicked(vertex)) {
						output.append("Vertex " + vertex + " selected\n");
					}
					// in the enclosing parent class
					pickedEdge = null;
					pickedVertex = vertex;
					incidentEdges.clear();
					incidentVertices.clear();
					for (AlignmentEdge edge : alignmentGraph.getIncidentEdges(vertex)) {
						incidentEdges.put(edge, edge);
						for (AlignmentVertex incVertex : alignmentGraph.getIncidentVertices(edge)) {
							incidentVertices.put(incVertex, incVertex);								
						}
					}
				}
			}
		});

		pickedEdgeState.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				Object subject = e.getItem();
				if (subject instanceof AlignmentEdge) {
					AlignmentEdge edge = (AlignmentEdge) subject;
					if (pickedEdgeState.isPicked(edge)) {
						output.append("Edge " + edge + " selected\n");
						pickedEdge = edge;
						pickedVertex = null;
						incidentEdges.clear();
						incidentVertices.clear();
					}
				}
			}
		});
	}

	public void exportGraph(String filename) throws IOException {

		System.out.println("Saving graph to " + filename);
		PajekNetWriter<AlignmentVertex, AlignmentEdge> writer = new PajekNetWriter<AlignmentVertex, AlignmentEdge>();
		Transformer<AlignmentEdge, Number> edgeWeight = new Transformer<AlignmentEdge, Number>() {
			@Override
			public Number transform(AlignmentEdge e) {
				return e.getWeight();
			}
		};
		writer.save(this.alignmentGraph, filename, null, edgeWeight);

	}

	public void findCluster(String title, String layout) {

		WeakComponentClusterer<AlignmentVertex, AlignmentEdge> wcc = new WeakComponentClusterer<AlignmentVertex, AlignmentEdge>();
		Collection<UndirectedGraph<AlignmentVertex, AlignmentEdge>> ccs = FilterUtils
				.createAllInducedSubgraphs(wcc.transform(this.alignmentGraph),
						this.alignmentGraph);

		int i = 0;
		for (Graph<AlignmentVertex, AlignmentEdge> g : ccs) {
			int count = g.getVertexCount();
			// System.out.println("Count " + count);
			if (count > 10) {
				System.out.println("Count " + count + " graph " + g);
				this.visualiseGraph(title + i, "", 400, 400, g, layout);
				i++;
			} else {
				continue;
			}
		}

	}

	private void setTransformers() {

		Transformer<AlignmentVertex, Shape> vertexSize = new Transformer<AlignmentVertex, Shape>() {
			public Shape transform(AlignmentVertex v) {
				Ellipse2D circle = new Ellipse2D.Double(-4, -4, 8, 8);
				List<IPeak> peaks = v.getPeaks();
				Set<Feature> features = v.getFeatures();
				double scale = 1;
				if (!peaks.isEmpty()) {
					scale = Math.log(peaks.size()) + 0.5;
				} else if (!features.isEmpty()){
					scale = Math.log(features.size()) + 0.5;
				}
				return AffineTransform.getScaleInstance(scale, scale).createTransformedShape(circle);					
			}
		};

		Transformer<AlignmentVertex, String> vertexLabel = new Transformer<AlignmentVertex, String>() {
			public String transform(AlignmentVertex v) {
				int size = 0;
				Collection<AlignmentEdge> edges = alignmentGraph.getIncidentEdges(v);
				if (edges == null) {
					return "";
				}
				for (AlignmentEdge e : edges) {
					size += e.getWeight();
				}
				if (isolating) {
					return v.toString();
				} else {
					if (size >= CombineGraphView.VERTEX_SIZE_THRESHOLD) {
						return v.toString();
					} else {
						return "";
					}					
				}
			}
		};

		Transformer<AlignmentEdge, String> edgeLabel = new Transformer<AlignmentEdge, String>() {
			public String transform(AlignmentEdge e) {
				if (isolating) {
					return (String.format("%.1f", e.getWeight()));					
				} else {
					if (e.getWeight() >= CombineGraphView.WEIGHT_THRESHOLD) {
						return (String.format("%.1f", e.getWeight()));
					} else {
						return "";
					}					
				}
			}
		};

		Transformer<AlignmentEdge, Paint> edgeColor = new Transformer<AlignmentEdge, Paint>() {
			public Paint transform(AlignmentEdge e) {
							
				Color c = null;
				
				if (incidentEdges.containsKey(e)) {
					c = new Color(0.0f, 1.0f, 1.0f, 0.5f);
				} else {
					if (e.getWeight() >= CombineGraphView.WEIGHT_THRESHOLD) {
						c = new Color(1.0f, 0.0f, 0.0f, 0.5f);
					} else {
						c = Color.LIGHT_GRAY;
					}					
				}
				
				assert(c != null);
				return c;
			
			}
		};

		Transformer<AlignmentEdge, Stroke> edgeStroke = new Transformer<AlignmentEdge, Stroke>() {

			protected final Stroke basic = new BasicStroke(1);
	        protected final Stroke heavy = new BasicStroke(5);
	        protected final Stroke dotted = RenderContext.DOTTED;
	        protected final float dash[] = { 1.0f };
	        protected final Stroke dashed = new BasicStroke(1f, BasicStroke.CAP_ROUND,
	                BasicStroke.JOIN_ROUND, 10.0f, dash, 0.0f);
			
			public Stroke transform(AlignmentEdge e) {
				if (e.getWeight() >= CombineGraphView.WEIGHT_THRESHOLD) {
					return heavy;
				} else {
					return dashed;
				}
			}
			
		};

		viewer.getRenderContext().setVertexLabelTransformer(vertexLabel);
		viewer.getRenderContext().setVertexFillPaintTransformer(new VertexPaintTransformer(viewer.getPickedVertexState()));
		viewer.getRenderContext().setVertexShapeTransformer(vertexSize);
		viewer.getRenderContext().setLabelOffset(20);
		viewer.getRenderer().getVertexLabelRenderer().setPosition(Position.S);

		viewer.getRenderContext().setEdgeLabelTransformer(edgeLabel);
		viewer.getRenderContext().setEdgeDrawPaintTransformer(edgeColor);
		viewer.getRenderContext().setEdgeStrokeTransformer(edgeStroke);
        viewer.getRenderContext().setEdgeShapeTransformer(new EdgeShape.QuadCurve<AlignmentVertex, AlignmentEdge>());
		
	}

	private Layout<AlignmentVertex, AlignmentEdge> initLayout(int width,
			int height, Graph<AlignmentVertex, AlignmentEdge> myGraph,
			String layoutStr) {
		Layout<AlignmentVertex, AlignmentEdge> layout = null;
		if (layoutStr.equals(CombineGraphView.LAYOUT_ISOM)) {
			layout = new ISOMLayout<AlignmentVertex, AlignmentEdge>(myGraph);
		} else if (layoutStr.equals(CombineGraphView.LAYOUT_FR)) {
			layout = new FRLayout<AlignmentVertex, AlignmentEdge>(myGraph);
		} else if (layoutStr.equals(CombineGraphView.LAYOUT_KK)) {
			layout = new KKLayout<AlignmentVertex, AlignmentEdge>(myGraph);
		} else if (layoutStr.equals(CombineGraphView.LAYOUT_SPRING)) {
			SpringLayout<AlignmentVertex, AlignmentEdge> temp = new SpringLayout<AlignmentVertex, AlignmentEdge>(myGraph);
			// temp.lock(true);
			layout = temp;
		} else {
			layout = new CircleLayout<AlignmentVertex, AlignmentEdge>(myGraph);
		}
		layout.setSize(new Dimension(width, height));
		return layout;
	}

	private void addOtherControls(JPanel jp,
			DefaultModalGraphMouse<AlignmentVertex, AlignmentEdge> gm) {

		final JPanel controlPanel = new JPanel();
		FlowLayout controlLayout = new FlowLayout();
		controlPanel.setLayout(controlLayout);
		jp.add(controlPanel, BorderLayout.SOUTH);

		inspectBtn = new JButton("Inspect");
		inspectBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if (pickedVertex != null) {

					StringBuilder builder = new StringBuilder();
					builder.append("sourcePeakSet=" + pickedVertex.getSourcePeakSet() + "\n");
					builder.append("groupId=" + pickedVertex.getGroupId() + "\n");

					List<IPeak> myPeaks = pickedVertex.getPeaks();
					Set<Feature> myFeatures = pickedVertex.getFeatures();
					
					if (!myPeaks.isEmpty()) {
						builder.append("peaks=" + myPeaks.size());
						for (IPeak p : myPeaks) {
							builder.append("\tpeakId=" + p.getAnnotation(Annotation.peakId).getValueAsInteger());
							builder.append("\t\tmass=" + String.format("%.6f", p.getMass()));
							builder.append("\tintensity=" + String.format("%10.2f", p.getIntensity()));
							builder.append("\trt=" + String.format("%4.2f", p.getRetentionTime()));
							builder.append("\n");
						}						
					} else if (!myFeatures.isEmpty()) {
						builder.append("features=" + myFeatures.size());
						for (Feature f : myFeatures) {
							builder.append("\tpeakId=" + f.getPeakID());
							builder.append("\t\tmass=" + String.format("%.6f", f.getMass()));
							builder.append("\tintensity=" + String.format("%10.2f", f.getIntensity()));
							builder.append("\trt=" + String.format("%4.2f", f.getRt()));
							builder.append("\n");
						}												
					}

					output.append(builder.toString());
					
				} else if (pickedEdge != null) {

					StringBuilder builder = new StringBuilder();
					builder.append("left=" + pickedEdge.getLeft() + "\n");
					builder.append("right=" + pickedEdge.getRight() + "\n");
					builder.append("weight=" + pickedEdge.getWeight() + "\n");
					for (AlignmentPair pair : pickedEdge.getAlignmentPairs()) {
						builder.append(pair.toString());
					}
					builder.append("\nINTENSITY MEAN SQUARE ERROR = " + String.format("%10.2f", pickedEdge.getIntensityMeanSquareError()) + "\n");
					builder.append("\nINTENSITY ROOT MEAN SQUARE ERROR = " + String.format("%10.2f", pickedEdge.getIntensityRootMeanSquareError()) + "\n\n");
					output.append(builder.toString());
					
				}
				
			}			
		});
		
		clearBtn = new JButton("Clear");
		clearBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				output.setText("");
			}
		});		
		
		String label = "";
		if (isolating) {
			label = "Return";
		} else {
			label = "Isolate";
		}
		isolateBtn = new JButton(label);
		isolateBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (!isolating) {

					isolating = true;

					Set<AlignmentVertex> subsets = new HashSet<AlignmentVertex>(incidentVertices.keySet());
					subsets.add(pickedVertex);
					UndirectedGraph<AlignmentVertex, AlignmentEdge> induced = FilterUtils
							.createInducedSubgraph(subsets, alignmentGraph);
					prevAlignmentGraph = alignmentGraph;
					alignmentGraph = induced;
					refreshGraph("Subgraph", 1000, 700, induced, LAYOUT_ISOM);
					
				} else {

					isolating = false;

					alignmentGraph = prevAlignmentGraph;
					refreshGraph("Subgraph", 1000, 700, alignmentGraph, LAYOUT_SPRING);

				}
			}			
		});

		gm.setMode(Mode.PICKING);

		@SuppressWarnings("rawtypes")
		JComboBox modeBox = gm.getModeComboBox();

		controlPanel.add(inspectBtn);
		controlPanel.add(clearBtn);		
		controlPanel.add(isolateBtn);
		controlPanel.add(modeBox);

		final JPanel outputPanel = new JPanel();
	    JScrollPane scroll = new JScrollPane(output);
	    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
	    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	    scroll.setPreferredSize(new Dimension(600, 700));
	    outputPanel.add(scroll);
		jp.add(outputPanel, BorderLayout.EAST);

	}
	
	private class VertexPaintTransformer implements Transformer<AlignmentVertex, Paint> {

		private PickedState<AlignmentVertex> pickedState;
		
		public VertexPaintTransformer(PickedState<AlignmentVertex> pickedState) {
			this.pickedState = pickedState;
			colorMap = new HashMap<Integer, Color>();
			
		}
		
		@Override
		public Paint transform(AlignmentVertex v) {
			
			Color c = null;
			
			if (isolating) {
				int sourcePeakSet = v.getSourcePeakSet();
				c = colorMap.get(sourcePeakSet);
			} else {
				if (pickedState.isPicked(v) || incidentVertices.containsKey(v)) {				
					// yellow
					c = new Color(1.0f, 1.0f, 0.0f, 0.5f);
				} else {
					int sourcePeakSet = v.getSourcePeakSet();
					c = colorMap.get(sourcePeakSet);
				}	
			}
						
			assert(c != null);
			return c;

		}
		
	}

}