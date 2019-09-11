package com.constellio.app.ui.pages.management;

import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.entities.navigation.NavigationItem.BaseNavigationItem;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
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

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

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
		String urlEndWith = ((BaseNavigationItem) item).urlNeedToEndWith();

		ComponentState state = presenter.getStateFor(item);

		boolean isUrlOk = true;
		if (urlEndWith != null) {
			String location = Page.getCurrent().getLocation().toString();
			isUrlOk = location.endsWith(urlEndWith);
		}

		button.setEnabled(state.isEnabled() && isUrlOk);
		button.setVisible(state.isVisible() && isUrlOk);
		button.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				item.activate(navigate());
			}
		});
		layout.addComponent(button);
	}
}
