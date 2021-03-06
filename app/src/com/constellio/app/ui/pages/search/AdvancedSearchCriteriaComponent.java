package com.constellio.app.ui.pages.search;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.components.converters.BaseStringToDoubleConverter;
import com.constellio.app.ui.framework.components.converters.JodaDateTimeToUtilConverter;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.date.BaseDateField;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.components.fields.lookup.PathLookupField;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.mouseover.NiceTitle;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator;
import com.constellio.app.ui.pages.search.criteria.Criterion.SearchOperator;
import com.constellio.app.ui.pages.search.criteria.RelativeCriteria.RelativeSearchOperator;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.wrappers.User;
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
import org.apache.commons.lang.StringUtils;
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

	public void setShowDeactivatedMetadatas(boolean shown) {
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

	public List<Criterion> getSearchCriteriaWithSchemaType() {
		List<Criterion> searchCriteria = getSearchCriteria();

		if (searchCriteria.isEmpty() && container.size() > 0) {
			Criterion criterion = container.getItemIds().iterator().next();
			if (StringUtils.isNotBlank(criterion.getSchemaType()) || StringUtils.isNotBlank(criterion.getSchemaCode())) {
				return Arrays.asList(criterion);
			}
		}

		return searchCriteria;
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
				criterion.clear();
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
			Component cell;
			if (criterion.getMetadataCode() == null) {
				return null;
			}
			Component extensionComponentForCriterion = presenter.getExtensionComponentForCriterion(criterion);
			if (extensionComponentForCriterion != null) {
				cell = extensionComponentForCriterion;
			} else if (criterion.getMetadataCode().endsWith("_schema")) {
				cell = buildSchemaCriterionComponent(criterion);
			} else {
				switch (criterion.getMetadataType()) {
					case STRING:
					case TEXT:
					case STRUCTURE:
						cell = criterion.getSearchOperator() == SearchOperator.IN_HIERARCHY ?
							   buildHierarchyValueCriterion(criterion) :
							   buildStringValueComponent(criterion);
						break;
					case DATE:
					case DATE_TIME:
						cell = buildDateValueComponent(criterion);
						break;
					case NUMBER:
					case INTEGER:
						cell = buildNumberValueComponent(criterion);
						break;
					case BOOLEAN:
						cell = buildBooleanValueComponent(criterion);
						break;
					case ENUM:
						cell = buildEnumValueComponent(criterion);
						break;
					case REFERENCE:
						cell = buildReferenceComponent(criterion);
						break;
					default:
						return null;
				}
			}
			return cell;
		}

		private Component buildReferenceComponent(final Criterion criterion) {
			I18NHorizontalLayout copiedMetadataValueContainer = new I18NHorizontalLayout();
			copiedMetadataValueContainer.setWidth("100%");
			Component referenceValueComponent = buildReferenceValueComponent(criterion, copiedMetadataValueContainer);

			VerticalLayout verticalLayout = new VerticalLayout(referenceValueComponent, copiedMetadataValueContainer);
			verticalLayout.setSpacing(true);

			return verticalLayout;
		}

		private Component buildReferenceValueComponent(final Criterion criterion,
													   I18NHorizontalLayout copiedMetadataValueContainer) {
			MetadataVO metadata = presenter.getMetadataVO(criterion.getMetadataCode());
			final Component referenceValue = buildReferenceEntryField(metadata.getAllowedReferences(), criterion);
			final Component copiedMetadataSelector = buildCopiedMetadataSelector(criterion, copiedMetadataValueContainer);
			final Component operator = buildReferenceOperatorComponent(criterion, referenceValue, copiedMetadataSelector, copiedMetadataValueContainer);

			appendHelpMessage(criterion, referenceValue);


			I18NHorizontalLayout component = new I18NHorizontalLayout(operator, referenceValue, copiedMetadataSelector);
			component.setComponentAlignment(referenceValue, Alignment.MIDDLE_RIGHT);
			component.setComponentAlignment(copiedMetadataSelector, Alignment.MIDDLE_RIGHT);
			component.setExpandRatio(referenceValue, 1);
			component.setExpandRatio(copiedMetadataSelector, 1);
			component.setWidth("100%");
			component.setSpacing(true);

			return component;
		}

		private Component buildReferenceOperatorComponent(final Criterion criterion, Component referenceValue,
														  Component copiedMetadataSelector,
														  I18NHorizontalLayout copiedMetadataValueContainer) {
			final ComboBox operator = buildIsEmptyIsNotEmptyComponentWithoutListener(criterion);
			operator.setWidth("180px");
			if (presenter.isSeparateCopiedMetadata()) {
				operator.addItem(SearchOperator.CONTAINS);
				operator.setItemCaption(SearchOperator.CONTAINS, $("AdvancedSearchView.contains"));
				operator.addItem(SearchOperator.NOT_CONTAINS);
				operator.setItemCaption(SearchOperator.NOT_CONTAINS, $("AdvancedSearchView.notContains"));
			}
			operator.setNullSelectionAllowed(false);
			operator.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					SearchOperator newOperator = (SearchOperator) operator.getValue();
					if (newOperator != null) {
						if (isOperatorOfTypeContains(criterion.getSearchOperator()) != isOperatorOfTypeContains(newOperator)
							|| isOperatorOfTypeEmpty(criterion.getSearchOperator()) != isOperatorOfTypeEmpty(newOperator)) {
							criterion.setValue(null);
							criterion.setEndValue(null);
						}
						criterion.setSearchOperator(newOperator);
						updateReferenceFieldsVisibility(criterion, referenceValue, copiedMetadataSelector, copiedMetadataValueContainer);
					} else {
						criterion.setValue(null);
						criterion.setEndValue(null);

						referenceValue.setVisible(true);
						copiedMetadataSelector.setVisible(false);
						copiedMetadataValueContainer.setVisible(false);
					}
				}
			});
			operator.setValue(criterion.getSearchOperator());
			updateReferenceFieldsVisibility(criterion, referenceValue, copiedMetadataSelector, copiedMetadataValueContainer);

			return operator;
		}

		private void updateReferenceFieldsVisibility(final Criterion criterion, Component referenceValue,
													 Component copiedMetadataSelector,
													 Component copiedMetadataValueContainer) {
			referenceValue.setVisible(!isOperatorOfTypeEmpty(criterion.getSearchOperator())
									  && !isOperatorOfTypeContains(criterion.getSearchOperator()));
			copiedMetadataSelector.setVisible(isOperatorOfTypeContains(criterion.getSearchOperator()));
			copiedMetadataValueContainer.setVisible(canShowCopiedMetadataValueContainer(criterion));
		}

		private Field<?> buildReferenceEntryField(AllowedReferences references, final Criterion criterion) {
			String allowedSchemaType = references.getAllowedSchemaType();
			Set<String> allowedSchemas = references.getAllowedSchemas();
			String firstAllowedSchema = !allowedSchemas.isEmpty() ? allowedSchemas.iterator().next() : null;

			LookupRecordField tmpField = null;
			if (User.SCHEMA_TYPE.equals(allowedSchemaType) || User.DEFAULT_SCHEMA.equals(firstAllowedSchema)) {
				tmpField = new LookupRecordField(allowedSchemaType, firstAllowedSchema, true, true, true, true, null);
			} else {
				tmpField = new LookupRecordField(allowedSchemaType, firstAllowedSchema, true);
			}

			final LookupRecordField field = tmpField;
			field.setWidth("100%");
			field.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					criterion.setValue(field.getValue());
				}
			});
			if (isOperatorOfTypeContains(criterion.getSearchOperator()) || isOperatorOfTypeEmpty(criterion.getSearchOperator())) {
				field.setValue(null);
			} else {
				field.setValue(criterion.getValue());
			}
			return field;
		}

		private Component buildCopiedMetadataSelector(final Criterion criterion,
													  I18NHorizontalLayout copiedMetadataValueContainer) {
			ComboBox comboBox = new BaseComboBox();
			comboBox.addStyleName("advanced-search-form-metadata");
			comboBox.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
			comboBox.setNullSelectionAllowed(false);
			comboBox.setVisible(false);

			if (!presenter.isSeparateCopiedMetadata()
				|| StringUtils.isBlank(criterion.getMetadataCode())) {
				return comboBox;
			}

			for (MetadataVO metadata : presenter.getCopiedMetadataAllowedInCriteria(criterion.getMetadataCode())) {
				MetadataVO sourceMetadata = presenter.getCopiedSourceMetadata(metadata);
				comboBox.addItem(metadata.getCode());
				comboBox.setItemCaption(metadata.getCode(), sourceMetadata.getLabel(ConstellioUI.getCurrentSessionContext().getCurrentLocale()));
			}
			comboBox.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					criterion.setValue(comboBox.getValue());
					if (comboBox.getValue() != null) {
						Criterion subCriterion = (Criterion) criterion.getEndValue();
						if (subCriterion == null || !subCriterion.getMetadataCode().equals(criterion.getValue())) {
							subCriterion = new Criterion(criterion.getSchemaType());
							MetadataVO copiedMetadataVO = presenter.getMetadataVO((String) comboBox.getValue());
							String enumClassName = null;
							if (copiedMetadataVO.getEnumClass() != null) {
								enumClassName = copiedMetadataVO.getEnumClass().getName();
							}
							subCriterion.setMetadata(copiedMetadataVO.getCode(), copiedMetadataVO.getType(), enumClassName);
						}
						criterion.setEndValue(subCriterion);

						copiedMetadataValueContainer.removeAllComponents();
						copiedMetadataValueContainer.addComponent(generateCell(subCriterion));
						copiedMetadataValueContainer.setVisible(isOperatorOfTypeContains(criterion.getSearchOperator()));
					} else {
						copiedMetadataValueContainer.setVisible(false);
						criterion.setEndValue(null);
					}
				}
			});
			if (isOperatorOfTypeContains(criterion.getSearchOperator())) {
				comboBox.setValue(criterion.getValue());
			} else {
				comboBox.setValue(null);
			}
			comboBox.setPageLength(comboBox.size());
			return comboBox;
		}

		private boolean canShowCopiedMetadataValueContainer(Criterion criterion) {
			return criterion.getValue() != null && isOperatorOfTypeContains(criterion.getSearchOperator());
		}

		private boolean isOperatorOfTypeContains(SearchOperator operator) {
			return operator.equals(SearchOperator.CONTAINS) || operator.equals(SearchOperator.NOT_CONTAINS);
		}

		private boolean isOperatorOfTypeEmpty(SearchOperator operator) {
			return operator.equals(SearchOperator.IS_NULL) || operator.equals(SearchOperator.IS_NOT_NULL);
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
			appendHelpMessage(criterion, value);
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
			appendHelpMessage(criterion, component);

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

			appendHelpMessage(criterion, operator);
			return operator;
		}

		private ComboBox buildIsEmptyIsNotEmptyComponent(final Criterion criterion) {
			final ComboBox operator = buildIsEmptyIsNotEmptyComponentWithoutListener(criterion);
			operator.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					criterion.setSearchOperator((SearchOperator) operator.getValue());
				}
			});

			return operator;
		}

		private ComboBox buildIsEmptyIsNotEmptyComponentWithoutListener(final Criterion criterion) {
			final ComboBox operator = new BaseComboBox();
			addIsEmptyIsNotEmpty(criterion, operator);
			operator.setWidth("150px");
			operator.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
			operator.setNullSelectionAllowed(true);
			operator.setValue(criterion.getSearchOperator());

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

		private Component buildDateValueComponent(final Criterion criterion) {
			// We only want to display a date, but we need a full datetime, so we cannot use a JodaDateField here
			Component relativeSearchComponent = buildRelativeSearchComboBox(criterion, false);
			Component endRelativeSearchComponent = buildRelativeSearchComboBox(criterion, true);
			ComboBox operator = buildComparisonComboBox(criterion, relativeSearchComponent, endRelativeSearchComponent);

			appendHelpMessage(criterion, relativeSearchComponent, endRelativeSearchComponent);

			I18NHorizontalLayout horizontalLayout = new I18NHorizontalLayout();
			horizontalLayout.setSpacing(true);
			horizontalLayout.addComponents(operator, relativeSearchComponent);

			VerticalLayout verticalLayout = new VerticalLayout(horizontalLayout, endRelativeSearchComponent);
			verticalLayout.setSpacing(true);

			return verticalLayout;
		}

		private Component buildNumberValueComponent(final Criterion criterion) {
			final TextField value = new BaseTextField();
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
			value.setImmediate(true);

			final TextField endValue = new BaseTextField();
			endValue.setWidth("100px");
			endValue.setNullRepresentation("");
			endValue.setConverter(new BaseStringToDoubleConverter());
			endValue.setConvertedValue(criterion.getEndValue());
			endValue.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					verifyEndValue(endValue, criterion);
				}
			});
			endValue.setImmediate(true);

			appendHelpMessage(criterion, value, endValue);

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

		private void verifyEndValue(TextField newValue, Criterion criterion) {
			try {
				criterion.setEndValue(newValue.getConvertedValue());
			} catch (ConversionException e) {
				criterion.setEndValue(null);
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

			final TextField textValue = new BaseTextField();
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

		protected void appendHelpMessage(Criterion criterion, Component... valueFields) {
			for (Component valueField : valueFields) {
				new NiceTitle(presenter.getMetadataVO(criterion.getMetadataCode()).getHelpMessage()).setParent(valueField);
			}
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
			comboBox.setWidth("150px");
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
			TextField field = new BaseTextField();
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

		default List<MetadataVO> getCopiedMetadataAllowedInCriteria(String referenceCode) {
			return getMetadataAllowedInCriteria();
		}

		default MetadataVO getCopiedSourceMetadata(MetadataVO copiedMetadata) {
			return null;
		}

		Map<String, String> getMetadataSchemasList(String schemaTypeCode);

		MetadataVO getMetadataVO(String metadataCode);

		Component getExtensionComponentForCriterion(Criterion criterion);

		void showErrorMessage(String message);

		default boolean isSeparateCopiedMetadata() {
			return false;
		}
	}
}
