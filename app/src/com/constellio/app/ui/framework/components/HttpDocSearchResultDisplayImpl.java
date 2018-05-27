package com.constellio.app.ui.framework.components;

import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpDocSearchResultDisplayImpl extends SearchResultDisplay {
    public HttpDocSearchResultDisplayImpl(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory, AppLayerFactory appLayerFactory, String query) {
        super(searchResultVO, componentFactory, appLayerFactory, query);
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

        List<String> parts = new ArrayList<>(highlights.size());
        for (Map.Entry<String, List<String>> entry : highlights.entrySet()) {
            if (highlightedDateStoreCodes.contains(entry.getKey())) {
                String description = searchResultVO.getRecordVO().get(metadata);
                parts.add(copyHighlightedPortion(StringUtils.join((List<String>)entry.getValue(), " "), description));
            }
        }
        String formattedHighlights = StringUtils.join(parts, SEPARATOR);

        if(StringUtils.isNotBlank(formattedHighlights)) {
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
