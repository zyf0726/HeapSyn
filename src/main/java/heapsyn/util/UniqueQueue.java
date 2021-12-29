package heapsyn.util;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

public class UniqueQueue<T> implements Iterable<T> {
	
	private Set<T> alreadyInQueue;
	private Queue<T> queue;
	
	public UniqueQueue() {
		this.alreadyInQueue = new HashSet<>();
		this.queue = new ArrayDeque<>();
	}
	
	public int size() {
		return queue.size();
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
	public boolean contains(Object o) {
		return alreadyInQueue.contains(o);
	}
	
	public Iterator<T> iterator() {
		return queue.iterator();
	}
	
	public void clear() {
		alreadyInQueue.clear();
		queue.clear();
	}
	
	public boolean add(T e) {
		if (alreadyInQueue.contains(e)) {
			return false;
		} else {
			alreadyInQueue.add(e);
			return queue.add(e);
		}
	}
	
	public T remove() {
		T e = queue.remove();
		alreadyInQueue.remove(e);
		return e;
	}
	
	public T element() {
		return queue.element();
	}

}
