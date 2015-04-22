/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
		ReferenceDisplay title = new ReferenceDisplay(searchResultVO.getRecordVO());
		title.addStyleName(TITLE_STYLE);

		Label highlights = new Label(formatHighlights(searchResultVO.getHighlights()), ContentMode.HTML);
		highlights.addStyleName(HIGHLIGHTS_STYLE);

		Component metadata = buildMetadataComponent(searchResultVO.getRecordVO(), componentFactory);
		metadata.addStyleName(METADATA_STYLE);

		addComponents(title, highlights, metadata);
		addStyleName(RECORD_STYLE);
		setWidthUndefined();
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
		for (MetadataValueVO metadataValue : recordVO.getMetadataValues()) {
			MetadataVO metadataVO = metadataValue.getMetadata();
			if (metadataVO.getCode().endsWith("_title")) {
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
