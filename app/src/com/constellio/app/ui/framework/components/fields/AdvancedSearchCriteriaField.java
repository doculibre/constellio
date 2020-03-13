package com.constellio.app.ui.framework.components.fields;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.pages.search.AdvancedSearchCriteriaComponent;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.model.entities.Language;
import com.vaadin.data.Property;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;


public class AdvancedSearchCriteriaField extends CustomField<List<Criterion>> {
	private AdvancedSearchCriteriaComponent advancedSearchCriteriaComponent;
	private AdvancedSearchCriteriaFieldPresenter presenter;
	private ComboBox types;

	public AdvancedSearchCriteriaField(ConstellioFactories constellioFactories) {
		presenter = new AdvancedSearchCriteriaFieldPresenter(this, constellioFactories);
		advancedSearchCriteriaComponent = new AdvancedSearchCriteriaComponent(presenter);
	}

	@Override
	protected Component initContent() {

		types = new ComboBox();
		for (MetadataSchemaTypeVO schemaType : presenter.getSchemaTypes()) {
			types.addItem(schemaType.getCode());
			String itemCaption = schemaType.getLabel(Language.withCode(ConstellioUI.getCurrentSessionContext().getCurrentLocale().getLanguage()));
			types.setItemCaption(schemaType.getCode(), itemCaption);
		}
		types.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
		types.setNullSelectionAllowed(false);
		types.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				String typesValue = (String) types.getValue();
				presenter.selectSchemaType(typesValue);
				advancedSearchCriteriaComponent.setSchemaType(typesValue);
			}
		});

		Button addCriterion = new Button($("add"));
		addCriterion.addClickListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				presenter.addCriterionRequested();
			}
		});
		addCriterion.addStyleName(ValoTheme.BUTTON_LINK);

		VerticalLayout mainLayout = new VerticalLayout();

		types.setWidth("250px");

		HorizontalLayout hLayout = new HorizontalLayout();

		hLayout.addComponent(types);
		hLayout.addComponent(addCriterion);
		hLayout.setWidth("100%");

		hLayout.setComponentAlignment(addCriterion, Alignment.TOP_RIGHT);
		hLayout.setComponentAlignment(types, Alignment.MIDDLE_LEFT);

		mainLayout.addComponent(hLayout);
		mainLayout.addComponent(advancedSearchCriteriaComponent);
		mainLayout.setSpacing(true);
		advancedSearchCriteriaComponent.setSizeFull();

		return mainLayout;

	}

	public AdvancedSearchCriteriaComponent getAdvancedCriterionComponent() {
		return advancedSearchCriteriaComponent;
	}


	@Override
	public Class<? extends List<Criterion>> getType() {
		return (Class) List.class;
	}

	@Override
	protected void setInternalValue(List<Criterion> newValue) {
		super.setInternalValue(newValue);
		if (newValue == null || newValue.isEmpty()) {
			advancedSearchCriteriaComponent.removeAllItems();
			advancedSearchCriteriaComponent.addEmptyCriterion();
		} else {
			advancedSearchCriteriaComponent.setSearchCriteria(newValue);
		}
	}

	@Override
	protected List<Criterion> getInternalValue() {
		return advancedSearchCriteriaComponent.getSearchCriteriaWithSchemaType();
	}
}
