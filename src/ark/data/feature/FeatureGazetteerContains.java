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

import ark.data.annotation.Datum;

/**
 * For datum d, string extractor S, and gazetteer G, 
 * FeatureGazetteerContains computes
 * 
 * max_{g\in G} 1(g=S(d))
 * 
 * @author Bill McDowell
 *
 * @param <D> datum type
 * @param <L> datum label type
 */
public class FeatureGazetteerContains<D extends Datum<L>, L> extends FeatureGazetteer<D, L> { 
	public FeatureGazetteerContains() {
		this.extremumType = FeatureGazetteer.ExtremumType.Maximum;
	}
	
	@Override
	protected double computeExtremum(String str) {
		if (this.gazetteer.contains(str))
			return 1.0;
		else 
			return 0.0;
	}
	
	@Override
	public String getGenericName() {
		return "GazetteerContains";
	}
	
	@Override
	protected Feature<D, L> makeInstance() {
		return new FeatureGazetteerContains<D, L>();
	}
}
