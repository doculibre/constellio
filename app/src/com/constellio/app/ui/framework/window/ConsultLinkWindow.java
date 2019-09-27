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
		setModal(true);
		setWidth("70%");
		setCaption($("consultationLink"));

		StringBuilder stringBuilder = new StringBuilder();

		for (String linkToDisplay : linkToDisplayList) {
			stringBuilder.append(linkToDisplay + "<br><br>");
		}

		Label label = new BaseLabel(stringBuilder.toString(), ContentMode.HTML);

		VerticalLayout mainLayout = new VerticalLayout();

		mainLayout.addComponent(label);
		mainLayout.setSpacing(true);
		mainLayout.addStyleName(WindowButton.WINDOW_CONTENT_STYLE_NAME);


		this.setContent(mainLayout);
	}


}
