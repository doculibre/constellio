package com.constellio.app.ui.pages.search;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.converters.BaseStringToDoubleConverter;
import com.constellio.app.ui.framework.components.converters.JodaDateTimeToUtilConverter;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.date.BaseDateField;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.components.fields.lookup.PathLookupField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator;
import com.constellio.app.ui.pages.search.criteria.Criterion.SearchOperator;
import com.constellio.app.ui.pages.search.criteria.RelativeCriteria.RelativeSearchOperator;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.schemas.AllowedReferences;
import com.constellio.model.services.search.query.logical.criteria.MeasuringUnitTime;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.i18n.i18n.isRightToLeft;

public class AdvancedSearchCriteriaComponent extends Table {
	public static final String LEFT_PARENS_FIELD = "leftParensField";
	public static final String METADATA_FIELD = "metadataField";
	public static final String VALUE_FIELD = "valueField";
	public static final String RIGHT_PARENS_FIELD = "rightParensField";
	public static final String OPERATOR_FIELD = "operatorField";
	public static final String DELETE_BUTTON = "delete";

	private final BeanItemContainer<Criterion> container;
	private String schemaType;

	public AdvancedSearchCriteriaComponent(SearchCriteriaPresenter presenter) {
		super();

		container = new BeanItemContainer<>(Criterion.class);
		setContainerDataSource(container);

		addGeneratedColumn(LEFT_PARENS_FIELD, new ParensFieldGenerator("leftParens", "("));
		addGeneratedColumn(METADATA_FIELD, new MetadataFieldGenerator(presenter));
		addGeneratedColumn(VALUE_FIELD, new ValueFieldGenerator(presenter));
		addGeneratedColumn(RIGHT_PARENS_FIELD, new ParensFieldGenerator("rightParens", ")"));
		addGeneratedColumn(OPERATOR_FIELD, new OperatorFieldGenerator());
		addGeneratedColumn(DELETE_BUTTON, new DeleteButtonGenerator());

		setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		Object[] visibleColumns = {LEFT_PARENS_FIELD, METADATA_FIELD, VALUE_FIELD, RIGHT_PARENS_FIELD, OPERATOR_FIELD, DELETE_BUTTON};
		if (isRightToLeft()) {
			ArrayUtils.reverse(visibleColumns);
		}
		setVisibleColumns(visibleColumns);
		setColumnExpandRatio(VALUE_FIELD, 1);
	}

	public void setSchemaType(String schemaType) {
		this.schemaType = schemaType;
		for (Criterion criterion : container.getItemIds()) {
			criterion.setSchemaType(schemaType);
		}
		refreshRowCache();
	}

	public void setSchemaSelected(String schemaCode) {
		for (Criterion criterion : container.getItemIds()) {
			criterion.setSchemaSelected(schemaCode);
		}
		refreshRowCache();
	}

	public void setShowDeactivatedMetadatas(boolean isShown) {
		refreshRowCache();

		List<Criterion> itemToRemove = new ArrayList<>();
		for (Criterion criterion : container.getItemIds()) {
			if (criterion.getMetadataCode() == null) {
				itemToRemove.add(criterion);
			}
		}

		for (Criterion criterion : itemToRemove) {
			removeItem(criterion);
		}

		for (int i = container.getItemIds().size(); i < 2; i++) {
			addEmptyCriterion();
		}
	}

	public AdvancedSearchCriteriaComponent addEmptyCriterion() {
		addItem(new Criterion(schemaType));
		setPageLength(size());
		return this;
	}

	public List<Criterion> getSearchCriteria() {
		List<Criterion> result = new ArrayList<>();
		for (Criterion criterion : container.getItemIds()) {
			if (criterion.isNotEmpty()) {
				result.add(criterion);
			}
		}
		return result;
	}

	public void setSearchCriteria(List<Criterion> criteria) {
		container.removeAllItems();
		container.addAll(criteria);
		setPageLength(criteria.size());
	}

	public void clear() {
		setSearchCriteria(new ArrayList<Criterion>());
	}

	public static class MetadataFieldGenerator implements ColumnGenerator {
		private SearchCriteriaPresenter presenter;

		public MetadataFieldGenerator(SearchCriteriaPresenter presenter) {
			this.presenter = presenter;
		}

		@Override
		public Component generateCell(Table source, Object itemId, Object columnId) {
			return buildMetadataField((Criterion) itemId, source);
		}

		private Component buildMetadataField(final Criterion criterion, final Table source) {
			ComboBox comboBox = new BaseComboBox();
			comboBox.addStyleName("advanced-search-form-metadata");
			comboBox.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
			comboBox.setNullSelectionAllowed(false);

			if (criterion.getSchemaType() == null) {
				comboBox.setEnabled(false);
				return comboBox;
			}

			for (MetadataVO metadata : presenter.getMetadataAllowedInCriteria()) {
				comboBox.addItem(metadata);
				comboBox.setItemCaption(metadata, metadata.getLabel(ConstellioUI.getCurrentSessionContext().getCurrentLocale()));
			}
			MetadataVO metadataVO = presenter.getMetadataVO(criterion.getMetadataCode());
			comboBox.setValue(metadataVO);
			comboBox.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					MetadataVO metadataVO = (MetadataVO) event.getProperty().getValue();
					String enumClassName = null;
					if (metadataVO.getEnumClass() != null) {
						enumClassName = metadataVO.getEnumClass().getName();
					}
					criterion.setMetadata(metadataVO.getCode(), metadataVO.getType(), enumClassName);
					source.refreshRowCache();
				}
			});
			//			comboBox.setPageLength(comboBox.size());
			comboBox.setPageLength(20);

			if (!comboBox.containsId(metadataVO)) {
				criterion.Clear();
			}

			return comboBox;
		}
	}

	public static class ValueFieldGenerator implements ColumnGenerator {
		private final SearchCriteriaPresenter presenter;

		public ValueFieldGenerator(SearchCriteriaPresenter presenter) {
			this.presenter = presenter;
		}

		@Override
		public Component generateCell(Table source, Object itemId, Object columnId) {
			return generateCell((Criterion) itemId);
		}

		private Component generateCell(Criterion criterion) {
			if (criterion.getMetadataCode() == null) {
				return null;
			}
			Component extensionComponentForCriterion = presenter.getExtensionComponentForCriterion(criterion);
			if (extensionComponentForCriterion != null) {
				return extensionComponentForCriterion;
			}
			if (criterion.getMetadataCode().endsWith("_schema")) {
				return buildSchemaCriterionComponent(criterion);
			}
			switch (criterion.getMetadataType()) {
				case STRING:
				case TEXT:
					return criterion.getSearchOperator() == SearchOperator.IN_HIERARCHY ?
						   buildHierarchyValueCriterion(criterion) :
						   buildStringValueComponent(criterion);
				case DATE:
				case DATE_TIME:
					return buildDateValueComponent(criterion);
				case NUMBER:
				case INTEGER:
					return buildNumberValueComponent(criterion);
				case BOOLEAN:
					return buildBooleanValueComponent(criterion);
				case ENUM:
					return buildEnumValueComponent(criterion);
				case REFERENCE:
					return buildReferenceValueComponent(criterion);
			}
			return null;
		}

		private Component buildReferenceValueComponent(final Criterion criterion) {
			MetadataVO metadata = presenter.getMetadataVO(criterion.getMetadataCode());

			final Field<?> value = buildReferenceEntryField(metadata.getAllowedReferences(), criterion);

			final ComboBox operator = buildIsEmptyIsNotEmptyComponent(criterion);
			operator.setNullSelectionAllowed(false);
			operator.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					SearchOperator newOperator = (SearchOperator) operator.getValue();
					if (newOperator != null) {
						criterion.setSearchOperator(newOperator);
						value.setVisible(
								!newOperator.equals(SearchOperator.IS_NULL) && !newOperator.equals(SearchOperator.IS_NOT_NULL));
					} else {
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

		private Field<?> buildReferenceEntryField(AllowedReferences references, final Criterion criterion) {
			String allowedSchemaType = references.getAllowedSchemaType();
			Set<String> allowedSchemas = references.getAllowedSchemas();
			String firstAllowedSchema = !allowedSchemas.isEmpty() ? allowedSchemas.iterator().next() : null;

			final LookupRecordField field = new LookupRecordField(allowedSchemaType, firstAllowedSchema, true, true, true, true);
			field.setWindowZIndex(BaseWindow.OVER_ADVANCED_SEARCH_FORM_Z_INDEX);
			field.setWidth("100%");
			field.setValue((String) criterion.getValue());
			field.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					criterion.setValue(field.getValue());
				}
			});
			return field;
		}

		private Component buildSchemaCriterionComponent(final Criterion criterion) {
			final ComboBox value = new BaseComboBox();
			Map<String, String> metadataSchemas = presenter.getMetadataSchemasList(criterion.getSchemaType());
			for (String code : metadataSchemas.keySet()) {
				value.addItem(code);
				value.setItemCaption(code, metadataSchemas.get(code));
			}
			value.setWidth("100%");
			value.setNullSelectionAllowed(false);
			value.setValue(criterion.getValue());
			value.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					criterion.setValue(value.getValue());
				}
			});

			final SearchOperator searchOperator = criterion.getSearchOperator();
			if (SearchOperator.CONTAINS_TEXT.equals(searchOperator)) {
				criterion.setSearchOperator(SearchOperator.EQUALS);
			}

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
						criterion.setSearchOperator(SearchOperator.EQUALS);
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

		private Component buildStringValueComponent(final Criterion criterion) {
			final TextField value = new BaseTextField();
			value.setWidth("100%");
			value.setValue((String) criterion.getValue());
			value.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					criterion.setValue(value.getValue());
				}
			});

			final CheckBox exact = new CheckBox($("AdvancedSearchView.exactMatch"));
			exact.setValue(criterion.getSearchOperator() == SearchOperator.EQUALS);
			exact.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					criterion.setSearchOperator(exact.getValue() ? SearchOperator.EQUALS : SearchOperator.CONTAINS_TEXT);
				}
			});

			final SearchOperator searchOperator = criterion.getSearchOperator();
			final ComboBox operator = buildIsEmptyIsNotEmptyComponent(criterion);
			operator.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					SearchOperator newOperator = (SearchOperator) operator.getValue();
					if (newOperator != null) {
						if (newOperator == SearchOperator.EQUALS || newOperator == SearchOperator.CONTAINS_TEXT) {
							criterion.setSearchOperator(exact.getValue() ? SearchOperator.EQUALS : SearchOperator.CONTAINS_TEXT);
						} else {
							criterion.setSearchOperator(newOperator);
						}
						value.setVisible(
								!newOperator.equals(SearchOperator.IS_NULL) && !newOperator.equals(SearchOperator.IS_NOT_NULL));
						exact.setVisible(
								!newOperator.equals(SearchOperator.IS_NULL) && !newOperator.equals(SearchOperator.IS_NOT_NULL));
					} else {
						criterion.setSearchOperator(searchOperator);
						value.setVisible(true);
						exact.setVisible(true);
					}
				}
			});
			I18NHorizontalLayout component = new I18NHorizontalLayout(operator, value, exact);
			component.setComponentAlignment(exact, Alignment.MIDDLE_RIGHT);
			component.setExpandRatio(value, 1);
			component.setWidth("100%");
			component.setSpacing(true);

			return component;
		}

		private Component buildHierarchyValueCriterion(final Criterion criterion) {
			//getPathField
			final PathLookupField lookup = new PathLookupField(criterion.getSchemaType());
			lookup.setWindowZIndex(BaseWindow.OVER_ADVANCED_SEARCH_FORM_Z_INDEX);
			lookup.setValue((String) criterion.getValue());

			lookup.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					criterion.setValue(lookup.getValue());
				}
			});

			I18NHorizontalLayout component = new I18NHorizontalLayout(lookup);
			component.setExpandRatio(lookup, 1);
			component.setWidth("100%");
			component.setSpacing(true);

			return component;
		}

		private Component buildBooleanValueComponent(final Criterion criterion) {
			final ComboBox operator = new BaseComboBox();
			operator.addItem(SearchOperator.IS_TRUE);
			operator.setItemCaption(SearchOperator.IS_TRUE, $("AdvancedSearchView.isTrue"));
			operator.addItem(SearchOperator.IS_FALSE);
			operator.setItemCaption(SearchOperator.IS_FALSE, $("AdvancedSearchView.isFalse"));
			//			addIsEmptyIsNotEmpty(criterion, operator);
			operator.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
			operator.setNullSelectionAllowed(false);
			operator.setValue(criterion.getSearchOperator());
			operator.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					criterion.setSearchOperator((SearchOperator) operator.getValue());
				}
			});
			return operator;
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

		private Component buildEnumValueComponent(final Criterion criterion) {
			Class<?> enumClass;
			try {
				enumClass = Class.forName(criterion.getEnumClassName());
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
			@SuppressWarnings("unchecked") final ComboBox value = new EnumWithSmallCodeComboBox<>((Class<? extends EnumWithSmallCode>) enumClass);
			value.setWidth("100%");
			value.setNullSelectionAllowed(false);
			value.setValue(criterion.getValue());
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

		private Component buildDateValueComponent(final Criterion criterion) {
			// We only want to display a date, but we need a full datetime, so we cannot use a JodaDateField here
			Component relativeSearchComponent = buildRelativeSearchComboBox(criterion, false);
			Component endRelativeSearchComponent = buildRelativeSearchComboBox(criterion, true);
			ComboBox operator = buildComparisonComboBox(criterion, relativeSearchComponent, endRelativeSearchComponent);

			I18NHorizontalLayout horizontalLayout = new I18NHorizontalLayout();
			horizontalLayout.setSpacing(true);
			horizontalLayout.addComponents(operator, relativeSearchComponent);

			VerticalLayout verticalLayout = new VerticalLayout(horizontalLayout, endRelativeSearchComponent);
			verticalLayout.setSpacing(true);

			return verticalLayout;
		}

		private Component buildNumberValueComponent(final Criterion criterion) {
			final TextField value = new TextField();
			value.setWidth("100px");
			value.setNullRepresentation("");
			value.setConverter(new BaseStringToDoubleConverter());
			value.setConvertedValue(criterion.getValue());
			value.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					verifyNewValue(value, criterion);
				}
			});
			value.setVisible(true);

			final TextField endValue = new TextField();
			endValue.setWidth("100px");
			endValue.setNullRepresentation("");
			endValue.setConverter(new BaseStringToDoubleConverter());
			endValue.setConvertedValue(criterion.getValue());
			endValue.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					verifyNewValue(value, criterion);
				}
			});

			final Label label = new Label($("and"));
			label.setWidth("100px");
			I18NHorizontalLayout horizontalLayoutLabel = new I18NHorizontalLayout();
			horizontalLayoutLabel.addComponent(label);
			horizontalLayoutLabel.setComponentAlignment(label, Alignment.MIDDLE_CENTER);

			I18NHorizontalLayout horizontalLayout = new I18NHorizontalLayout();
			horizontalLayout.setSpacing(true);
			horizontalLayout.addComponents(horizontalLayoutLabel, endValue);

			ComboBox operator = buildComparisonComboBox(criterion, value, horizontalLayout);

			I18NHorizontalLayout component = new I18NHorizontalLayout(operator, value, horizontalLayout);
			component.setSpacing(true);

			return component;
		}

		private void verifyNewValue(TextField newValue, Criterion criterion) {
			try {
				criterion.setValue(newValue.getConvertedValue());
			} catch (ConversionException e) {
				criterion.setValue(null);
				presenter.showErrorMessage($("AdvancedSearchView.invalidDoubleFormat"));
			}
		}

		private ComboBox buildComparisonComboBox(final Criterion criterion, final Component firstComponent,
												 final Component endComponent) {

			Object defaultValue = criterion.getSearchOperator() != null ? criterion.getSearchOperator() : SearchOperator.EQUALS;

			final ComboBox operator = new BaseComboBox();
			operator.setWidth("150px");
			operator.addItem(SearchOperator.EQUALS);
			operator.setItemCaption(SearchOperator.EQUALS, "=");
			operator.addItem(SearchOperator.LESSER_THAN);
			operator.setItemCaption(SearchOperator.LESSER_THAN, "<");
			operator.addItem(SearchOperator.GREATER_THAN);
			operator.setItemCaption(SearchOperator.GREATER_THAN, ">");
			operator.addItem(SearchOperator.BETWEEN);
			operator.setItemCaption(SearchOperator.BETWEEN, $("AdvancedSearchView.between"));
			addIsEmptyIsNotEmpty(criterion, operator);
			operator.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
			operator.setValue(defaultValue);
			operator.setNullSelectionAllowed(false);
			operator.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					configureVisibility(operator, criterion, firstComponent, endComponent);
				}
			});
			configureVisibility(operator, criterion, firstComponent, endComponent);
			return operator;
		}

		private void configureVisibility(ComboBox operator, Criterion criterion, Component firstComponent,
										 Component endComponent) {
			SearchOperator newOperator = (SearchOperator) operator.getValue();
			criterion.setSearchOperator(newOperator);
			criterion.getRelativeCriteria();
			if (firstComponent != null) {
				if (newOperator.equals(SearchOperator.IS_NULL) || newOperator.equals(SearchOperator.IS_NOT_NULL)) {
					firstComponent.setVisible(false);
					endComponent.setVisible(false);
				} else if (newOperator.equals(SearchOperator.BETWEEN)) {
					firstComponent.setVisible(true);
					endComponent.setVisible(true);

				} else {
					firstComponent.setVisible(true);
					endComponent.setVisible(false);
				}
			}
		}

		private Component buildRelativeSearchComboBox(final Criterion criterion, final boolean isEndValue) {

			Object value;
			final MeasuringUnitTime measuringUnitTimeValue;
			if (!isEndValue) {
				value = criterion.getValue();
				measuringUnitTimeValue = criterion.getRelativeCriteria().getMeasuringUnitTime();
			} else {
				value = criterion.getEndValue();
				measuringUnitTimeValue = criterion.getRelativeCriteria().getEndMeasuringUnitTime();
			}

			final DateField date = new BaseDateField();
			date.setWidth("150px");
			date.setConverter(new JodaDateTimeToUtilConverter());
			try {
				date.setConvertedValue(value);
			} catch (Exception e) {
			}
			date.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					if (!isEndValue) {
						criterion.setValue(expandToBeforeMidnight((LocalDateTime) date.getConvertedValue()));
					} else {
						criterion.setEndValue(expandToBeforeMidnight((LocalDateTime) date.getConvertedValue()));
					}

				}
			});

			final TextField textValue = new TextField();
			textValue.setWidth("100px");
			textValue.setNullRepresentation("");
			try {
				textValue.setConvertedValue(value);
			} catch (Exception e) {
			}
			textValue.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					Object convertedValue = textValue.getConvertedValue();
					if (!isEndValue) {
						criterion.setValue(convertedValue);
					} else {
						criterion.setEndValue(convertedValue);
					}
				}
			});

			final ComboBox measuringTimeField = new BaseComboBox();
			measuringTimeField.setWidth("150px");
			measuringTimeField.addItem(MeasuringUnitTime.DAYS);
			measuringTimeField.setItemCaption(MeasuringUnitTime.DAYS, $("MeasuringUnitTime.D"));
			measuringTimeField.addItem(MeasuringUnitTime.WEEKS);
			measuringTimeField.setItemCaption(MeasuringUnitTime.WEEKS, $("MeasuringUnitTime.W"));
			measuringTimeField.addItem(MeasuringUnitTime.MONTHS);
			measuringTimeField.setItemCaption(MeasuringUnitTime.MONTHS, $("MeasuringUnitTime.M"));
			measuringTimeField.addItem(MeasuringUnitTime.YEARS);
			measuringTimeField.setItemCaption(MeasuringUnitTime.YEARS, $("MeasuringUnitTime.Y"));
			measuringTimeField.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					MeasuringUnitTime measuringUnitTime = (MeasuringUnitTime) measuringTimeField.getValue();
					if (!isEndValue) {
						criterion.getRelativeCriteria().setMeasuringUnitTime(measuringUnitTime);
					} else {
						criterion.getRelativeCriteria().setEndMeasuringUnitTime(measuringUnitTime);
					}
				}
			});
			measuringTimeField.setValue(measuringUnitTimeValue);

			Object defaultRelativeSearchOperatorValue;
			if (isEndValue) {
				defaultRelativeSearchOperatorValue = criterion.getRelativeCriteria().getEndRelativeSearchOperator() != null ?
													 criterion.getRelativeCriteria().getEndRelativeSearchOperator() :
													 RelativeSearchOperator.EQUALS;
			} else {
				defaultRelativeSearchOperatorValue = criterion.getRelativeCriteria().getRelativeSearchOperator() != null ?
													 criterion.getRelativeCriteria().getRelativeSearchOperator() :
													 RelativeSearchOperator.EQUALS;
			}
			final ComboBox relativeSearchOperatorCombo = new BaseComboBox();
			relativeSearchOperatorCombo.setWidth("150px");
			relativeSearchOperatorCombo.addItem(RelativeSearchOperator.EQUALS);
			relativeSearchOperatorCombo.setItemCaption(RelativeSearchOperator.EQUALS,
					$("AdvancedSearchView.relativeSearchOperatorCombo.equals"));
			relativeSearchOperatorCombo.addItem(RelativeSearchOperator.TODAY);
			relativeSearchOperatorCombo.setItemCaption(RelativeSearchOperator.TODAY,
					$("AdvancedSearchView.relativeSearchOperatorCombo.today"));
			relativeSearchOperatorCombo.addItem(RelativeSearchOperator.PAST);
			relativeSearchOperatorCombo.setItemCaption(RelativeSearchOperator.PAST,
					$("AdvancedSearchView.relativeSearchOperatorCombo.past"));
			relativeSearchOperatorCombo.addItem(RelativeSearchOperator.FUTURE);
			relativeSearchOperatorCombo.setItemCaption(RelativeSearchOperator.FUTURE,
					$("AdvancedSearchView.relativeSearchOperatorCombo.future"));
			relativeSearchOperatorCombo.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
			relativeSearchOperatorCombo.setValue(defaultRelativeSearchOperatorValue);
			relativeSearchOperatorCombo.setNullSelectionAllowed(false);
			relativeSearchOperatorCombo.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					configureVisibility(relativeSearchOperatorCombo, date, textValue, isEndValue, criterion, measuringTimeField);
				}
			});

			final Label label = new Label($("and"));
			label.setWidth("150px");
			I18NHorizontalLayout horizontalLayoutLabel = new I18NHorizontalLayout();
			horizontalLayoutLabel.addComponent(label);
			horizontalLayoutLabel.setComponentAlignment(label, Alignment.MIDDLE_CENTER);
			horizontalLayoutLabel.setVisible(isEndValue);

			I18NHorizontalLayout component = new I18NHorizontalLayout(horizontalLayoutLabel, relativeSearchOperatorCombo, date, textValue,
					measuringTimeField);
			component.setComponentAlignment(horizontalLayoutLabel, Alignment.MIDDLE_CENTER);
			component.setSpacing(true);
			component.setVisible(!isEndValue);

			configureVisibility(relativeSearchOperatorCombo, date, textValue, isEndValue, criterion, measuringTimeField);

			return component;
		}

		private void configureVisibility(ComboBox relativeSearchOperatorCombo, DateField date, TextField textValue,
										 boolean isEndValue, Criterion criterion, ComboBox measuringTimeField) {
			RelativeSearchOperator newRelativeSearchOperator = (RelativeSearchOperator) relativeSearchOperatorCombo
					.getValue();
			if (newRelativeSearchOperator.equals(RelativeSearchOperator.EQUALS)) {
				date.setVisible(true);
				textValue.setVisible(false);
			} else if (newRelativeSearchOperator.equals(RelativeSearchOperator.TODAY)) {
				date.setVisible(false);
				textValue.setVisible(false);
				if (!isEndValue) {
					LocalDateTime ldt = TimeProvider.getLocalDateTime();
					criterion.setValue(ldt);
				} else {
					LocalDateTime ldt = TimeProvider.getLocalDateTime();
					criterion.setEndValue(ldt);
				}
			} else {
				date.setVisible(false);
				textValue.setVisible(true);
			}
			measuringTimeField.setVisible(textValue.isVisible());
			if (!isEndValue) {
				criterion.getRelativeCriteria().setRelativeSearchOperator(newRelativeSearchOperator);
			} else {
				criterion.getRelativeCriteria().setEndRelativeSearchOperator(newRelativeSearchOperator);
			}
		}

		private LocalDateTime trimToMidnight(LocalDateTime date) {
			return date.withTime(0, 0, 0, 0);
		}

		private LocalDateTime expandToBeforeMidnight(LocalDateTime date) {
			return date.withTime(23, 59, 59, 999);
		}
	}

	public static class OperatorFieldGenerator implements ColumnGenerator {
		@Override
		public Component generateCell(Table source, Object itemId, Object columnId) {
			return buildOperatorField((Criterion) itemId);
		}

		private Component buildOperatorField(final Criterion criterion) {
			ComboBox comboBox = new BaseComboBox();
			comboBox.addItem(BooleanOperator.AND);
			comboBox.setItemCaption(BooleanOperator.AND, $("AdvancedSearchView.AND"));
			comboBox.addItem(BooleanOperator.OR);
			comboBox.setItemCaption(BooleanOperator.OR, $("AdvancedSearchView.OR"));
			comboBox.addItem(BooleanOperator.AND_NOT);
			comboBox.setItemCaption(BooleanOperator.AND_NOT, $("AdvancedSearchView.AND_NOT"));
			comboBox.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
			comboBox.setNullSelectionAllowed(false);
			comboBox.setValue(criterion.getBooleanOperator());
			comboBox.setWidth("100px");
			comboBox.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					criterion.setBooleanOperator((BooleanOperator) event.getProperty().getValue());
				}
			});
			return comboBox;
		}
	}

	public static class ParensFieldGenerator implements ColumnGenerator {
		private final String propertyId;
		private final String symbol;

		public ParensFieldGenerator(String propertyId, String symbol) {
			this.propertyId = propertyId;
			this.symbol = symbol;
		}

		@Override
		public Component generateCell(final Table source, final Object itemId, Object columnId) {
			TextField field = new TextField();
			boolean active = (boolean) source.getItem(itemId).getItemProperty(propertyId).getValue();
			field.setValue(active ? symbol : "");
			field.setWidth("25px");
			field.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					source.getItem(itemId).getItemProperty(propertyId).setValue(symbol.equals(event.getProperty().getValue()));
				}
			});
			return field;
		}
	}

	public static class DeleteButtonGenerator implements ColumnGenerator {
		public static final Resource ICON_RESOURCE = new ThemeResource("images/commun/supprimer.gif");

		@Override
		public Object generateCell(final Table source, final Object itemId, Object columnId) {
			Button delete = new IconButton(ICON_RESOURCE, $("delete"), true) {
				@Override
				protected void buttonClick(ClickEvent event) {
					source.removeItem(itemId);
					source.setPageLength(source.size());
				}
			};
			delete.setEnabled(source.size() > 1);
			return delete;
		}
	}

	public interface SearchCriteriaPresenter extends Serializable {
		void addCriterionRequested();

		List<MetadataVO> getMetadataAllowedInCriteria();

		Map<String, String> getMetadataSchemasList(String schemaTypeCode);

		MetadataVO getMetadataVO(String metadataCode);

		Component getExtensionComponentForCriterion(Criterion criterion);

		void showErrorMessage(String message);
	}
}
