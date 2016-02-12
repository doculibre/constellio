package com.constellio.app.modules.rm.ui.pages.agent;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;

public class AgentRequestViewImpl extends BaseViewImpl implements AgentRequestView {
	@Override
	protected String getTitle() {
		return $("AgentRequestView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		Label message = new Label($("AgentRequestView.message"), ContentMode.HTML);

		Link request = new Link($("AgentRequestView.requestAgent"),
				new ExternalResource("mailto:sales@constellio.com?Subject=" + $("AgentRequestView.subject")));

		VerticalLayout layout = new VerticalLayout(message, request);
		layout.setSpacing(true);

		return layout;
	}
}
