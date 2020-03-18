package com.constellio.app.modules.rm.ui.pages.externallink;

import com.constellio.app.modules.rm.wrappers.ExternalLink;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;

public class ExternalLinkSourceViewImpl extends BaseViewImpl implements ExternalLinkSourceView {
	private final ExternalLinkSourcePresenter presenter;

	private Map<String, String> titles;
	private VerticalLayout layoutContainer;

	public ExternalLinkSourceViewImpl(String source, String folderId) {
		presenter = new ExternalLinkSourcePresenter(this);
		presenter.forParams(source, folderId);

		titles = new HashMap<>();
	}

	@Override
	protected boolean isBreadcrumbsVisible() {
		return false;
	}

	public void addTitle(String source, String title) {
		titles.put(source, title);
	}

	@Override
	protected String getTitle() {
		if (titles.containsKey(presenter.getSource())) {
			return titles.get(presenter.getSource());
		}

		return $("ExternalLinkSourceView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		layoutContainer = new VerticalLayout();

		Label errorLabel = new Label($("ExternalLinkSourceView.noSource"));
		errorLabel.addStyleName("error-label");
		layoutContainer.addComponent(errorLabel);

		return layoutContainer;
	}

	public String getSource() {
		return presenter.getSource();
	}

	public void setMainLayout(Component layout) {
		layoutContainer.removeAllComponents();
		layoutContainer.addComponent(layout);
	}

	public void closeWindow() {
		if (isInWindow()) {
			ComponentTreeUtils.findParent(this, Window.class).close();
		}
	}

	public void addExternalLinks(List<ExternalLink> links) {
		presenter.addExternalLinks(links);
	}
}
