package com.constellio.app.api.extensions;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.dom4j.Element;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class XmlDataSourceExtension {

	public void addExtraMetadataInformation(XmlDataSourceExtensionExtraMetadataInformationParams params) {
	}

	public Map<String, List<Record>> getExtraReferences(
			XmlDataSourceExtensionExtraReferencesParams extraMetadataParams) {
		return Collections.emptyMap();
	}

	@AllArgsConstructor
	@Getter
	public static class XmlDataSourceExtensionExtraMetadataInformationParams {
		private final Element element;
		private final Metadata metadata;
		private final Record record;
		private final String nullValue;
	}

	@AllArgsConstructor
	@Getter
	public static class XmlDataSourceExtensionExtraReferencesParams {
		private final Record record;
	}

}
