package com.constellio.app.modules.rm.model.calculators.group;

import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.StringMetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.schemas.Schemas;

import java.util.List;

import static java.util.Arrays.asList;

public class GroupCaptionCalculator extends StringMetadataValueCalculator {
	ReferenceDependency<String> parentCaptionDependency = ReferenceDependency
			.toAString(Group.PARENT, Schemas.CAPTION.getLocalCode());

	LocalDependency<String> titleDependency = LocalDependency.toAString(Group.TITLE);

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
