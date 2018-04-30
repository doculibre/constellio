package com.constellio.app.ui.framework.components.capsule;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;

public class CapsuleComponent extends Panel {

	public CapsuleComponent(String title, String htmlContent) {
		addStyleName("information-capsule");
		Label content = new Label(htmlContent, ContentMode.HTML);
		content.addStyleName("information-capsule-content");
		setCaption(title);
		setContent(content);
	}

}
