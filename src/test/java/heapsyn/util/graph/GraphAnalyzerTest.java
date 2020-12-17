package heapsyn.util.graph;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

public class GraphAnalyzerTest {
	
	static private GraphAnalyzer<Integer, Integer>
		gaEmp, gaTour, gaIso;
	static private GraphAnalyzer<Integer, String>
		gaRand;
	static private GraphAnalyzer<String, String>
		ga1, ga2, ga3;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		gaEmp = new GraphAnalyzer<>(null, null);
		
		ArrayList<Integer> nodes = new ArrayList<>();
		ArrayList<Edge<Integer, Integer>> edges = new ArrayList<>();		
		for (int uid = 0; uid < 12; ++uid)
			nodes.add(uid);
		for (int uid = 0; uid < 12; ++uid)
			for (int vid = uid + 1; vid < 12; ++vid) {
				edges.add(new Edge<Integer, Integer>(uid, vid, vid));
			}
		gaTour = new GraphAnalyzer<>(nodes, edges);
		gaIso = new GraphAnalyzer<>(nodes, null);
		
		gaRand = new GraphAnalyzer<>(
			Arrays.asList(0, 1, 2, 3, 4, 5),
			Arrays.asList(
				new Edge<>(4, 0, "e1"),
				new Edge<>(1, 1, "e2"),
				new Edge<>(1, 2, "e3"),
				new Edge<>(4, 2, "e4"),
				new Edge<>(3, 1, "e5"),
				new Edge<>(5, 3, "e6"),
				new Edge<>(0, 5, "e7"),
				new Edge<>(3, 4, "e8")
			)
		);
		
		ga1 = new GraphAnalyzer<>(
			Arrays.asList("o1", "o2", "o3", "o4", "o5", "o6", "o7", "o8", "o9", "null"),
			Arrays.asList(
				new Edge<>("o1", "o3", ".A"),
				new Edge<>("o2", "o3", ".A"),
				new Edge<>("o4", "o5", ".A"),
				new Edge<>("o6", "o7", ".B"),
				new Edge<>("o8", "o9", ".B"),
				new Edge<>("o3", "null", ".C"),
				new Edge<>("o5", "null", ".C"),
				new Edge<>("o7", "null", ".C"),
				new Edge<>("o9", "null", ".C")
			)
		);
		
		ga2 = new GraphAnalyzer<>(
			Arrays.asList("s1", "s2", "s3", "s4", "s5", "s7", "s8", "s9", "s6", "null"),
			Arrays.asList(
				new Edge<>("s3", "s1", ".A"),
				new Edge<>("s4", "s1", ".A"),
				new Edge<>("s5", "s2", ".A"),
				new Edge<>("s6", "s8", ".B"),
				new Edge<>("s9", "s7", ".B"),
				new Edge<>("s1", "null", ".C"),
				new Edge<>("s2", "null", ".C"),
				new Edge<>("s7", "null", ".C"),
				new Edge<>("s8", "null", ".C")
			)
		);
		
		ga3 = new GraphAnalyzer<>(
			Arrays.asList("o1", "o2", "o3", "o4", "o5", "o6", "o7", "o8", "o9", "null"),
			Arrays.asList(
				new Edge<>("o1", "o3", ".A"),
				new Edge<>("o2", "o4", ".A"),
				new Edge<>("o5", "o6", ".A"),
				new Edge<>("o7", "o9", ".B"),
				new Edge<>("o8", "o9", ".B"),
				new Edge<>("o3", "null", ".C"),
				new Edge<>("o4", "null", ".C"),
				new Edge<>("o6", "null", ".C"),
				new Edge<>("o9", "null", ".C")
			)
		);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructor1() {
		new GraphAnalyzer<Integer, Integer>(
			null,
			Arrays.asList(new Edge<>(0, 0, -1))
		);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructor2() {
		new GraphAnalyzer<Integer, String>(
			Arrays.asList(1, 2, 3),
			Arrays.asList(new Edge<>(1, 4, ".L"))
		);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructor3() {
		new GraphAnalyzer<Integer, String>(
			Arrays.asList(1),
			Arrays.asList(new Edge<>(1, 1, null))
		);
	}
	
	@Test
	public void testGetFeatureNodewise() {
		assertNull(gaEmp.getFeatureNodewise(0));
		assertEquals(ga1.getFeatureNodewise("o1"), ga1.getFeatureNodewise("o2"));
		assertNotEquals(ga1.getFeatureNodewise("o1"), ga2.getFeatureNodewise("s1"));
		/* test in-degree */
		assertEquals(4, ga2.getFeatureNodewise("null").getInDegree());
		assertEquals(2, ga1.getFeatureNodewise("o3").getInDegree());
		assertEquals(2, ga3.getFeatureNodewise("o9").getInDegree());
		assertEquals(11, gaTour.getFeatureNodewise(11).getInDegree());
		assertEquals(7, gaTour.getFeatureNodewise(7).getInDegree());
		assertEquals(0, gaIso.getFeatureNodewise(7).getInDegree());
	}

	@Test
	public void testGetFeatureGraphwise() {
		assertEquals(gaEmp.getFeatureGraphwise(), gaEmp.getFeatureGraphwise());
		assertNotEquals(gaEmp.getFeatureGraphwise(), gaTour.getFeatureGraphwise());
		assertEquals(ga1.getFeatureGraphwise(), ga2.getFeatureGraphwise());
		// assertNotEquals(ga1.getFeatureGraphwise(), ga3.getFeatureGraphwise());
	}

	@Test
	public void testComputeNumberNodes() {
		assertEquals(0, gaEmp.computeNumberNodes());
		assertEquals(12, gaIso.computeNumberNodes());
		assertEquals(6, gaRand.getFeatureGraphwise().getNumberNodes());
	}

}
