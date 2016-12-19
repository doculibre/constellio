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
		mainLayout.addStyleName(STYLE_NAME + "-main-layout"); 
		
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
		if (displayComponent.isVisible()) {
			HorizontalLayout captionAndComponentLayout = new HorizontalLayout();
			captionAndComponentLayout.setSizeFull();
			
			mainLayout.addComponent(captionAndComponentLayout);
			captionAndComponentLayout.addComponent(captionLabel);
			captionAndComponentLayout.addComponent(displayComponent);
		}
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
