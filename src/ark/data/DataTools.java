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

package ark.data;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import ark.wrapper.BrownClusterer;

import ark.util.OutputWriter;
import ark.util.StringUtil;
import ark.data.Gazetteer;

/**
 * 
 * DataTools loads gazetteers, brown clusterers, string cleaning 
 * functions, and other tools used in various 
 * models and experiments.  
 * 
 * A DataSet (in ark.data.annotation) has
 * access to a Tools object (defined in ark.data.annotation.Datum),
 * and that object contains a pointer to a DataTools object, so
 * any place in the code that has access to a DataSet also has access
 * to DataTools.  The difference between DataTools and Datum.Tools is
 * that Datum.Tools contains tools specific to a particular kind of
 * datum (e.g. a document datum in text classification or a tlink datum
 * in temporal ordering), whereas DataTools contains generic tools
 * that can be useful when working with many kinds of Datums.  This
 * split between generic tools and datum-specific tools allows the generic
 * tools to be loaded into memory only once even if you're working with
 * many kinds of datums at the same time.
 * 
 * Currently, for convenience, DataTools just loads everything into 
 * memory upon construction.  If memory conservation becomes particularly
 * important, then possibly this class should be rewritten to only keep 
 * things in memory when they are needed.
 * 
 * @author Bill McDowell
 *
 */
public class DataTools {
	/**
	 * Interface for a function that maps a string to another string--for
	 * example, for cleaning out garbage text before processing by features
	 * or models.
	 *
	 */
	public interface StringTransform {
		String transform(String str);
		// Return constant name for this transformation (used for deserializing features)
		String toString(); 
	}
	
	/**
	 * Interface for a function that maps a pair of strings to a real number--
	 * for example, as a measure of their similarity.
	 *
	 */
	public interface StringPairMeasure {
		double compute(String str1, String str2);
	}
	
	/**
	 * Interface for a function that maps a string to a collection of strings--
	 * for example, to compute a collection of prefixes or suffixes for a string.
	 *
	 */
	public interface StringCollectionTransform {
		Collection<String> transform(String str);
		String toString();
	}
	
	/**
	 * Represents a named file path.  It's useful for file paths to have
	 * names so that they can be referenced in experiment configuration files
	 * without machine specific file locations.
	 *
	 */
	public class Path {
		private String name;
		private String value;
		
		public Path(String name, String value) {
			this.name = name;
			this.value = value;
		}
		
		public String getValue() {
			return this.value;
		}
		
		public String getName() {
			return this.name;
		}
	}
	
	protected Map<String, Gazetteer> gazetteers;
	protected Map<String, DataTools.StringTransform> cleanFns;
	protected Map<String, DataTools.StringCollectionTransform> collectionFns;
	protected Map<String, BrownClusterer> brownClusterers;
	protected Map<String, Path> paths;
	protected Map<String, String> parameterEnvironment; // Environment variables that have been set 
	
	protected long randomSeed;
	protected Random globalRandom;
	protected OutputWriter outputWriter;
	
	public DataTools(OutputWriter outputWriter) {
		this.gazetteers = new HashMap<String, Gazetteer>();
		this.cleanFns = new HashMap<String, DataTools.StringTransform>();
		this.collectionFns = new HashMap<String, DataTools.StringCollectionTransform>();
		this.brownClusterers = new HashMap<String, BrownClusterer>();
		this.paths = new HashMap<String, Path>();
		this.parameterEnvironment = new HashMap<String, String>();
		
		this.outputWriter = outputWriter;
		
		this.cleanFns.put("DefaultCleanFn", new DataTools.StringTransform() {
			public String toString() {
				return "DefaultCleanFn";
			}
			
			public String transform(String str) {
				return StringUtil.clean(str);
			}
		});
		
		this.collectionFns.put("Prefixes", new DataTools.StringCollectionTransform() {
			public String toString() {
				return "Prefixes";
			}
			
			public Collection<String> transform(String str) {
				return StringUtil.prefixes(str);
			}
		});
		
		this.collectionFns.put("None", null);
		this.brownClusterers.put("None", null);
		this.globalRandom = new Random();
	}
	
	public Gazetteer getGazetteer(String name) {
		return this.gazetteers.get(name);
	}
	
	public DataTools.StringTransform getCleanFn(String name) {
		return this.cleanFns.get(name);
	}
	
	public DataTools.StringCollectionTransform getCollectionFn(String name) {
		return this.collectionFns.get(name);
	}
	
	public BrownClusterer getBrownClusterer(String name) {
		return this.brownClusterers.get(name);
	}
	
	public Path getPath(String name) {
		return this.paths.get(name);
	}
	
	public Map<String, String> getParameterEnvironment() {
		return this.parameterEnvironment;
	}
	
	public OutputWriter getOutputWriter() {
		return this.outputWriter;
	}
	
	/**
	 * @return a Random object instance that was instantiated when the DataTools
	 * object was instantiated. 
	 */
	public Random getGlobalRandom() {
		return this.globalRandom;
	}
	
	/**
	 * @return a Random object instance that is instantiated when makeLocalRandom 
	 * is called.  This is useful when there are multiple threads that require
	 * their own Random instances in order to preserve determinism with respect
	 * to a single Random seed.  Otherwise, if threads share the same Random 
	 * instance, then the order in which they interleave execution will determine 
	 * the behavior of the program.
	 * 
	 */
	public Random makeLocalRandom() {
		return new Random(this.randomSeed); 
	}
	
	public boolean addGazetteer(Gazetteer gazetteer) {
		this.gazetteers.put(gazetteer.getName(), gazetteer);
		return true;
	}
	
	public boolean addCleanFn(DataTools.StringTransform cleanFn) {
		this.cleanFns.put(cleanFn.toString(), cleanFn);
		return true;
	}
	
	public boolean addStopWordsCleanFn(final Gazetteer stopWords) {
		this.cleanFns.put("StopWordsCleanFn_" + stopWords.getName(), 
			new DataTools.StringTransform() {
				public String toString() {
					return "StopWordsCleanFn_" + stopWords.getName();
				}
				
				public String transform(String str) {
					str = StringUtil.clean(str);
					String stoppedStr = stopWords.removeTerms(str);
					if (stoppedStr.length() > 0)
						return stoppedStr;
					else 
						return str;
				}
			}
		);
		return true;
	}
	
	public boolean addCollectionFn(DataTools.StringCollectionTransform collectionFn) {
		this.collectionFns.put(collectionFn.toString(), collectionFn);
		return true;
	}
	
	public boolean addBrownClusterer(BrownClusterer brownClusterer) {
		this.brownClusterers.put(brownClusterer.toString(), brownClusterer);
		return true;
	}
	
	public boolean addPath(String name, Path path) {
		this.paths.put(name, path);
		return true;
	}
	
	public boolean addToParameterEnvironment(String name, String value) {
		this.parameterEnvironment.put(name, value);
		return true;
	}
	
	public boolean setRandomSeed(long seed) {
		this.randomSeed = seed;
		this.globalRandom.setSeed(this.randomSeed);
		return true;
	}
}
