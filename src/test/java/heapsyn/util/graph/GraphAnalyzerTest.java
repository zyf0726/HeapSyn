package heapsyn.util.graph;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

public class GraphAnalyzerTest {
	
	private GraphAnalyzer<Integer, Integer>
		gaEmp, gaTour, gaNoEdge;
	private GraphAnalyzer<Integer, String>
		gaRand1, gaRand2;
	private GraphAnalyzer<String, String>
		ga1, ga2, ga3;

	@Before
	public void setUp() throws Exception {
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
		gaNoEdge = new GraphAnalyzer<>(nodes, null);
		
		gaRand1 = new GraphAnalyzer<>(
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
		
		gaRand2 = new GraphAnalyzer<>(
			Arrays.asList(1, 2, 3, 4, 5, 6),
			Arrays.asList(
				new Edge<>(1, 2, "e1"),
				new Edge<>(2, 4, "e2"),
				new Edge<>(2, 6, "e3"),
				new Edge<>(6, 2, "e4"),
				new Edge<>(6, 6, "e5"),
				new Edge<>(5, 6, "e6"),
				new Edge<>(6, 3, "e7"),
				new Edge<>(4, 4, "e8")
			)
		);
		
		ga1 = new GraphAnalyzer<>(
			Arrays.asList("o1", "o2", "o3", "o4", "o5", "o6", "o7", "o8", "o9", "null"),
			Arrays.asList(
				new Edge<>("o1", "o3", ".A"),
				new Edge<>("o2", "o3", ".A"),
				new Edge<>("o3", "null", ".A"),
				new Edge<>("o4", "o5", ".A"),
				new Edge<>("o5", "o6", ".A"),
				new Edge<>("o6", "null", ".A"),
				new Edge<>("o7", "o8", ".A"),
				new Edge<>("o8", "o9", ".A"),
				new Edge<>("o9", "null", ".A")
			)
		);
		
		ga2 = new GraphAnalyzer<>(
			Arrays.asList("s1", "s2", "s3", "s4", "s5", "s7", "s8", "s9", "s6", "s3", "null"),
			Arrays.asList(
				new Edge<>("s3", "s1", ".A"),
				new Edge<>("s4", "s1", ".A"),
				new Edge<>("s5", "s2", ".A"),
				new Edge<>("s2", "s8", ".A"),
				new Edge<>("s9", "s7", ".A"),
				new Edge<>("s7", "s6", ".A"),
				new Edge<>("s1", "null", ".A"),
				new Edge<>("s8", "null", ".A"),
				new Edge<>("s6", "null", ".A")
			)
		);
		
		ga3 = new GraphAnalyzer<>(
			Arrays.asList("o1", "o2", "o3", "o4", "o5", "o6", "o7", "o8", "o9", "null"),
			Arrays.asList(
				new Edge<>("o1", "o3", ".A"),
				new Edge<>("o3", "null", ".A"),
				new Edge<>("o2", "o4", ".A"),
				new Edge<>("o4", "null", ".A"),
				new Edge<>("o5", "o6", ".A"),
				new Edge<>("o6", "o9", ".A"),
				new Edge<>("o7", "o8", ".A"),
				new Edge<>("o8", "o9", ".A"),
				new Edge<>("o9", "null", ".A")
			)
		);
	}
	
	@Test(expected = NullPointerException.class)
	public void testConstructor1() {
		new GraphAnalyzer<Integer, Integer>(
			null,
			Arrays.asList(new Edge<>(0, 0, -1))
		);
	}
	
	@Test(expected = NullPointerException.class)
	public void testConstructor2() {
		new GraphAnalyzer<Integer, String>(
			Arrays.asList(1, 2, 3),
			Arrays.asList(new Edge<>(1, 4, ".L"))
		);
	}
	
	@Test(expected = NullPointerException.class)
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
		assertEquals(ga1.getFeatureNodewise("o4"), ga1.getFeatureNodewise("o7"));
		assertNotEquals(ga1.getFeatureNodewise("o1"), ga2.getFeatureNodewise("s1"));
		assertEquals(gaRand1.getFeatureNodewise(1), gaRand1.getFeatureNodewise(1));
		assertNotEquals(gaRand1.getFeatureNodewise(1), gaRand2.getFeatureNodewise(2));
		/* test in-degree */
		assertEquals(3, ga2.getFeatureNodewise("null").getInDegree());
		assertEquals(2, ga1.getFeatureNodewise("o3").getInDegree());
		assertEquals(2, ga3.getFeatureNodewise("o9").getInDegree());
		assertEquals(11, gaTour.getFeatureNodewise(11).getInDegree());
		assertEquals(7, gaTour.getFeatureNodewise(7).getInDegree());
		assertEquals(0, gaNoEdge.getFeatureNodewise(7).getInDegree());
		/* test SCC size */
		assertEquals(4, gaRand1.getFeatureNodewise(0).getSizeSCC());
		assertEquals(2, gaRand2.getFeatureNodewise(6).getSizeSCC());
		assertEquals(1, gaTour.getFeatureNodewise(3).getSizeSCC());
	}

	@Test
	public void testGetFeatureGraphwise() {
		assertEquals(gaEmp.getFeatureGraphwise(), gaEmp.getFeatureGraphwise());
		assertNotEquals(gaEmp.getFeatureGraphwise(), gaTour.getFeatureGraphwise());
		assertEquals(ga1.getFeatureGraphwise(), ga2.getFeatureGraphwise());
		assertNotEquals(gaRand1.getFeatureGraphwise(), gaRand2.getFeatureGraphwise());
		// assertNotEquals(ga1.getFeatureGraphwise(), ga3.getFeatureGraphwise());
	}

	@Test
	public void testComputeNumberNodes() {
		assertEquals(0, gaEmp.computeNumberNodes());
		assertEquals(12, gaNoEdge.computeNumberNodes());
		assertEquals(6, gaRand1.getFeatureGraphwise().getNumberNodes());
		assertEquals(10, ga2.computeNumberNodes());
	}
	
	@Test
	public void testComputeSizeSCCsRepr() {
		assertEquals("", gaEmp.computeSizeSCCsRepr());
		assertEquals("1.0.0.0.0.0.0.0.0.0.", ga3.computeSizeSCCsRepr());
		assertEquals("1.0.3.", gaRand1.computeSizeSCCsRepr());
		assertEquals("1.0.0.0.1.", gaRand2.getFeatureGraphwise().getSizeSCCsRepr());
	}
	
	@Test
	public void testAboutTarjanAlgorithm() {
		assertEquals(null, gaRand1.getSCCIdentifier(6));
		assertEquals(null, gaRand2.getSCCMembers(0));
		
		assertEquals(Arrays.asList(0, 3, 4, 5),
				gaRand1.getSCCMembers(3).stream().sorted().collect(Collectors.toList()));
		assertEquals(Arrays.asList(1),
				gaRand1.getSCCMembers(1).stream().sorted().collect(Collectors.toList()));
		assertEquals(Arrays.asList(2, 6),
				gaRand2.getSCCMembers(2).stream().sorted().collect(Collectors.toList()));
		assertEquals(Arrays.asList(4),
				gaRand2.getSCCMembers(4).stream().sorted().collect(Collectors.toList()));
		assertTrue(gaRand1.getSCCIdentifier(5) == gaRand1.getSCCIdentifier(0));
		assertTrue(gaRand1.getSCCIdentifier(3) == gaRand1.getSCCIdentifier(4));
		assertTrue(gaRand2.getSCCIdentifier(2) == gaRand2.getSCCIdentifier(6));
		
		// check SCCs are extracted in reversed topological order
		assertTrue(gaRand1.getSCCIdentifier(2) < gaRand1.getSCCIdentifier(1));
		assertTrue(gaRand1.getSCCIdentifier(1) < gaRand1.getSCCIdentifier(3));
		assertTrue(gaRand2.getSCCIdentifier(3) < gaRand2.getSCCIdentifier(6));
		assertTrue(gaRand2.getSCCIdentifier(4) < gaRand2.getSCCIdentifier(2));
		assertTrue(gaRand2.getSCCIdentifier(6) < gaRand2.getSCCIdentifier(5));
		assertTrue(gaRand2.getSCCIdentifier(2) < gaRand2.getSCCIdentifier(1));
	}

}
