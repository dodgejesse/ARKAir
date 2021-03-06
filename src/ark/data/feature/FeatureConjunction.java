/**
 * Copyright 2014 Bill McDowell 
 *
 * This file is part of theMess (https://github.com/forkunited/theMess)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 */

package ark.data.feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ark.data.annotation.Datum;
import ark.data.annotation.Datum.Tools;
import ark.util.BidirectionalLookupTable;
import ark.util.CounterTable;

/**
 * For a datum d, FeatureConjunction computes a vector whose elements are given by
 * a flattening of the tensor product of vectors computed for d by a set of referenced
 * features.  
 * 
 * The referenced features are given by a list of feature 'referenceNames'
 * used within a FeaturizedDataSet constructed from an experiment configuration file.
 * 
 * @author Bill McDowell
 *
 * @param <D> datum type
 * @param <L> datum label type
 */
public class FeatureConjunction<D extends Datum<L>, L> extends Feature<D, L> {
	private BidirectionalLookupTable<String, Integer> vocabulary;
	private int minFeatureOccurrence;
	private String[] featureReferences;
	private String[] parameterNames = {"minFeatureOccurrence", "featureReferences"};
	
	private FeaturizedDataSet<D, L> dataSet; // Has other initialized features to be conjoined
	
	public FeatureConjunction() {
		this.vocabulary = new BidirectionalLookupTable<String, Integer>();
	}
	
	@Override
	public boolean init(FeaturizedDataSet<D, L> dataSet) {
		this.dataSet = dataSet;

		CounterTable<String> counter = new CounterTable<String>();
		for (D datum : this.dataSet) {
			Map<String, Double> conjunction = conjunctionForDatum(datum);
			for (String key : conjunction.keySet())
				counter.incrementCount(key);
		}
		
		counter.removeCountsLessThan(this.minFeatureOccurrence);
		this.vocabulary = new BidirectionalLookupTable<String, Integer>(counter.buildIndex());
		
		return true;
	}

	@Override
	public Map<Integer, Double> computeVector(D datum) {
		Map<String, Double> unfilteredConjunction = conjunctionForDatum(datum);
		Map<Integer, Double> vector = new HashMap<Integer, Double>();
		for (Entry<String, Double> entry : unfilteredConjunction.entrySet()) {
			if (this.vocabulary.containsKey(entry.getKey()))
				vector.put(this.vocabulary.get(entry.getKey()), entry.getValue());
		}
		
		return vector;
	}
	
	private Map<String, Double> conjunctionForDatum(D datum) {
		Map<String, Double> conjunction = new HashMap<String, Double>();
		conjunction.put("", 1.0);
		for (int i = 0; i < this.featureReferences.length; i++) {
			Feature<D, L> feature = this.dataSet.getFeatureByReferenceName(this.featureReferences[i]);
			Map<Integer, Double> values = feature.computeVector(datum);
			Map<Integer, String> vocab = feature.getVocabularyForIndices(values.keySet());
			Map<String, Double> nextConjunction = new HashMap<String, Double>();
			
			for (Entry<String, Double> conjunctionEntry : conjunction.entrySet()) {
				for (Entry<Integer, String> vocabEntry : vocab.entrySet()) {
					nextConjunction.put(conjunctionEntry.getKey() + "//" + vocabEntry.getValue(), conjunctionEntry.getValue()*values.get(vocabEntry.getKey()));
				}
			}
			
			conjunction = nextConjunction;
		}
		
		return conjunction;
	}

	@Override
	public String getGenericName() {
		return "Conjunction";
	}

	@Override
	public int getVocabularySize() {
		return this.vocabulary.size();
	}

	@Override
	public String getVocabularyTerm(int index) {
		return this.vocabulary.reverseGet(index);
	}

	@Override
	protected boolean setVocabularyTerm(int index, String term) {
		this.vocabulary.put(term, index);
		return true;
	}

	@Override
	protected String[] getParameterNames() {
		return this.parameterNames;
	}

	@Override
	protected String getParameterValue(String parameter) {
		if (parameter.equals("minFeatureOccurrence"))
			return String.valueOf(this.minFeatureOccurrence);
		else if (parameter.equals("featureReferences")) {
			if (this.featureReferences == null)
				return "";
			StringBuilder featureReferences = new StringBuilder();
			for (int i = 0; i < this.featureReferences.length; i++)
				featureReferences = featureReferences.append(this.featureReferences[i]).append("/");
			if (featureReferences.length() > 0)
				featureReferences = featureReferences.delete(featureReferences.length() - 1, featureReferences.length());
			return featureReferences.toString();
		}

		return null;
	}

	@Override
	protected boolean setParameterValue(String parameter,
			String parameterValue, Tools<D, L> datumTools) {
		if (parameter.equals("minFeatureOccurrence")) {
		 	this.minFeatureOccurrence = Integer.valueOf(parameterValue);
		} else if (parameter.equals("featureReferences")) {
			this.featureReferences = parameterValue.split("/");
		} else {
			return false;
		}
		return true;
	}

	@Override
	protected Feature<D, L> makeInstance() {
		return new FeatureConjunction<D, L>();
	}
	
}
