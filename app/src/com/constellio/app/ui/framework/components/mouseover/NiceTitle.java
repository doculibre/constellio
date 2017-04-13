package com.constellio.app.ui.framework.components.mouseover;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.util.ComponentTreeUtils;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.Component;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;

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
			TabSheet tabSheet = ComponentTreeUtils.findParent(component, TabSheet.class);
			if (tabSheet != null) {
				tabSheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {
					@Override
					public void selectedTabChange(SelectedTabChangeEvent event) {
						runJavascript();
					}
				});
			}
			runJavascript();
		}
	}
	
	private void runJavascript() {
		String titleEscaped = StringEscapeUtils.escapeJavaScript(title);
		String componentId = component.getId();
		if (componentId == null) {
			componentId = new UUIDV1Generator().next();
			component.setId(componentId);
		}
		component.addStyleName("nicetitle-link");
		
		StringBuilder js = new StringBuilder();
		String getById = "document.getElementById(\"" + componentId + "\")";
		js.append("if (" + getById + ") {");
		js.append(getById + ".className = " + getById + ".className.replace(\"v-disabled\", \"nicetitle-link-disabled\")");
		js.append(";");
		js.append(getById + ".setAttribute(\"title\", \"" + titleEscaped + "\")");
		js.append(";");
		js.append("makeNiceTitleA(" + getById + ")");
		js.append("}");
		
		JavaScript javascript = JavaScript.getCurrent();
		javascript.execute(js.toString());
	}

	@Override
	public void detach() {
		JavaScript javascript = JavaScript.getCurrent();
		javascript.execute("hideNiceTitle()");
		super.detach();
	}

}
