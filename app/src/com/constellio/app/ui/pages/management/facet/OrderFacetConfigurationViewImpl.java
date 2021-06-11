package com.constellio.app.ui.pages.management.facet;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.components.breadcrumb.BaseBreadcrumbTrail;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.pages.management.searchConfig.SearchConfigurationViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public class OrderFacetConfigurationViewImpl extends BaseViewImpl implements OrderFacetConfigurationView {
	private OrderFacetConfigurationPresenter presenter;
	private ListSelect listSelect;
	public static final String BUTTONS_LAYOUT = "base-form-buttons-layout";
	public static final String SAVE_BUTTON = "base-form-save";
	public static final String CANCEL_BUTTON = "base-form-cancel";
	public static final ThemeResource UP_IMG = new ThemeResource("images/icons/actions/up_grey.png");
	public static final ThemeResource DOWN_IMG = new ThemeResource("images/icons/actions/down_grey.png");
	private HorizontalLayout buttonLayout;

	public OrderFacetConfigurationViewImpl() {
		presenter = new OrderFacetConfigurationPresenter(this);
	}

	@Override
	protected String getTitle() {
		return $("OrderFacetConfigurationView.viewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		HorizontalLayout subLayout = new HorizontalLayout();
		subLayout.setSizeFull();
		subLayout.setSpacing(true);
		subLayout.addComponents(facetList());
		subLayout.addComponent(getSideButtonLayout());

		mainLayout.addComponent(subLayout);
		Layout formButton = getFormButton();

		if (isAddButtonsToStaticFooter()) {
			ConstellioUI.getCurrent().setStaticFooterContent(formButton);
		} else {
			mainLayout.addComponent(formButton);
		}


		return mainLayout;
	}

	private boolean isAddButtonsToStaticFooter() {
		return !isInWindow() && !ConstellioUI.getCurrent().isNested();
	}

	private Layout getFormButton() {
		buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);


		Button save = new Button($("save"));
		save.addStyleName(SAVE_BUTTON);
		save.addStyleName(ValoTheme.BUTTON_PRIMARY);
		save.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.saveButtonClicked();
			}
		});

		Button cancel = new Button($("cancel"));
		cancel.addStyleName(CANCEL_BUTTON);
		cancel.addStyleName(ValoTheme.BUTTON_PRIMARY);
		cancel.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.cancelButtonClicked();
			}
		});

		buttonLayout.addComponent(save);
		buttonLayout.addComponent(cancel);
		buttonLayout.addStyleName(BUTTONS_LAYOUT);

		return buttonLayout;
	}

	private Component getSideButtonLayout() {
		VerticalLayout verticalLayout = new VerticalLayout();

		Button up = new IconButton(UP_IMG, "", true, false) {
			@Override
			protected void buttonClick(ClickEvent event) {
				String value = (String) listSelect.getValue();
				if (value != null && !value.isEmpty()) {
					presenter.swap(value, -1);
					refreshList();
					listSelect.select(value);
				}
			}
		};
		up.setWidth("35px");
		up.addStyleName("upanddownposition");

		Button down = new IconButton(DOWN_IMG, "", false, false) {
			@Override
			protected void buttonClick(ClickEvent event) {
				String value = (String) listSelect.getValue();
				if (value != null && !value.isEmpty()) {
					presenter.swap(value, 1);
					refreshList();
					listSelect.select(value);
				}
			}
		};
		down.setWidth("35px");
		down.addStyleName("upanddownposition");

		verticalLayout.addComponent(up);
		verticalLayout.addComponent(down);

		verticalLayout.addStyleName("upanddownpositionlayout");

		return verticalLayout;
	}

	private void refreshList() {
		listSelect.removeAllItems();

		for (String code : presenter.getFacetTitle()) {
			listSelect.addItem(code);
			String tmp = presenter.getLabelForCode(code);
			listSelect.setItemCaption(code, tmp);
		}
	}

	private ListSelect facetList() {
		listSelect = new ListSelect("");
		listSelect.setNullSelectionAllowed(false);
		listSelect.setWidth("100%");

		refreshList();

		return listSelect;
	}

	@Override
	protected BaseBreadcrumbTrail buildBreadcrumbTrail() {
		return SearchConfigurationViewImpl.getFacetListBreadCrumbTrail(this, getTitle());
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
	}
}
