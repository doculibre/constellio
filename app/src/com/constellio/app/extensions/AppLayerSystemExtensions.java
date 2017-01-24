package com.constellio.app.extensions;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.api.extensions.LabelTemplateExtension;
import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.UpdateModeExtension;
import com.constellio.app.api.extensions.params.DecorateMainComponentAfterInitExtensionParams;
import com.constellio.app.api.extensions.params.PagesComponentsExtensionParams;
import com.constellio.app.extensions.sequence.AvailableSequence;
import com.constellio.app.extensions.sequence.AvailableSequenceForSystemParams;
import com.constellio.app.extensions.sequence.SystemSequenceExtension;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;

public class AppLayerSystemExtensions {

	public VaultBehaviorsList<PagesComponentsExtension> pagesComponentsExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<SystemSequenceExtension> systemSequenceExtensions = new VaultBehaviorsList<>();

	public VaultBehaviorsList<LabelTemplateExtension> labelTemplateExtensions = new VaultBehaviorsList<>();

	public List<AvailableSequence> getAvailableSequences() {

		AvailableSequenceForSystemParams params = new AvailableSequenceForSystemParams();

		List<AvailableSequence> availableSequences = new ArrayList<>();

		for (SystemSequenceExtension extension : systemSequenceExtensions) {
			List<AvailableSequence> extensionSequences = extension.getAvailableSequences(params);
			if (extensionSequences != null) {
				availableSequences.addAll(extensionSequences);
			}
		}

		return availableSequences;
	}

	public void decorateView(PagesComponentsExtensionParams params) {
		for (PagesComponentsExtension extension : pagesComponentsExtensions) {
			extension.decorateView(params);
		}
	}

	public void decorateMainComponentBeforeViewInstanciated(DecorateMainComponentAfterInitExtensionParams params) {
		for (PagesComponentsExtension extension : pagesComponentsExtensions) {
			extension.decorateMainComponentBeforeViewInstanciated(params);
		}
	}

	public void decorateMainComponentAfterViewAssembledOnViewEntered(DecorateMainComponentAfterInitExtensionParams params) {
		for (PagesComponentsExtension extension : pagesComponentsExtensions) {
			extension.decorateMainComponentAfterViewAssembledOnViewEntered(params);
		}
	}

	public void decorateMainComponentBeforeViewAssembledOnViewEntered(DecorateMainComponentAfterInitExtensionParams params) {
		for (PagesComponentsExtension extension : pagesComponentsExtensions) {
			extension.decorateMainComponentBeforeViewAssembledOnViewEntered(params);
		}
	}

	public UpdateModeExtension alternateUpdateMode = new UpdateModeExtension();
	
	public void addLabelTemplates(String schemaType, List<LabelTemplate> labelTemplates) {
		for (LabelTemplateExtension extension : labelTemplateExtensions) {
			extension.addLabelTemplates(schemaType, labelTemplates);
		}
	}
	
}
