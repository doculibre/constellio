package com.constellio.app.ui.framework.components.mouseover;

import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.vaadin.server.AbstractExtension;
import com.vaadin.server.ClientConnector;
import com.vaadin.ui.Component;
import com.vaadin.ui.JavaScript;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

@com.vaadin.annotations.JavaScript("theme://nicetitle/nicetitle.js")
@com.vaadin.annotations.StyleSheet("theme://nicetitle/nicetitle.css")
public class NiceTitle extends AbstractExtension {

	public static final String DISABLED_STYLE = "nicetitle-link-disabled";

	private Component component;

	private String title;

	private boolean visibleWhenDisabled;

	public NiceTitle(String title) {
		this(title, true);
	}

	public NiceTitle(String title, boolean visibleWhenDisabled) {
		this.title = title;
		this.visibleWhenDisabled = visibleWhenDisabled;
	}

	@Override
	public void beforeClientResponse(boolean initial) {
		super.beforeClientResponse(initial);
		runJavascript();
	}

	@Override
	public void setParent(ClientConnector parent) {
		super.setParent(parent);
		this.component = (Component) parent;
	}

	private void runJavascript() {
		String componentId = component.getId();
		if (componentId == null) {
			componentId = new UUIDV1Generator().next();
			component.setId(componentId);
		}
		String niceTitleStyleName = "nicetitle-link";
		boolean emptyTitle = StringUtils.isBlank(title) || "null".equals(title);
		boolean alreadyApplied = component.getStyleName() != null && component.getStyleName().contains(niceTitleStyleName);
		if (component.isVisible() && !alreadyApplied && !emptyTitle) {
			component.addStyleName(niceTitleStyleName);

			StringBuilder js = new StringBuilder();
			String getById = "document.getElementById(\"" + componentId + "\")";
			js.append("if (" + getById + ") {");
			if (visibleWhenDisabled) {
				js.append(getById + ".className = " + getById + ".className.replace(\"v-disabled\", \"" + DISABLED_STYLE + "\")");
				js.append(";");
			}
			String titleEscaped = StringEscapeUtils.escapeJavaScript(title);
			js.append(getById + ".setAttribute(\"title\", \"" + titleEscaped + "\")");
			js.append(";");
			js.append("makeNiceTitleA(" + getById + ")");
			js.append("}");

			JavaScript javascript = JavaScript.getCurrent();
			javascript.execute(js.toString());
		}
	}

	@Override
	public void detach() {
		try {
			JavaScript javascript = JavaScript.getCurrent();
			javascript.execute("hideNiceTitle()");
		} finally {
			super.detach();
		}
	}

}
