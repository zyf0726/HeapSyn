package heapsyn.heap;

import heapsyn.util.Bijection;

public interface ActionIfFound {
	
	boolean emitMapping(Bijection<ObjectH, ObjectH> ret);

}
