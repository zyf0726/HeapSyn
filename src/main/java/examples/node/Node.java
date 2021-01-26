package examples.node;

import java.lang.reflect.Method;

import heapsyn.heap.ClassH;
import heapsyn.heap.FieldH;

public class Node {
	
	public static ClassH classH;
	public static FieldH fNext, fElem;
	public static Method mNew, mGetNext, mGetElem, mSetElem, mAddAfter, mAddBefore;
	
	static {
		try {
			classH = ClassH.of(Node.class);
			fNext = FieldH.of(Node.class.getDeclaredField("next"));
			fElem = FieldH.of(Node.class.getDeclaredField("elem"));
			mNew = Node.class.getDeclaredMethod("__new__", int.class);
			mGetNext = Node.class.getDeclaredMethod("getNext");
			mGetElem = Node.class.getDeclaredMethod("getElem");
			mSetElem = Node.class.getDeclaredMethod("setElem", int.class);
			mAddAfter = Node.class.getDeclaredMethod("addAfter", int.class);
			mAddBefore = Node.class.getDeclaredMethod("addBefore", int.class);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/* private fields */
	private Node next;
	private int elem;
	
	/* private methods */
	private Node(Node next, int elem) {
		this.next = next;
		this.elem = elem;
	}
	
	/* public methods */
	public static Node __new__(int elem) {
		Node node = new Node(null, elem);
		return node;
	}
	
	public Node getNext() {
		return this.next;
	}
	
	public int getElem() {
		return this.elem;
	}
	
	public boolean setElem(int elem) {
		if (this.elem == elem) {
			return true;
		} else {
			this.elem = elem;
			return false;
		}
	}
	
	public void addAfter(int elem) {
		Node next = new Node(null, elem);
		this.next = next;
	}
	
	public Node addBefore(int elem) {
		Node prev = new Node(this, elem);
		return prev;
	}
}
