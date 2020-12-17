package heapsyn.util.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class GraphAnalyzer<N, L extends Comparable<L>> {
	
	private ArrayList<N> allNodes;
	private Map<N, Integer> node2ID;
	private ArrayList<SortedMap<L, Integer>> succMap;
	
	private ArrayList<FeatureNodewise> featNode;
	private FeatureGraphwise featGraph;
	
	public GraphAnalyzer(Collection<N> nodes, Collection<Edge<N, L>> edges) {
		this.allNodes = new ArrayList<>();
		this.node2ID = new HashMap<>();
		this.succMap = new ArrayList<>();
		if (nodes != null) {
			for (N anode : nodes) {
				if (this.node2ID.containsKey(anode))
					continue;
				this.node2ID.put(anode, this.allNodes.size());
				this.allNodes.add(anode);
				this.succMap.add(new TreeMap<>());
			}
		}
		if (edges != null) {
			for (Edge<N, L> e : edges) {
				Integer head = this.node2ID.get(e.head);
				Integer tail = this.node2ID.get(e.tail);
				if (head == null || tail == null)
					throw new IllegalArgumentException("the head/tail of an edge in 'edges' not in 'nodes'");
				this.succMap.get(head).put(e.label, tail);
			}
		}
		this.featNode = null;
		this.featGraph = null;
	}
	
	public FeatureNodewise getFeatureNodewise(N anode) {
		Integer id = this.node2ID.get(anode);
		if (id == null) {
			return null;
		}
		if (this.featNode == null) {
			this.featNode = computeNodewiseFeature();
		}
		return this.featNode.get(id);
	}
	
	public FeatureGraphwise getFeatureGraphwise() {
		if (this.featGraph == null) {
			this.featGraph = new FeatureGraphwise();
			this.featGraph.numNodes = computeNumberNodes();
		}
		return this.featGraph;
	}
	
	ArrayList<FeatureNodewise> computeNodewiseFeature() {
		ArrayList<FeatureNodewise> featNode = new ArrayList<>();
		for (int id = 0; id < allNodes.size(); ++id)
			featNode.add(new FeatureNodewise());
		/* compute in-degree */
		for (int uid = 0; uid < allNodes.size(); ++uid)
			for (int vid : succMap.get(uid).values()) {
				featNode.get(vid).inDeg += 1;
			}
		return featNode;
	}
	
	int computeNumberNodes() {
		return this.allNodes.size();
	}
	
}
