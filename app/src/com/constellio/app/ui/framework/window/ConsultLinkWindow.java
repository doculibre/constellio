package com.constellio.app.ui.framework.window;

import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseLabel;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class ConsultLinkWindow extends BaseWindow {
	
	public ConsultLinkWindow(List<String> linkToDisplayList) {
		addStyleName("consultation-link-window");
		setModal(true);
		setWidth("90%");
		setCaption($("consultationLink"));

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.addStyleName("consultation-link-window-layout");
		mainLayout.setSpacing(true);
		mainLayout.addStyleName(WindowButton.WINDOW_CONTENT_STYLE_NAME);

		for (String linkToDisplay : linkToDisplayList) {
			Label linkLabel = new BaseLabel(linkToDisplay, ContentMode.HTML);
			linkLabel.addStyleName("consultation-link-window-link");
			mainLayout.addComponent(linkLabel);
		}

		this.setContent(mainLayout);
	}


}
