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

import java.util.Arrays;

import ark.data.DataTools;
import ark.data.annotation.Datum;
import ark.util.StringUtil;

/**
 * For datum d, string extractor S, and gazetteer G, 
 * FeatureGazetteerPrefixTokens computes
 * 
 * max_{g\in G} 1(S(d) shares k prefix tokens with G)
 * 
 * The value of k is determined by the parameter 'minTokens'
 * 
 * @author Bill McDowell
 *
 * @param <D> datum type
 * @param <L> datum label type
 *
 */
public class FeatureGazetteerPrefixTokens<D extends Datum<L>, L> extends FeatureGazetteer<D, L> {
	private DataTools.StringPairMeasure prefixTokensMeasure;
	private int minTokens;
	
	public FeatureGazetteerPrefixTokens() {
		this.extremumType = FeatureGazetteer.ExtremumType.Maximum;
		
		this.prefixTokensMeasure = new DataTools.StringPairMeasure() {
			public double compute(String str1, String str2) {
				return StringUtil.prefixTokenOverlap(str1, str2);
			}
		};
		
		this.minTokens = 2;
		
		this.parameterNames = Arrays.copyOf(this.parameterNames, this.parameterNames.length + 1);
		this.parameterNames[this.parameterNames.length - 1] = "minTokens";
	}
	
	@Override
	protected double computeExtremum(String str) {
		double tokenPrefixCount = this.gazetteer.max(str, this.prefixTokensMeasure);
		
		if (tokenPrefixCount >= this.minTokens)
			return 1.0;
		else
			return 0.0;
	}

	@Override
	public String getGenericName() {
		return "GazetteerPrefixTokens";
	}

	@Override
	protected Feature<D, L> makeInstance() {
		return new FeatureGazetteerPrefixTokens<D, L>();
	}

	@Override
	protected String getParameterValue(String parameter) {
		String parameterValue = super.getParameterValue(parameter);
		if (parameterValue != null)
			return parameterValue;
		else if (parameter.equals("minTokens"))
			return String.valueOf(this.minTokens);
		return null;
	}

	@Override
	protected boolean setParameterValue(String parameter, String parameterValue, Datum.Tools<D, L> datumTools) {
		if (super.setParameterValue(parameter, parameterValue, datumTools))
			return true;
		else if (parameter.equals("minTokens"))
			this.minTokens = Integer.valueOf(parameterValue);
		else
			return false;
		
		return true;
	}
}
