package com.constellio.app.modules.robots.ui.components.criteria;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import com.constellio.app.modules.robots.ui.pages.AddEditRobotPresenter;
import com.constellio.app.ui.pages.search.AdvancedSearchCriteriaComponent;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class AdvancedSearchCriteriaField extends CustomField<List<Criterion>> {
	private final AddEditRobotPresenter presenter;
	private AdvancedSearchCriteriaComponent component;

	public AdvancedSearchCriteriaField(AddEditRobotPresenter presenter) {
		this.presenter = presenter;
		component = new AdvancedSearchCriteriaComponent(presenter);
	}

	public AdvancedSearchCriteriaField setSchemaType(String schemaType) {
		component.setSchemaType(schemaType);
		return this;
	}

	public AdvancedSearchCriteriaField addEmptyCriterion() {
		component.addEmptyCriterion();
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends List<Criterion>> getType() {
		return (Class) List.class;
	}

	@Override
	protected Component initContent() {
		Button addCriterion = new Button($("add"));
		addCriterion.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addCriterionRequested();
			}
		});
		addCriterion.addStyleName(ValoTheme.BUTTON_LINK);

		SearchResultTableComponent searchResultTableComponent = new SearchResultTableComponent(presenter, component,
				presenter.getCurrentUserVO());

		component.setWidth("100%");

		VerticalLayout layout = new VerticalLayout(addCriterion, component, searchResultTableComponent);
		layout.setComponentAlignment(addCriterion, Alignment.TOP_RIGHT);
		layout.setSpacing(true);
		layout.setWidth("100%");

		return layout;
	}

	@Override
	protected void setInternalValue(List<Criterion> newValue) {
		super.setInternalValue(newValue);
		if (newValue.isEmpty()) {
			component.removeAllItems();
			component.addEmptyCriterion();
		} else {
			component.setSearchCriteria(newValue);
		}
	}

	@Override
	protected List<Criterion> getInternalValue() {
		return component.getSearchCriteria();
	}
}
