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

import java.io.Serializable;
import java.util.List;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class BaseDisplay extends CustomComponent {
	
	public static final String STYLE_NAME = "base-display";
	public static final String STYLE_CAPTION = "display-caption";
	public static final String STYLE_VALUE = "display-value";

	protected VerticalLayout mainLayout;

	public BaseDisplay(List<CaptionAndComponent> captionsAndDisplayComponents) {
		addStyleName(STYLE_NAME);
		
		setSizeFull();

		mainLayout = new VerticalLayout();
		mainLayout.setSizeUndefined();
		mainLayout.setSpacing(true);
		
		setCaptionsAndComponents(captionsAndDisplayComponents);

		setCompositionRoot(mainLayout);
	}
	
	protected void setCaptionsAndComponents(List<CaptionAndComponent> captionsAndDisplayComponents) {
		if (mainLayout.iterator().hasNext()) {
			mainLayout.removeAllComponents();
		}
		for (CaptionAndComponent captionAndComponent : captionsAndDisplayComponents) {
			Label captionLabel = captionAndComponent.captionLabel;
			Component displayComponent = captionAndComponent.displayComponent;
			captionLabel.addStyleName(STYLE_CAPTION);
			displayComponent.addStyleName(STYLE_VALUE);
			addCaptionAndDisplayComponent(captionLabel, displayComponent);
		}
	}
	
	protected void addCaptionAndDisplayComponent(Label captionLabel, Component displayComponent) {
		HorizontalLayout captionAndComponentLayout = new HorizontalLayout();
		captionAndComponentLayout.setSizeFull();
		
		mainLayout.addComponent(captionAndComponentLayout);
		captionAndComponentLayout.addComponent(captionLabel);
		captionAndComponentLayout.addComponent(displayComponent);
	}
	
	public static class CaptionAndComponent implements Serializable {
		
		public Label captionLabel;
		
		public Component displayComponent;

		public CaptionAndComponent(Label captionLabel, Component displayComponent) {
			super();
			this.captionLabel = captionLabel;
			this.displayComponent = displayComponent;
		}
		
	}

}
