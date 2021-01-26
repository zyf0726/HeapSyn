package heapsyn.util;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class Bijection<U, V> {
	
	private Map<U, V> mapU2V;
	private Map<V, U> mapV2U;
	
	public Bijection() {
		this.mapU2V = new HashMap<>();
		this.mapV2U = new HashMap<>();
	}
	
	public Bijection(Bijection<U, V> other) {
		this.mapU2V = new HashMap<>(other.mapU2V);
		this.mapV2U = new HashMap<>(other.mapV2U);
	}
	
	public boolean putUV(U keyU, V keyV) {
		Preconditions.checkNotNull(keyU);
		Preconditions.checkNotNull(keyV);
		if (mapU2V.containsKey(keyU))
			return keyV.equals(mapU2V.get(keyU));
		if (mapV2U.containsKey(keyV))
			return false;
		mapU2V.put(keyU, keyV);
		mapV2U.put(keyV, keyU);
		return true;
	}
	
	public V getV(U keyU) {
		return this.mapU2V.get(keyU);
	}
	
	public U getU(V keyV) {
		return this.mapV2U.get(keyV);
	}
	
	public boolean containsU(U keyU) {
		return this.mapU2V.containsKey(keyU);
	}
	
	public boolean containsV(V keyV) {
		return this.mapV2U.containsKey(keyV);
	}
	
	public Map<U, V> getMapU2V() {
		return ImmutableMap.copyOf(this.mapU2V);
	}
	
	public Map<V, U> getMapV2U() {
		return ImmutableMap.copyOf(this.mapV2U);
	}
	
	public int size() {
		return this.mapU2V.size();
	}
	
	public void clear() {
		this.mapU2V.clear();
		this.mapV2U.clear();
	}

}
