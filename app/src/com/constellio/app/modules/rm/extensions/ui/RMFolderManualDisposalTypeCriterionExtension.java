package com.constellio.app.modules.rm.extensions.ui;

import com.constellio.app.api.extensions.SearchCriterionExtension;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.mouseover.NiceTitle;
import com.constellio.app.ui.pages.base.BasePresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.app.ui.pages.search.criteria.Criterion.SearchOperator;
import com.constellio.model.services.records.RecordServices;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;

import java.util.Arrays;

import static com.constellio.app.ui.i18n.i18n.$;

public class RMFolderManualDisposalTypeCriterionExtension extends SearchCriterionExtension {

	AppLayerFactory appLayerFactory;
	String collection;
	RMSchemasRecordsServices rmSchemasRecordsServices;
	RecordServices recordServices;

	public RMFolderManualDisposalTypeCriterionExtension(AppLayerFactory appLayerFactory, String collection) {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
		this.rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
	}

	@Override
	public Component getComponentForCriterion(Criterion criterion) {
		Component component = null;

		if (criterion.getSchemaType().equals(Folder.SCHEMA_TYPE)
			&& criterion.getMetadataCode().endsWith(Folder.MANUAL_DISPOSAL_TYPE)) {
			component = buildComponentForManualDisposalType(criterion);
		}

		return component;
	}

	private Component buildComponentForManualDisposalType(final Criterion criterion) {
		Class<?> enumClass;
		try {
			enumClass = Class.forName(criterion.getEnumClassName());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		@SuppressWarnings("unchecked") final ComboBox value = new EnumWithSmallCodeComboBox<DisposalType>((Class<DisposalType>) enumClass) {
			@Override
			protected boolean isIgnored(String enumCode) {
				return DisposalType.SORT.getCode().equals(enumCode);
			}
		};
		value.setWidth("100%");
		value.setNullSelectionAllowed(false);
		value.setValue(criterion.getValue());
		appendHelpMessage(criterion, value);
		value.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				criterion.setValue(value.getValue());
			}
		});
		//value.setVisible();

		final SearchOperator searchOperator = criterion.getSearchOperator();
		final ComboBox operator = buildIsEmptyIsNotEmptyComponent(criterion);
		operator.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				SearchOperator newOperator = (SearchOperator) operator.getValue();
				if (newOperator != null) {
					criterion.setSearchOperator(newOperator);
					boolean visible = !newOperator.equals(SearchOperator.IS_NULL) && !newOperator.equals(SearchOperator.IS_NOT_NULL);
					value.setVisible(visible);
					if (!visible) {
						criterion.setValue(null);
					}
				} else {
					criterion.setSearchOperator(searchOperator);
					value.setVisible(true);
				}
			}
		});
		I18NHorizontalLayout component = new I18NHorizontalLayout(operator, value);
		component.setComponentAlignment(value, Alignment.MIDDLE_RIGHT);
		component.setExpandRatio(value, 1);
		component.setWidth("100%");
		component.setSpacing(true);

		return component;
	}

	private ComboBox buildIsEmptyIsNotEmptyComponent(final Criterion criterion) {
		final ComboBox operator = new BaseComboBox();
		addIsEmptyIsNotEmpty(criterion, operator);
		operator.setWidth("100px");
		operator.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
		operator.setNullSelectionAllowed(true);
		operator.setValue(criterion.getSearchOperator());
		operator.addValueChangeListener(new ValueChangeListener() {
			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				criterion.setSearchOperator((SearchOperator) operator.getValue());
			}
		});

		return operator;
	}

	private void addIsEmptyIsNotEmpty(final Criterion criterion, final ComboBox operator) {

		Object defaultValue = SearchOperator.EQUALS;
		if (Arrays.asList(SearchOperator.EQUALS, SearchOperator.IS_NULL, SearchOperator.IS_NOT_NULL).contains(criterion.getSearchOperator())) {
			defaultValue = criterion.getSearchOperator();
		}

		operator.addItem(SearchOperator.EQUALS);
		operator.setItemCaption(SearchOperator.EQUALS, "=");
		operator.addItem(SearchOperator.IS_NULL);
		operator.setItemCaption(SearchOperator.IS_NULL, $("AdvancedSearchView.isEmpty"));
		operator.addItem(SearchOperator.IS_NOT_NULL);
		operator.setItemCaption(SearchOperator.IS_NOT_NULL, $("AdvancedSearchView.isNotEmpty"));
		operator.setValue(defaultValue);
	}

	protected void appendHelpMessage(Criterion criterion, Component... valueFields) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		BasePresenterUtils presenterUtils = new BasePresenterUtils(ConstellioUI.getCurrent().getConstellioFactories(), sessionContext);
		for (Component valueField : valueFields) {
			new NiceTitle(
					presenterUtils.presenterService().getMetadataVO(criterion.getMetadataCode(), sessionContext)
							.getHelpMessage()).setParent(valueField);
		}
	}
}
