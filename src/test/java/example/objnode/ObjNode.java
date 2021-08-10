package example.objnode;

import java.lang.reflect.Method;

import heapsyn.heap.ClassH;
import heapsyn.heap.FieldH;

public class ObjNode {
	
	public static ClassH classH;
	public static FieldH fNxt, fVal;
	public static Method mNewAlias, mNewNull, mNewFresh, mGetNext, mGetValue;
	public static Method mSetValue, mSetFreshValue, mResetValue;
	public static Method mAddBefore, mAddAfter, mAddAfterFresh;
	public static Method mSetValueAliasNext, mMakeValueFresh;
	
	static {
		try {
			classH = ClassH.of(ObjNode.class);
			fNxt = FieldH.of(ObjNode.class.getDeclaredField("nxt"));
			fVal = FieldH.of(ObjNode.class.getDeclaredField("val"));
			mNewAlias = ObjNode.class.getMethod("newAlias", Object.class);
			mNewNull = ObjNode.class.getMethod("newNull");
			mNewFresh = ObjNode.class.getMethod("newFresh");
			mGetNext = ObjNode.class.getMethod("getNext");
			mGetValue = ObjNode.class.getMethod("getValue");
			mSetValue = ObjNode.class.getMethod("setValue", Object.class);
			mSetFreshValue = ObjNode.class.getMethod("setFreshValue");
			mResetValue = ObjNode.class.getMethod("resetValue");
			mAddBefore = ObjNode.class.getMethod("addBefore", Object.class);
			mAddAfter = ObjNode.class.getMethod("addAfter", Object.class);
			mAddAfterFresh = ObjNode.class.getMethod("addAfterFresh");
			mSetValueAliasNext = ObjNode.class.getMethod("setValueAliasNext");
			mMakeValueFresh = ObjNode.class.getMethod("makeValueFresh", ObjNode.class, ObjNode.class);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private ObjNode nxt;
	private Object val;
	
	private ObjNode(ObjNode nxt, Object val) {
		this.nxt = nxt;
		this.val = val;
	}
	
	public static ObjNode newAlias(Object val) {
		return new ObjNode(null, val);
	}
	
	public static ObjNode newNull() {
		return new ObjNode(null, null);
	}
	
	public static ObjNode newFresh() {
		return new ObjNode(null, new Object());
	}
	
	public ObjNode getNext() { return this.nxt; }
	
	public Object getValue() { return this.val; }
	
	public Object setValue(Object newVal) {
		if (this.val == newVal) {
			return null;
		} else {
			Object oldVal = this.val;
			this.val = newVal;
			return oldVal;
		}
	}
	
	public Object setFreshValue() {
		Object oldVal = this.val;
		this.val = new Object();
		return oldVal;
	}
	
	public void resetValue() {
		this.val = null;
	}
	
	public ObjNode addBefore(Object val) {
		ObjNode prev = new ObjNode(this, val);
		return prev;
	}
	
	public void addAfter(Object val) {
		ObjNode next = new ObjNode(null, val);
		this.nxt = next;
	}
	
	public void addAfterFresh() {
		ObjNode next = new ObjNode(null, new Object());
		this.nxt = next;
	}
	
	public void setValueAliasNext() {
		if (this.nxt != null) {
			this.val = this.nxt.val;
		}
	}
	
	public static Object makeValueFresh(ObjNode o1, ObjNode o2) {
		o1.val = new Object();
		o2.val = o1.val;
		return new Object();
	}

}
