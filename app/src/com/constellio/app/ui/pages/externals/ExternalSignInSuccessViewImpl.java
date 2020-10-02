package com.constellio.app.ui.pages.externals;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import static com.constellio.app.ui.i18n.i18n.$;

@com.vaadin.annotations.JavaScript("theme://Constellio.js")
public class ExternalSignInSuccessViewImpl extends VerticalLayout implements ExternalSignInSuccessView {

	public ExternalSignInSuccessViewImpl() {
		Label text = new Label(new StringBuilder()
				.append("<div style=\"margin:10px;\">")
				.append("<h1>").append($("ExternalWebSignInSuccess.title")).append("</h1>")
				.append("<p>").append($("ExternalWebSignInSuccess.description")).append("</p>")
				.append("</div>")
				.toString());

		text.setContentMode(ContentMode.HTML);

		addComponent(text);
	}
}
