package com.constellio.app.ui.framework.components;

import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
                parts.add(StringUtils.join(entry.getValue(), SEPARATOR));
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
}
