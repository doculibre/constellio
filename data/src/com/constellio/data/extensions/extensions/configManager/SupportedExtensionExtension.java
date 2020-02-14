package com.constellio.data.extensions.extensions.configManager;

import java.util.Collections;
import java.util.List;

public class SupportedExtensionExtension {

	public List<String> getAdditionalSupportedExtension() {
		return Collections.emptyList();
	}

	public List<String> getExtentionDisabledForPreviewConvertion() {
		return Collections.emptyList();
	}

	public ExtensionConverter getConverter() {
		return null;
	}
}