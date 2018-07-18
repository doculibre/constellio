package com.constellio.app.ui.framework.components.capsule;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class CapsuleComponent extends VerticalLayout {

	public CapsuleComponent(String title, String htmlContent) {
		addStyleName("information-capsule");
		Label titleLabel = new Label(title);
		titleLabel.addStyleName("information-capsule-title");
		
		Label contentLabel = new Label(htmlContent, ContentMode.HTML);
		contentLabel.addStyleName("information-capsule-content");
		
		addComponents(titleLabel, contentLabel);
	}

}
