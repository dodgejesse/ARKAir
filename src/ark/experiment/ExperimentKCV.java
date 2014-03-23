package ark.experiment;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ark.data.annotation.DataSet;
import ark.data.annotation.Datum;
import ark.data.annotation.Datum.Tools;
import ark.data.feature.Feature;
import ark.model.SupervisedModel;
import ark.model.evaluation.KFoldCrossValidation;
import ark.util.SerializationUtil;

public class ExperimentKCV<D extends Datum<L>, L> extends Experiment<D, L> {
	protected SupervisedModel<D, L> model;
	protected List<Feature<D, L>> features;
	protected int crossValidationFolds;
	protected Datum.Tools.TokenSpanExtractor<D, L> errorExampleExtractor;
	protected Map<String, List<String>> gridSearchParameterValues;
	
	public ExperimentKCV(String name, String inputPath, Tools<D, L> datumTools) {
		super(name, inputPath, datumTools);
		
		this.features = new ArrayList<Feature<D, L>>();
		this.gridSearchParameterValues = new HashMap<String, List<String>>();
	}
	
	@Override
	protected boolean execute(DataSet<D, L> data) {
		KFoldCrossValidation<D, L> validation = new KFoldCrossValidation<D, L>(
			this.name,
			this.model,
			this.features,
			data,
			this.crossValidationFolds, 
			this.random
		);
		
		for (Entry<String, List<String>> entry : this.gridSearchParameterValues.entrySet())
			for (String value : entry.getValue())
			validation.addPossibleHyperParameterValue(entry.getKey(), value);
		
		if (validation.run(this.maxThreads, this.errorExampleExtractor) < 0)
			return false;

		return true;
	}
	@Override
	protected boolean deserializeNext(Reader reader, String nextName) throws IOException {
		if (nextName.equals("crossValidationFolds")) {
			this.crossValidationFolds = Integer.valueOf(SerializationUtil.deserializeAssignmentRight(reader));
		
		} else if (nextName.equals("model")) {
			String modelName = SerializationUtil.deserializeGenericName(reader);
			SupervisedModel<D, L> model = this.datumTools.makeModelInstance(modelName);
			if (!model.deserialize(reader, false, false, this.datumTools))
				return false;
			this.model = model;
		
		} else if (nextName.equals("feature")) {
			String modelName = SerializationUtil.deserializeGenericName(reader);
			this.model = this.datumTools.makeModelInstance(modelName);
			if (!this.model.deserialize(reader, false, false, this.datumTools))
				return false;
		} else if (nextName.equals("errorExampleExtractor")) {
			this.errorExampleExtractor = this.datumTools.getTokenSpanExtractor(
					SerializationUtil.deserializeAssignmentRight(reader));
			
		} else if (nextName.equals("gridSearchParameterValues")) {
			String parameterName = SerializationUtil.deserializeGenericName(reader);
			List<String> parameterValues = SerializationUtil.deserializeList(reader);
			this.gridSearchParameterValues.put(parameterName, parameterValues);
		
		}
		
		return false;
	}
}
