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
import java.util.Locale;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.fields.comment.RecordCommentsEditorImpl;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class RecordDisplay extends BaseDisplay {
	public static final String STYLE_NAME = "record-display";
	private RecordVO recordVO;
	private MetadataDisplayFactory metadataDisplayFactory;

	public RecordDisplay(RecordVO recordVO) {
		this(recordVO, new MetadataDisplayFactory(), STYLE_NAME);
	}

	public RecordDisplay(RecordVO recordVO, MetadataDisplayFactory metadataDisplayFactory) {
		this(recordVO, metadataDisplayFactory, STYLE_NAME);
	}

	public RecordDisplay(RecordVO recordVO, MetadataDisplayFactory metadataDisplayFactory, String styleName) {
		super(toCaptionsAndComponents(recordVO, metadataDisplayFactory));
		this.recordVO = recordVO;
		this.metadataDisplayFactory = metadataDisplayFactory;
		addStyleName(styleName);
	}

	private static List<CaptionAndComponent> toCaptionsAndComponents(RecordVO recordVO,
			MetadataDisplayFactory metadataDisplayFactory) {
		List<CaptionAndComponent> captionsAndComponents = new ArrayList<CaptionAndComponent>();

		Locale locale = ConstellioUI.getCurrentSessionContext().getCurrentLocale();
		for (MetadataValueVO metadataValue : recordVO.getDisplayMetadataValues()) {
			Component displayComponent = metadataDisplayFactory.build(recordVO, metadataValue);
			if (displayComponent != null) {
				MetadataVO metadata = metadataValue.getMetadata();
				String caption = metadata.getLabel(locale);
				Label captionLabel = new Label(caption);

				String captionId = STYLE_CAPTION + "-" + metadata.getCode();
				captionLabel.setId(captionId);
				captionLabel.addStyleName(captionId);

				String valueId = STYLE_VALUE + "-" + metadata.getCode();
				displayComponent.setId(valueId);
				displayComponent.addStyleName(valueId);

				captionsAndComponents.add(new CaptionAndComponent(captionLabel, displayComponent));
			}
		}
		return captionsAndComponents;
	}

	@Override
	protected void addCaptionAndDisplayComponent(Label captionLabel, Component displayComponent) {
		if (displayComponent instanceof RecordCommentsEditorImpl) {
			VerticalLayout verticalLayout = new VerticalLayout(displayComponent);
			verticalLayout.setWidth("100%");
			verticalLayout.setSpacing(true);
			mainLayout.addComponent(verticalLayout);
		} else {
			super.addCaptionAndDisplayComponent(captionLabel, displayComponent);
		}
	}

	public void refresh() {
		setCaptionsAndComponents(toCaptionsAndComponents(this.recordVO, metadataDisplayFactory));
	}

}
