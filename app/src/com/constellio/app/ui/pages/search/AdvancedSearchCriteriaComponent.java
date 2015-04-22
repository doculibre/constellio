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
package com.constellio.app.ui.pages.search;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.components.converters.JodaDateTimeToUtilConverter;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.date.BaseDateField;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.pages.search.criteria.Criterion;
import com.constellio.app.ui.pages.search.criteria.Criterion.BooleanOperator;
import com.constellio.app.ui.pages.search.criteria.Criterion.SearchOperator;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.converter.StringToDoubleConverter;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;

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
		addGeneratedColumn(VALUE_FIELD, new ValueFieldGenerator());
		addGeneratedColumn(RIGHT_PARENS_FIELD, new ParensFieldGenerator("rightParens", ")"));
		addGeneratedColumn(OPERATOR_FIELD, new OperatorFieldGenerator());
		addGeneratedColumn(DELETE_BUTTON, new DeleteButtonGenerator());

		setColumnHeaderMode(ColumnHeaderMode.HIDDEN);
		setVisibleColumns(LEFT_PARENS_FIELD, METADATA_FIELD, VALUE_FIELD, RIGHT_PARENS_FIELD, OPERATOR_FIELD, DELETE_BUTTON);
		setColumnExpandRatio(VALUE_FIELD, 1);
	}

	public void setSchemaType(String schemaType) {
		this.schemaType = schemaType;
		for (Criterion criterion : container.getItemIds()) {
			criterion.setSchemaType(schemaType);
		}
		refreshRowCache();
	}

	public void addEmptyCriterion() {
		addItem(new Criterion(schemaType));
		setPageLength(size());
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
			ComboBox comboBox = new ComboBox();
			comboBox.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
			comboBox.setNullSelectionAllowed(false);

			if (criterion.getSchemaType() == null) {
				comboBox.setEnabled(false);
				return comboBox;
			}

			for (MetadataVO metadata : presenter.getMetadatasAllowedInCriteria()) {
				comboBox.addItem(metadata);
				comboBox.setItemCaption(metadata, metadata.getLabel(ConstellioUI.getCurrentSessionContext().getCurrentLocale()));
			}
			comboBox.setValue(criterion.getMetadata());
			comboBox.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					criterion.setMetadata((MetadataVO) event.getProperty().getValue());
					source.refreshRowCache();
				}
			});
			comboBox.setPageLength(comboBox.size());
			return comboBox;
		}
	}

	public static class ValueFieldGenerator implements ColumnGenerator {
		@Override
		public Component generateCell(Table source, Object itemId, Object columnId) {
			return generateCell((Criterion) itemId);
		}

		private Component generateCell(Criterion criterion) {
			if (criterion.getMetadata() == null) {
				return null;
			}
			switch (criterion.getMetadata().getType()) {
			case STRING:
			case TEXT:
				return buildStringValueComponent(criterion);
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
			MetadataVO metadataVO = criterion.getMetadata();
			final LookupRecordField value = new LookupRecordField(metadataVO.getSchemaTypeCode());
			value.setValue((String) criterion.getValue());
			value.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					criterion.setValue(value.getValue());
				}
			});
			return value;
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

			HorizontalLayout component = new HorizontalLayout(value, exact);
			component.setComponentAlignment(exact, Alignment.MIDDLE_RIGHT);
			component.setExpandRatio(value, 1);
			component.setWidth("100%");
			component.setSpacing(true);

			return component;
		}

		private Component buildBooleanValueComponent(final Criterion criterion) {
			final ComboBox operator = new ComboBox();
			operator.addItem(SearchOperator.IS_TRUE);
			operator.setItemCaption(SearchOperator.IS_TRUE, $("AdvancedSearchView.isTrue"));
			operator.addItem(SearchOperator.IS_FALSE);
			operator.setItemCaption(SearchOperator.IS_FALSE, $("AdvancedSearchView.isFalse"));
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

		private Component buildEnumValueComponent(final Criterion criterion) {
			Class<? extends Enum<?>> enumClass = criterion.getMetadata().getEnumClass();
			final ComboBox value = new EnumWithSmallCodeComboBox((Class) enumClass);
			value.setNullSelectionAllowed(false);
			value.setValue(criterion.getValue());
			value.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					criterion.setValue(value.getValue());
				}
			});
			return value;
		}

		private Component buildDateValueComponent(final Criterion criterion) {
			// We only want to display a date, but we need a full datetime, so we cannot use a JodaDateField here
			final DateField date = new BaseDateField();
			date.setConverter(new JodaDateTimeToUtilConverter());
			date.setConvertedValue(criterion.getValue());
			date.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					criterion.setValue(trimToMidnight((LocalDateTime) date.getConvertedValue()));
				}
			});

			final DateField endDate = new BaseDateField();
			endDate.setConverter(new JodaDateTimeToUtilConverter());
			endDate.setConvertedValue(criterion.getValue());
			endDate.setVisible(SearchOperator.BETWEEN.equals(criterion.getSearchOperator()));
			endDate.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					criterion.setEndValue(expandToBeforeMidnight((LocalDateTime) endDate.getConvertedValue()));
				}
			});

			ComboBox operator = buildComparisonComboBox(criterion, endDate);

			HorizontalLayout component = new HorizontalLayout(operator, date, endDate);
			component.setExpandRatio(date, 1);
			component.setExpandRatio(endDate, 1);
			component.setWidth("100%");
			component.setSpacing(true);

			return component;
		}

		private Component buildNumberValueComponent(final Criterion criterion) {
			final TextField value = new TextField();
			value.setNullRepresentation("");
			value.setConverter(new StringToDoubleConverter());
			value.setConvertedValue(criterion.getValue());
			value.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					criterion.setValue(value.getConvertedValue());
				}
			});

			final TextField endValue = new TextField();
			endValue.setNullRepresentation("");
			endValue.setConverter(new StringToDoubleConverter());
			endValue.setConvertedValue(criterion.getValue());
			endValue.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					criterion.setEndValue(endValue.getConvertedValue());
				}
			});

			ComboBox operator = buildComparisonComboBox(criterion, endValue);

			HorizontalLayout component = new HorizontalLayout(operator, value, endValue);
			component.setExpandRatio(value, 1);
			component.setExpandRatio(endValue, 1);
			component.setWidth("100%");
			component.setSpacing(true);

			return component;
		}

		private ComboBox buildComparisonComboBox(final Criterion criterion, final Component endValue) {
			final ComboBox operator = new ComboBox();
			operator.setWidth("100px");
			operator.addItem(SearchOperator.EQUALS);
			operator.setItemCaption(SearchOperator.EQUALS, "=");
			operator.addItem(SearchOperator.LESSER_THAN);
			operator.setItemCaption(SearchOperator.LESSER_THAN, "<");
			operator.addItem(SearchOperator.GREATER_THAN);
			operator.setItemCaption(SearchOperator.GREATER_THAN, ">");
			operator.addItem(SearchOperator.BETWEEN);
			operator.setItemCaption(SearchOperator.BETWEEN, $("AdvancedSearchView.between"));
			operator.setItemCaptionMode(ItemCaptionMode.EXPLICIT);
			operator.setValue(criterion.getSearchOperator());
			operator.setNullSelectionAllowed(false);
			operator.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					SearchOperator newOperator = (SearchOperator) operator.getValue();
					criterion.setSearchOperator(newOperator);
					endValue.setVisible(newOperator.equals(SearchOperator.BETWEEN));
				}
			});
			endValue.setVisible(SearchOperator.BETWEEN.equals(criterion.getSearchOperator()));
			return operator;
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
			ComboBox comboBox = new ComboBox();
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

		List<MetadataVO> getMetadatasAllowedInCriteria();
	}
}
