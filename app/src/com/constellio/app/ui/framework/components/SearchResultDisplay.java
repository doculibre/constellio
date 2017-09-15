package com.constellio.app.ui.framework.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.SearchResultVO;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

import static com.constellio.app.ui.application.ConstellioUI.getCurrent;

public class SearchResultDisplay extends VerticalLayout {
	public static final String RECORD_STYLE = "search-result-record";
	public static final String TITLE_STYLE = "search-result-title";
	public static final String HIGHLIGHTS_STYLE = "search-result-highlights";
	public static final String METADATA_STYLE = "search-result-metadata";
	public static final String SEPARATOR = " ... ";

	private AppLayerFactory appLayerFactory;
	private SessionContext sessionContext;

	public SearchResultDisplay(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.sessionContext = getCurrent().getSessionContext();
		init(searchResultVO, componentFactory);
	}

	protected void init(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory) {
		addComponents(newTitleComponent(searchResultVO),
				newHighlightsLabel(searchResultVO),
				newMetadataComponent(searchResultVO, componentFactory));
		addStyleName(RECORD_STYLE);
		setWidth("100%");
		setSpacing(true);
	}

	protected Component newTitleComponent(SearchResultVO searchResultVO) {
		ReferenceDisplay title = new ReferenceDisplay(searchResultVO.getRecordVO());
		title.addStyleName(TITLE_STYLE);
		title.setWidthUndefined();
		return title;
	}

	protected Component newMetadataComponent(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory) {
		Component metadata = buildMetadataComponent(searchResultVO.getRecordVO(), componentFactory);
		metadata.addStyleName(METADATA_STYLE);
		return metadata;
	}

	protected Label newHighlightsLabel(SearchResultVO searchResultVO) {
		String formattedHighlights = formatHighlights(searchResultVO.getHighlights(), searchResultVO.getRecordVO());
		Label highlights = new Label(formattedHighlights, ContentMode.HTML);
		highlights.addStyleName(HIGHLIGHTS_STYLE);
		if (StringUtils.isBlank(formattedHighlights)) {
			highlights.setVisible(false);
		}
		return highlights;
	}

	private String formatHighlights(Map<String, List<String>> highlights, RecordVO recordVO) {
		if (highlights == null) {
			return null;
		}

		String currentCollection = sessionContext.getCurrentCollection();
		List<String> collectionLanguages = appLayerFactory.getCollectionsManager().getCollectionLanguages(currentCollection);
		MetadataSchema schema = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(currentCollection).getSchema(recordVO.getSchema().getCode());
		SchemasDisplayManager displayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		List<String> highlightedDateStoreCodes = new ArrayList<>();
		for(Metadata metadata: schema.getMetadatas()) {
			if(displayManager.getMetadata(currentCollection, metadata.getCode()).isHighlight()) {
				highlightedDateStoreCodes.add(metadata.getDataStoreCode());
				for(String language: collectionLanguages) {
					highlightedDateStoreCodes.add(metadata.getAnalyzedField(language).getDataStoreCode());
				}
			}
		}
		List<String> parts = new ArrayList<>(highlights.size());
		for(Map.Entry<String, List<String>> entry : highlights.entrySet()) {
			if(highlightedDateStoreCodes.contains(entry.getKey())) {
				parts.add(StringUtils.join(entry.getValue(), SEPARATOR));
			}
		}
		return StringUtils.join(parts, SEPARATOR);
	}

	private Layout buildMetadataComponent(RecordVO recordVO, MetadataDisplayFactory componentFactory) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		for (MetadataValueVO metadataValue : recordVO.getSearchMetadataValues()) {
			MetadataVO metadataVO = metadataValue.getMetadata();
			if (metadataVO.codeMatches(CommonMetadataBuilder.TITLE)) {
				continue;
			}

			Component value = componentFactory.build(recordVO, metadataValue);
			if (value == null) {
				continue;
			}

			Label caption = new Label(metadataVO.getLabel() + ":");
			caption.addStyleName("metadata-caption");

			HorizontalLayout item = new HorizontalLayout(caption, value);
			item.setHeight("100%");
			item.setSpacing(true);
			item.addStyleName("metadata-caption-layout");

			layout.addComponent(item);
		}
		return layout;
	}

	protected AppLayerFactory getAppLayerFactory() {
		return appLayerFactory;
	}
}
