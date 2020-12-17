package heapsyn.util.graph;

public class Edge<N, L extends Comparable<L>> {
	N head, tail;
	L label;
	
	public Edge(N head, N tail, L label) {
		if (label == null)
			throw new IllegalArgumentException("a non-null and comparable label expected");
		this.head = head;
		this.tail = tail;
		this.label = label;
	}
}
