package com.constellio.app.ui.pages.search.batchProcessing.components;

import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class BatchProcessingEntryTable extends CustomField<List<String>> {
	private final BatchProcessingEntryPresenter presenter;
	private TableComponent table;

	public BatchProcessingEntryTable(BatchProcessingEntryPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	protected Component initContent() {
		BeanItemContainer<BatchProcessingEntry> entries = new BeanItemContainer<>(
				BatchProcessingEntry.class, presenter.getEntries());
		table = new TableComponent(entries);
		table.setWidth("100%");
		return table;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<? extends List<String>> getType() {
		return (Class) List.class;
	}

	public void refreshEntries() {
		table.removeAllItems();
		table.addItems(presenter.getEntries());
		table.sort(new Object[] { table.getSortContainerPropertyId() }, new boolean[] { true });
	}

	public interface BatchProcessingEntryPresenter {

		List<BatchProcessingEntry> getEntries();
	}

	private static class TableComponent extends Table implements ColumnGenerator {
		public static final String LABEL = "label";
		public static final String TYPE = "type";
		public static final String INDEX = "index";
		public static final String VALUE = "value";
		public static final String MAPPING = "mapping";
		public static final String DEFAULT = "default";

		public TableComponent(BeanItemContainer<BatchProcessingEntry> entries) {
			super($("BatchProcessingEntryTable.caption", entries.size()), entries);

			setColumnHeader(LABEL, $("BatchProcessingEntryTable.label"));

			addGeneratedColumn(TYPE, this);
			setColumnHeader(TYPE, $("BatchProcessingEntryTable.type"));

			addGeneratedColumn(INDEX, this);
			setColumnHeader(INDEX, $("BatchProcessingEntryTable.index"));

			addGeneratedColumn(VALUE, this);
			setColumnHeader(VALUE, $("BatchProcessingEntryTable.value"));

			addGeneratedColumn(MAPPING, this);
			setColumnHeader(MAPPING, $("BatchProcessingEntryTable.mapping"));

			addGeneratedColumn(DEFAULT, this);
			setColumnHeader(DEFAULT, $("BatchProcessingEntryTable.default"));

			setVisibleColumns(LABEL, TYPE, INDEX, VALUE, MAPPING, DEFAULT);
			setColumnExpandRatio(LABEL, 1);
			setSortContainerPropertyId(LABEL);
			setSortAscending(true);
		}

		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			if (TYPE.equals(columnId)) {
				return buildTypeComponent((BatchProcessingEntry) itemId);
			}
			if (VALUE.equals(columnId)) {
				return buildValueComponent((BatchProcessingEntry) itemId);
			}
			if (INDEX.equals(columnId)) {
				return buildIndexComponent((BatchProcessingEntry) itemId);
			}
			if (MAPPING.equals(columnId)) {
				return buildMappingComponent((BatchProcessingEntry) itemId);
			}
			if (DEFAULT.equals(columnId)) {
				return buildDefaultComponent((BatchProcessingEntry) itemId);
			}
			return null;
		}

		private Object buildTypeComponent(BatchProcessingEntry entry) {
			EnumWithSmallCodeComboBox<BatchProcessingAction> box = new EnumWithSmallCodeComboBox<>(BatchProcessingAction.class);
			box.setPropertyDataSource(new MethodProperty(entry, "mappingType"));
			box.setNullSelectionAllowed(false);
			box.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					refreshRowCache();
				}
			});
			return box;
		}

		private Object buildIndexComponent(BatchProcessingEntry entry) {
			TextField field = new BaseTextField(new MethodProperty<String>(entry, "index"));
			if (entry.getBatchProcessingAction().isDirectOrIndirect()) {
				field.setEnabled(true);
				field.setRequired(true);
			} else {
				field.setEnabled(false);
				field.setRequired(false);
				field.setValue(null);
			}
			return field;
		}

		private Object buildValueComponent(BatchProcessingEntry entry) {
			TextField field = new BaseTextField(new MethodProperty<String>(entry, "fixedValue"));
			if (entry.getBatchProcessingAction().isFixed()) {
				field.setEnabled(true);
				field.setRequired(true);
			} else {
				field.setEnabled(false);
				field.setRequired(false);
				field.setValue(null);
			}
			return field;
		}

		private Object buildMappingComponent(BatchProcessingEntry entry) {
			LookupRecordField field = new LookupRecordField("mappingTable");
			if (entry.getBatchProcessingAction().isIndirect()) {
				field.setEnabled(true);
			} else {
				field.setEnabled(false);
				field.setValue(null);
			}
			return field;
		}

		private Object buildDefaultComponent(BatchProcessingEntry entry) {
			TextField field = new BaseTextField(new MethodProperty<String>(entry, "defaultValue"));
			if (entry.getBatchProcessingAction().isDirectOrIndirect()) {
				field.setEnabled(true);
			} else {
				field.setEnabled(false);
				field.setValue(null);
			}
			return field;
		}
	}
}
