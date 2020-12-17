package heapsyn.util.graph;

public class FeatureGraphwise {
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof FeatureGraphwise))
			return false;
		FeatureGraphwise other = (FeatureGraphwise) obj;
		
		if (this.numNodes != other.numNodes)
			return false;
		
		return true;
	}
	
	/* number of nodes in the graph */
	int numNodes = 0;
	public int getNumberNodes() { return this.numNodes; }
}
