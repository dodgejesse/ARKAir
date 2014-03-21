package ark.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class BidirectionalLookupTable<S, T> {
	private Map<S, T> forwardLookup;
	private Map<T, S> reverseLookup;
	
	public BidirectionalLookupTable(Map<S, T> forwardLookup) {
		this.forwardLookup = forwardLookup;
		this.reverseLookup = new HashMap<T, S>(forwardLookup.size());
		for (Entry<S, T> entry : forwardLookup.entrySet())
			this.reverseLookup.put(entry.getValue(), entry.getKey());
	}
	
	public boolean containsKey(S key) {
		return this.forwardLookup.containsKey(key);
	}
	
	public boolean reverseContainsKey(T key) {
		return this.reverseLookup.containsKey(key);
	}
	
	public T get(S key) {
		return this.forwardLookup.get(key);
	}
	
	public S reverseGet(T value) {
		return this.reverseLookup.get(value);
	}
	
	public T put(S key, T value) {
		this.reverseLookup.put(value, key);
		return this.forwardLookup.put(key, value);
	}
	
	public int size() {
		return this.forwardLookup.size();
	}
}