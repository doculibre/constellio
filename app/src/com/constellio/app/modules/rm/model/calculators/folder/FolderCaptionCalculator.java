package com.constellio.app.modules.rm.model.calculators.folder;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.StringMetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.calculators.dependencies.ReferenceDependency;
import com.constellio.model.entities.schemas.Schemas;

import java.util.List;

import static java.util.Arrays.asList;

public class FolderCaptionCalculator extends StringMetadataValueCalculator {

	ReferenceDependency<String> parentCaptionDependency = ReferenceDependency
			.toAString(Folder.PARENT_FOLDER, Schemas.CAPTION.getLocalCode());

	LocalDependency<String> titleDependency = LocalDependency.toAString(Folder.TITLE).whichIsRequired();

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
