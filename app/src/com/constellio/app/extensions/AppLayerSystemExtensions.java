package com.constellio.app.extensions;

import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.UpdateModeExtension;
import com.constellio.app.api.extensions.params.DecorateMainComponentAfterInitExtensionParams;
import com.constellio.app.api.extensions.params.PagesComponentsExtensionParams;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;

public class AppLayerSystemExtensions {

	public VaultBehaviorsList<PagesComponentsExtension> pagesComponentsExtensions = new VaultBehaviorsList<>();

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
}
