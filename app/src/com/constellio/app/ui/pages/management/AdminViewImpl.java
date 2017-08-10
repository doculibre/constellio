package com.constellio.app.ui.pages.management;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.framework.components.bpmn.BpmnModeler;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class AdminViewImpl extends BaseViewImpl implements AdminView {
	private final AdminPresenter presenter;

	public AdminViewImpl() {
		presenter = new AdminPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("AdminView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout layout = new VerticalLayout();
		layout.addStyleName("view-group");
		layout.addComponent(new BpmnModeler());
		layout.addComponent(buildButtonPanel(presenter.getCollectionItems()));
		if (presenter.isSystemSectionVisible()) {
			Label systemSectionTitle = new Label($("AdminView.systemSectionTitle"));
			systemSectionTitle.addStyleName(ValoTheme.LABEL_H1);
			layout.addComponents(systemSectionTitle, buildButtonPanel(presenter.getSystemItems()));
		}
		return layout;
	}

	private Component buildButtonPanel(List<NavigationItem> items) {
		CssLayout layout = new CssLayout();
		for (NavigationItem item : items) {
			buildButton(layout, item);
		}
		return layout;
	}

	private void buildButton(Layout layout, final NavigationItem item) {
		Button button = new Button($("AdminView." + item.getCode()), new ThemeResource(item.getIcon()));
		button.addStyleName(ValoTheme.BUTTON_ICON_ALIGN_TOP);
		button.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		button.addStyleName(item.getCode());
		ComponentState state = presenter.getStateFor(item);
		button.setEnabled(state.isEnabled());
		button.setVisible(state.isVisible());
		button.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				item.activate(navigate());
			}
		});
		layout.addComponent(button);
	}
}
