package com.constellio.app.modules.rm.model.calculators.document;

import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.StringMetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.Schemas;

import java.util.List;

import static java.util.Arrays.asList;

public class DocumentCaptionCalculator extends StringMetadataValueCalculator {

	ReferenceDependency<String> parentCaptionDependency = ReferenceDependency
			.toAString(Document.FOLDER, Schemas.CAPTION.getLocalCode());

	LocalDependency<String> titleDependency = LocalDependency.toAString(Document.TITLE).whichIsRequired();

	@Override
	public String calculate(CalculatorParameters parameters) {
		String parentCaption = parameters.get(parentCaptionDependency);
		String title = parameters.get(titleDependency);

		if (parentCaption == null) {
			return title;
		} else {
			return parentCaption + " / " + title;
		}
	}

	@Override
	public List<? extends Dependency> getDependencies() {
		return asList(parentCaptionDependency, titleDependency);
	}
}
