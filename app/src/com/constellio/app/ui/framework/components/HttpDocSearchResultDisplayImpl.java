package com.constellio.app.ui.framework.components;

import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpDocSearchResultDisplayImpl extends SearchResultDisplay {
	public HttpDocSearchResultDisplayImpl(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory,
										  AppLayerFactory appLayerFactory, String query, boolean noLinks) {
		super(searchResultVO, componentFactory, appLayerFactory, query, noLinks);
	}

	@Override
	protected Label newHighlightsLabel(SearchResultVO searchResultVO) {
		String currentCollection = sessionContext.getCurrentCollection();
		List<String> collectionLanguages = appLayerFactory.getCollectionsManager().getCollectionLanguages(currentCollection);
		MetadataSchema schema = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(currentCollection).getSchema(ConnectorHttpDocument.DEFAULT_SCHEMA);

		Metadata metadata = schema.getMetadata(ConnectorHttpDocument.DESCRIPTION);

		List<String> highlightedDateStoreCodes = new ArrayList<>();
		for (String language : collectionLanguages) {
			highlightedDateStoreCodes.add(metadata.getAnalyzedField(language).getDataStoreCode());
		}

		Map<String, List<String>> highlights = searchResultVO.getHighlights();

		String description = searchResultVO.getRecordVO().get(metadata);

		List<String> parts = new ArrayList<>(highlights.size());
		if (!highlights.isEmpty()) {
			for (Map.Entry<String, List<String>> entry : highlights.entrySet()) {
				if (highlightedDateStoreCodes.contains(entry.getKey())) {
					parts.add(copyHighlightedPortion(StringUtils.join((List<String>) entry.getValue(), " "), description));
				}
			}
		} else {
			parts.add(description);
		}

		String formattedHighlights = StringUtils.join(parts, SEPARATOR);

		if (StringUtils.isNotBlank(formattedHighlights)) {
			Label label = new Label(formattedHighlights, ContentMode.HTML);
			label.addStyleName(HIGHLIGHTS_STYLE);
			if (StringUtils.isBlank(formattedHighlights)) {
				label.setVisible(false);
			}

			return label;
		} else {
			return super.newHighlightsLabel(searchResultVO);
		}
	}

	private String copyHighlightedPortion(String highlihted, String normal) {
		String removedEmTag = StringUtils.removeAll(highlihted, "<em>");
		removedEmTag = StringUtils.removeAll(removedEmTag, "</em>");

		int index = StringUtils.indexOf(normal, removedEmTag);
		return StringUtils.overlay(normal, highlihted, index, removedEmTag.length());
	}
}
