package ark.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * CounterTable represents a histogram of strings.  It allows incrementing 
 * and decrementing counts for each string, and transforming the histogram
 * into various data-structures.
 * 
 * @author Lingpeng Kong, Bill McDowell
 * 
 */
public class CounterTable{
	public HashMap<String, Integer> counts;
	
	public CounterTable(){
		this.counts= new HashMap<String,Integer>();
	}
	
	public void incrementCount(String w){
		if(this.counts.containsKey(w)){
			this.counts.put(w, this.counts.get(w) + 1);
		}else{
			this.counts.put(w, 1);
		}
	}
	
	public void removeCountsLessThan(int minCount) {
		List<String> valuesToRemove = new ArrayList<String>();
		for (Entry<String, Integer> entry : this.counts.entrySet()) {
			if (entry.getValue() < minCount)
				valuesToRemove.add(entry.getKey());
		}
		
		for (String valueToRemove : valuesToRemove)
			this.counts.remove(valueToRemove);
	}
	
	public HashMap<String, Integer> buildIndex() {
		HashMap<String, Integer> index = new HashMap<String, Integer>();
		int i = 0;
		
		for (Entry<String, Integer> entry : this.counts.entrySet()) {
			index.put(entry.getKey(), i);
			i++;
		}
		
		return index;
	}
	
	public TreeMap<Integer, List<String>> getSortedCounts() {
		TreeMap<Integer, List<String>> sortedCounts = new TreeMap<Integer, List<String>>();
		
		for (Entry<String, Integer> entry : this.counts.entrySet()) {
			if (!sortedCounts.containsKey(entry.getValue()))
				sortedCounts.put(entry.getValue(), new ArrayList<String>());
			
			sortedCounts.get(entry.getValue()).add(entry.getKey());
		}
		
		return sortedCounts;
	}
	
	public int getSize() {
		return this.counts.size();
	}
}