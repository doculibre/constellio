package com.constellio.app.ui.pages.management.facet;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class OrderFacetConfigurationViewImpl extends BaseViewImpl implements OrderFacetConfigurationView {
	private OrderFacetConfigurationPresenter presenter;
	private ListSelect listSelect;
	public static final String BUTTONS_LAYOUT = "base-form-buttons-layout";
	public static final String SAVE_BUTTON = "base-form-save";
	public static final String CANCEL_BUTTON = "base-form-cancel";

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
		mainLayout.addComponent(getFormButton());
		return mainLayout;
	}

	private Component getFormButton() {
		HorizontalLayout mainLayout = new HorizontalLayout();
		mainLayout.setSpacing(true);

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

		mainLayout.addComponent(save);
		mainLayout.addComponent(cancel);
		mainLayout.addStyleName(BUTTONS_LAYOUT);

		return mainLayout;
	}

	private Component getSideButtonLayout() {
		VerticalLayout verticalLayout = new VerticalLayout();

		Button up = new Button($("OrderFacetConfigurationView.up"));
		up.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				String value = (String) listSelect.getValue();
				if (value != null && !value.isEmpty()) {
					presenter.swap(value, -1);
					refreshList();
					listSelect.select(value);
				}
			}
		});

		Button down = new Button($("OrderFacetConfigurationView.down"));
		down.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				String value = (String) listSelect.getValue();
				if (value != null && !value.isEmpty()) {
					presenter.swap(value, 1);
					refreshList();
					listSelect.select(value);
				}
			}
		});

		verticalLayout.addComponent(up);
		verticalLayout.addComponent(down);

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
		listSelect.setWidth("100%");

		refreshList();

		return listSelect;
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
	}
}
