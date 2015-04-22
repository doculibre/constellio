/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.base;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.framework.buttons.SearchButton;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.handlers.OnEnterKeyHandler;
import com.constellio.app.ui.pages.search.AdvancedSearchCriteriaComponent;
import com.constellio.app.ui.pages.search.AdvancedSearchView;
import com.constellio.app.ui.pages.search.SimpleSearchView;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.event.MouseEvents;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Responsive;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.PopupView.PopupVisibilityListener;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("serial")
public class ConstellioHeaderImpl extends HorizontalLayout implements ConstellioHeader {

	private static final String ID = "header-advanced-search-form";

	private static final String SHOW_ADVANCED_SEARCH_POPUP_HIDDEN_STYLE_NAME = "header-show-advanced-search-button-popup-hidden";
	private static final String SHOW_ADVANCED_SEARCH_POPUP_VISIBLE_STYLE_NAME = "header-show-advanced-search-button-popup-visible";

	private final ConstellioHeaderPresenter presenter;
	private ObjectProperty<String> searchFieldProperty;
	private TextField searchField;
	private ComboBox types;
	private PopupView advancedSearchForm;
	private Button clearAdvancedSearch;
	private AdvancedSearchCriteriaComponent criteria;

	public ConstellioHeaderImpl() {
		presenter = new ConstellioHeaderPresenter(this);

		Image logo = new Image("", new ThemeResource("images/logo_eim_203x30.png"));
		logo.addStyleName("header-logo");
		logo.addClickListener(new MouseEvents.ClickListener() {
			@Override
			public void click(MouseEvents.ClickEvent event) {
				if (event.getButton() == MouseButton.LEFT) {
					ConstellioUI.getCurrent().navigateTo().home();
				}
			}
		});

		searchFieldProperty = new ObjectProperty<String>(null, String.class);

		searchField = new BaseTextField(searchFieldProperty);
		searchField.addStyleName("header-search");
		searchField.addFocusListener(new FocusListener() {
			@Override
			public void focus(FocusEvent event) {
				if (presenter.isValidAdvancedSearchCriterionPresent()) {
					advancedSearchForm.setPopupVisible(true);
				}
			}
		});

		final Button showAdvancedSearch = new Button();
		showAdvancedSearch.addStyleName("header-show-advanced-search-button");
		showAdvancedSearch.addStyleName(ValoTheme.BUTTON_BORDERLESS);
		showAdvancedSearch.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				advancedSearchForm.setPopupVisible(true);
			}
		});
		showAdvancedSearch.addStyleName(SHOW_ADVANCED_SEARCH_POPUP_HIDDEN_STYLE_NAME);

		Button searchButton = new SearchButton();
		searchButton.addStyleName("header-search-button");
		searchButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		searchButton.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.searchRequested(searchField.getValue(), getAdvancedSearchSchemaType());
			}
		});

		OnEnterKeyHandler onEnterHandler = new OnEnterKeyHandler() {
			@Override
			public void onEnterKeyPressed() {
				presenter.searchRequested(searchField.getValue(), getAdvancedSearchSchemaType());
			}
		};
		onEnterHandler.installOn(searchField);

		advancedSearchForm = new PopupView("", buildAdvancedSearchUI());
		advancedSearchForm.setId(ID);
		advancedSearchForm.addStyleName(ID);
		advancedSearchForm.setHideOnMouseOut(false);
		advancedSearchForm.addPopupVisibilityListener(new PopupVisibilityListener() {
			@Override
			public void popupVisibilityChange(PopupVisibilityEvent event) {
				if (event.isPopupVisible()) {
					showAdvancedSearch.removeStyleName(SHOW_ADVANCED_SEARCH_POPUP_HIDDEN_STYLE_NAME);
					showAdvancedSearch.addStyleName(SHOW_ADVANCED_SEARCH_POPUP_VISIBLE_STYLE_NAME);
				} else {
					showAdvancedSearch.removeStyleName(SHOW_ADVANCED_SEARCH_POPUP_VISIBLE_STYLE_NAME);
					showAdvancedSearch.addStyleName(SHOW_ADVANCED_SEARCH_POPUP_HIDDEN_STYLE_NAME);
				}
				adjustSearchFieldContent();
			}
		});

		addComponents(logo, searchField, showAdvancedSearch, searchButton, advancedSearchForm);
		setSizeFull();

		adjustSearchFieldContent();

		UI.getCurrent().getNavigator().addViewChangeListener(new ViewChangeListener() {
			@Override
			public boolean beforeViewChange(ViewChangeEvent event) {
				return true;
			}

			@Override
			public void afterViewChange(ViewChangeEvent event) {
				if (!(event.getNewView() instanceof AdvancedSearchView || event.getNewView() instanceof SimpleSearchView)) {
					searchField.setValue(null);
					types.setValue(null);
					clearAdvancedSearch.setEnabled(false);
				}
			}
		});
	}

	private void adjustSearchFieldContent() {
		if (advancedSearchForm.isPopupVisible()) {
			searchField.setInputPrompt("");
		} else if (presenter.isValidAdvancedSearchCriterionPresent()) {
			searchField.setInputPrompt($("AdvancedSearchView.advancedCriteriaPrompt"));
		} else {
			searchField.setInputPrompt($("AdvancedSearchView.searchPrompt"));
		}
	}

	private Component buildAdvancedSearchUI() {
		Button addCriterion = new Button($("add"));
		addCriterion.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addCriterionRequested();
			}
		});
		addCriterion.addStyleName(ValoTheme.BUTTON_LINK);

		HorizontalLayout top = new HorizontalLayout(buildTypeComponent(), addCriterion);
		top.setComponentAlignment(addCriterion, Alignment.BOTTOM_RIGHT);
		top.setWidth("100%");

		criteria = new AdvancedSearchCriteriaComponent(presenter);
		criteria.addEmptyCriterion();
		criteria.addEmptyCriterion();
		criteria.setWidth("100%");

		Button advancedSearch = new Button($("AdvancedSearchView.search"));
		advancedSearch.addStyleName(ValoTheme.BUTTON_PRIMARY);
		advancedSearch.addStyleName("advanced-search-button");
		advancedSearch.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.searchRequested(searchField.getValue(), getAdvancedSearchSchemaType());
			}
		});

		clearAdvancedSearch = new Button($("AdvancedSearchView.clearAdvancedSearch"));
		clearAdvancedSearch.addStyleName(ValoTheme.BUTTON_LINK);
		clearAdvancedSearch.addStyleName("clear-advanced-search-button");
		clearAdvancedSearch.setEnabled(false);
		clearAdvancedSearch.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				types.setValue(null);
				clearAdvancedSearch.setEnabled(false);
			}
		});

		HorizontalLayout bottom = new HorizontalLayout(advancedSearch, clearAdvancedSearch);
		bottom.addStyleName("header-advanced-search-form-clear-and-search-buttons");
		bottom.setSpacing(true);

		VerticalLayout searchUI = new VerticalLayout(top, criteria, bottom);
		Responsive.makeResponsive(searchUI);
		searchUI.addStyleName("header-advanced-search-form-content");
		searchUI.setWidthUndefined();
		searchUI.setSpacing(true);

		Panel wrapper = new Panel(searchUI);
		wrapper.addStyleName("header-advanced-search-form-content-wrapper");
		return wrapper;
	}

	private Component buildTypeComponent() {
		Label label = new Label($("AdvancedSearchView.type"));

		types = new ComboBox();
		for (MetadataSchemaTypeVO schemaType : presenter.getSchemaTypes()) {
			types.addItem(schemaType.getCode());
			String itemCaption = schemaType.getLabel(ConstellioUI.getCurrentSessionContext().getCurrentLocale());
			types.setItemCaption(schemaType.getCode(), itemCaption);
		}
		types.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
		types.setNullSelectionAllowed(false);
		types.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				presenter.schemaTypeSelected((String) types.getValue());
				clearAdvancedSearch.setEnabled(true);
			}
		});

		HorizontalLayout layout = new HorizontalLayout(label, types);
		layout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
		layout.setSpacing(true);
		return layout;
	}

	@Override
	public void setSearchExpression(String expression) {
		searchField.setValue(expression);
	}

	@Override
	public String getSearchExpression() {
		return searchField.getValue();
	}

	@Override
	public void addEmptyCriterion() {
		criteria.addEmptyCriterion();
	}

	@Override
	public void setAdvancedSearchSchemaType(String schemaTypeCode) {
		criteria.setSchemaType(schemaTypeCode);
	}

	@Override
	public void hideAdvancedSearchPopup() {
		advancedSearchForm.setPopupVisible(false);
	}

	@Override
	public List<Criterion> getAdvancedSearchCriteria() {
		return criteria.getSearchCriteria();
	}

	@Override
	public String getAdvancedSearchSchemaType() {
		return presenter.getSchemaType();
	}

	@Override
	public ConstellioNavigator navigateTo() {
		return new ConstellioNavigator(getUI().getNavigator());
	}

	@Override
	public String getCollection() {
		return ConstellioUI.getCurrentSessionContext().getCurrentCollection();
	}

	@Override
	public ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}
}
