package com.constellio.app.modules.rm.ui.pages.personalspace;

import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class PersonnalSpaceViewImpl extends BaseViewImpl implements PersonnalSpaceView {

	private final PersonnalSpacePresenter presenter;

	public PersonnalSpaceViewImpl() {
		presenter = new PersonnalSpacePresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("PersonnalSpaceView.title");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {

		CssLayout layout = new CssLayout();
		buildButtonPanel(presenter.getPersonnalSpaceItems(), layout);

		return layout;
	}

	private void buildButtonPanel(List<NavigationItem> items, Layout layout) {
		for (NavigationItem item : items) {
			buildButton(layout, item);
		}
	}


	private void buildButton(Layout layout, final NavigationItem item) {
		Button button = new Button($("PersonnalSpaceView." + item.getCode()), new ThemeResource(item.getIcon()));
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
