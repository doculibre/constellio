package com.constellio.app.ui.framework.components.mouseover;

import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.Component;
import com.vaadin.ui.JavaScript;

@com.vaadin.annotations.JavaScript("theme://nicetitle/nicetitle.js")
@com.vaadin.annotations.StyleSheet("theme://nicetitle/nicetitle.css")
public class NiceTitle extends AbstractExtension {

	private Component component;

	private String title;

	public NiceTitle(Component component, String title) {
		this.component = component;
		this.title = title;
	}

	@Override
	public void attach() {
		super.attach();

		if (StringUtils.isNotBlank(title)) {
			String titleEscaped = StringEscapeUtils.escapeJavaScript(title);
			String componentId = component.getId();
			if (componentId == null) {
				componentId = new UUIDV1Generator().next();
				component.setId(componentId);
			}
			component.addStyleName("nicetitle-link");
			JavaScript javascript = JavaScript.getCurrent();
			javascript.execute(
					"document.getElementById(\"" + componentId + "\").setAttribute(\"title\", \"" + titleEscaped + "\")");
			javascript.execute("makeNiceTitleA(document.getElementById(\"" + componentId + "\"))");
		}
	}

	@Override
	public void detach() {
		JavaScript javascript = JavaScript.getCurrent();
		javascript.execute("hideNiceTitle()");
		super.detach();
	}

}
