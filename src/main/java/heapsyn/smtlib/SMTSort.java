package heapsyn.smtlib;

public enum SMTSort {
	SMT_INT("Int"),
	SMT_BOOL("Bool"),
	SMT_UNKNOWN("Unknown")
	;
	
	private String repr;
	
	private SMTSort(String repr) {
		this.repr = repr;
	}
	
	public String toSMTString() {
		return this.repr;
	}
}
