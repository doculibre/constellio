package com.constellio.app.ui.framework.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

public class SearchResultDisplay extends VerticalLayout {
	public static final String RECORD_STYLE = "search-result-record";
	public static final String TITLE_STYLE = "search-result-title";
	public static final String HIGHLIGHTS_STYLE = "search-result-highlights";
	public static final String METADATA_STYLE = "search-result-metadata";
	public static final String SEPARATOR = " ... ";

	public SearchResultDisplay(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory) {
		init(searchResultVO, componentFactory);
	}

	protected void init(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory) {
		addComponents(newTitleComponent(searchResultVO),
				newHighlightsLabel(searchResultVO),
				newMetadataComponent(searchResultVO, componentFactory));
		addStyleName(RECORD_STYLE);
		setWidth("100%");
	}

	protected Component newTitleComponent(SearchResultVO searchResultVO) {
		ReferenceDisplay title = new ReferenceDisplay(searchResultVO.getRecordVO());
		title.addStyleName(TITLE_STYLE);
		return title;
	}

	protected Component newMetadataComponent(SearchResultVO searchResultVO, MetadataDisplayFactory componentFactory) {
		Component metadata = buildMetadataComponent(searchResultVO.getRecordVO(), componentFactory);
		metadata.addStyleName(METADATA_STYLE);
		return metadata;
	}

	protected Label newHighlightsLabel(SearchResultVO searchResultVO) {
		Label highlights = new Label(formatHighlights(searchResultVO.getHighlights()), ContentMode.HTML);
		highlights.addStyleName(HIGHLIGHTS_STYLE);
		return highlights;
	}

	private String formatHighlights(Map<String, List<String>> highlights) {
		if (highlights == null) {
			return null;
		}
		List<String> parts = new ArrayList<>(highlights.size());
		for (List<String> fieldHighlights : highlights.values()) {
			parts.add(StringUtils.join(fieldHighlights, SEPARATOR));
		}
		return StringUtils.join(parts, SEPARATOR);
	}

	private Layout buildMetadataComponent(RecordVO recordVO, MetadataDisplayFactory componentFactory) {
		VerticalLayout layout = new VerticalLayout();
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
			item.setSpacing(true);

			layout.addComponent(item);
		}
		return layout;
	}
}
