package heapsyn.heap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import heapsyn.smtlib.ExistentialProposition;
import heapsyn.smtlib.Variable;
import heapsyn.util.Bijection;
import heapsyn.util.graph.Edge;
import heapsyn.util.graph.GraphAnalyzer;

public class SymbolicHeap {
	
	private ArrayList<ObjectH> accObjs;
	private HashSet<ObjectH> accObjSet;
	private ExistentialProposition constraint;
	
	private ArrayList<ObjectH> allObjs;		// must be reachable
	private ArrayList<Variable> allVars;	
	
	private GraphAnalyzer<ObjectH, FieldH> GA;
	
	public SymbolicHeap() {
		this.accObjs = new ArrayList<>(Arrays.asList(ObjectH.NULL_OBJECT));
		this.accObjSet = new HashSet<>(Arrays.asList(ObjectH.NULL_OBJECT));
		this.setConstraint(null);
		
		this.allObjs = new ArrayList<>(Arrays.asList(ObjectH.NULL_OBJECT));
		this.allVars = new ArrayList<>();
		
		this.GA = new GraphAnalyzer<>(this.allObjs, null);
	}
	
	public SymbolicHeap(Collection<ObjectH> accObjs, ExistentialProposition constraint) {
		if (accObjs == null)
			throw new IllegalArgumentException("the set of accessible objects cannot be null");
		if (!accObjs.contains(ObjectH.NULL_OBJECT))
			throw new IllegalArgumentException("ObjectH.NULL_OBJECT must be accessible");
		this.accObjs = new ArrayList<>(accObjs);
		this.accObjSet = new HashSet<>(accObjs);
		this.setConstraint(constraint);
		
		Set<ObjectH> visited = new HashSet<>(accObjs);
		LinkedList<ObjectH> queue = new LinkedList<>(accObjs);
		List<Edge<ObjectH, FieldH>> edges = new ArrayList<>();
		this.allVars = new ArrayList<>();
		while (!queue.isEmpty()) {
			ObjectH o = queue.removeFirst();
			if (o.isVariable()) {
				this.allVars.add(o.getVariable());
			}
			for (Entry<FieldH, ObjectH> entry : o.getEntries()) {
				edges.add(new Edge<ObjectH, FieldH>(o, entry.getValue(), entry.getKey()));
				if (visited.add(entry.getValue())) {
					queue.addLast(entry.getValue());
				}
			}
		}
		this.allObjs = new ArrayList<>(visited);
		
		this.GA = new GraphAnalyzer<>(visited, edges);
	}
	
	public void setConstraint(ExistentialProposition ep) {
		if (ep == null) {
			this.constraint = ExistentialProposition.ALWAYS_TRUE;
		} else {
			this.constraint = ep;
		}
	}
	
	public ArrayList<ObjectH> getAccessibleObjects() {
		return new ArrayList<>(this.accObjs);
	}
	
	public ExistentialProposition getConstraint() {
		return this.constraint;
	}
	
	public ArrayList<ObjectH> getAllObjects() {
		return new ArrayList<>(this.allObjs);
	}
	
	public ArrayList<Variable> getAllVariables() {
		return new ArrayList<>(this.allVars);
	}
	
	public boolean isAccessible(ObjectH obj) {
		return this.accObjSet.contains(obj);
	}
	
	public boolean maybeIsomorphic(SymbolicHeap other) {
		if (this.accObjs.size() != other.accObjs.size())
			return false;
		if (this.allObjs.size() != other.allObjs.size())
			return false;
		if (this.allVars.size() != other.allVars.size())
			return false;
		
		// TODO hash-based isomorphism decision
		return this.GA.getFeatureGraphwise().equals(other.GA.getFeatureGraphwise());
	}
	
	public Bijection<ObjectH, ObjectH> getIsomorphicMapping(SymbolicHeap other) {
		// TODO optimize by a SCC-based algorithm
		return _searchMapping(
			0, other,
			new Bijection<ObjectH, ObjectH>() {
				{ putUV(ObjectH.NULL_OBJECT, ObjectH.NULL_OBJECT); }
			}
		);
	}
	
	private Bijection<ObjectH, ObjectH> _searchMapping(int depth, SymbolicHeap other,
			Bijection<ObjectH, ObjectH> curMap) {
		if (depth == this.accObjs.size()) {
			return curMap.size() == this.allObjs.size() ? curMap : null;
		}
		
		ObjectH objU = this.accObjs.get(depth);
/*		Bijection<ObjectH, ObjectH> result = _searchMapping(depth + 1, other, curMap);
		if (curMap.containsU(objU) || result != null) {
			return result;
		}*/
		if (curMap.containsU(objU))
			return _searchMapping(depth + 1, other, curMap);
		
		for (ObjectH objV : other.accObjs) {
			Bijection<ObjectH, ObjectH> newMap = new Bijection<>(curMap);
			if (_updateMappingRecur(objU, objV, other, newMap)) {
				Bijection<ObjectH, ObjectH> result = _searchMapping(depth + 1, other, newMap);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}
	private boolean _updateMappingRecur(ObjectH objU, ObjectH objV, SymbolicHeap other,
			Bijection<ObjectH, ObjectH> aMap) {
		if (objU.getClassH() != objV.getClassH())
			return false;
		if (this.isAccessible(objU) != other.isAccessible(objV))
			return false;
		if (!this.GA.getFeatureNodewise(objU).equals(other.GA.getFeatureNodewise(objV)))
			return false;
		
		if (aMap.containsU(objU))
			return aMap.getV(objU) == objV;
		if (aMap.containsV(objV))
			return false;
		aMap.putUV(objU, objV);
		
		for (FieldH field : objU.getFields()) {
			ObjectH valueU = objU.getValue(field);
			ObjectH valueV = objV.getValue(field);
			if (!_updateMappingRecur(valueU, valueV, other, aMap))
				return false;
		}
		return true;
	}
	
	public Collection<ObjectH> cloneObjects(Bijection<ObjectH, ObjectH> cloneMap) {
		if (cloneMap == null) {
			cloneMap = new Bijection<ObjectH, ObjectH>();
		}
		for (ObjectH obj : this.allObjs) {
			ObjectH objClone = 
				obj.isNonNullObject() ?
					new ObjectH(obj.getClassH(), null) :
				obj.isVariable() ?
					new ObjectH(obj.getVariable().cloneVariable()) :
				/* obj.isNullObject() ! */
					ObjectH.NULL_OBJECT;
			cloneMap.putUV(obj, objClone);
		}
		for (ObjectH obj : this.allObjs) {
			Map<FieldH, ObjectH> fieldValMap = new HashMap<>();
			for (Entry<FieldH, ObjectH> entry : obj.getEntries()) {
				FieldH field = entry.getKey();
				ObjectH value = entry.getValue();
				fieldValMap.put(field, cloneMap.getV(value));
			}
			cloneMap.getV(obj).setFieldValueMap(fieldValMap);
		}
		return new ArrayList<>(cloneMap.getMapU2V().values());
	}
}
