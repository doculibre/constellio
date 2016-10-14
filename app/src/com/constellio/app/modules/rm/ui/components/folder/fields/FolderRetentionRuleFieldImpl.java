package com.constellio.app.modules.rm.ui.components.folder.fields;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.ui.builders.RetentionRuleToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;

public class FolderRetentionRuleFieldImpl extends CustomField<String> implements FolderRetentionRuleField {
	private final String collection;
	private List<String> options;
	private Table table;
	private String value;

	public FolderRetentionRuleFieldImpl(String collection) {
		this.collection = collection;
		options = new ArrayList<>();
	}

	@Override
	protected Component initContent() {
		table = new Generator().attachedTo(new Table());
		table.setWidth("100%");
		return table;
	}

	@Override
	public Class<? extends String> getType() {
		return String.class;
	}

	@Override
	public List<String> getOptions() {
		return options;
	}

	@Override
	public void setOptions(List<String> options) {
		this.options = options;
		table.removeAllItems();
		RetentionRuleToVOBuilder builder = getBuilder();
		for (Record rule : loadRetentionRules(options)) {
			table.addItem(builder.build(rule, VIEW_MODE.TABLE, sessionContext()));
		}
		table.setPageLength(options.size());
	}

	@Override
	public String getFieldValue() {
		return value;
	}

	@Override
	public void setValue(String newFieldValue)
			throws ReadOnlyException, ConversionException {
		value = newFieldValue;
		super.setValue(newFieldValue);
		table.refreshRowCache();
	}

	@Override
	public void setFieldValue(Object value) {
		setValue((String) value);
	}

	private List<Record> loadRetentionRules(List<String> options) {
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(types().getSchemaType(RetentionRule.SCHEMA_TYPE)).where(Schemas.IDENTIFIER).isIn(options))
				.sortAsc(Schemas.CODE);
		return modelLayerFactory().newSearchServices().search(query);
	}

	private RetentionRuleToVOBuilder getBuilder() {
		return new RetentionRuleToVOBuilder(appLayerFactory(), types().getDefaultSchema(Category.SCHEMA_TYPE),
				types().getDefaultSchema(UniformSubdivision.SCHEMA_TYPE));
	}

	private MetadataSchemaTypes types() {
		return modelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
	}

	private AppLayerFactory appLayerFactory() {
		return ConstellioFactories.getInstance().getAppLayerFactory();
	}

	private ModelLayerFactory modelLayerFactory() {
		return ConstellioFactories.getInstance().getModelLayerFactory();
	}

	private SessionContext sessionContext() {
		return ConstellioUI.getCurrentSessionContext();
	}

	private class Generator implements ColumnGenerator {
		public static final String SELECTOR = "selector";
		public static final String CODE = "code";
		public static final String TITLE = "title";
		public static final String DESCRIPTION = "description";

		public Table attachedTo(Table table) {
			table.addGeneratedColumn(SELECTOR, this);
			table.setColumnHeader(SELECTOR, "");
			table.setColumnWidth(SELECTOR, 50);

			table.addGeneratedColumn(CODE, this);
			table.setColumnHeader(CODE, $("FolderRetentionRuleField.code"));

			table.addGeneratedColumn(TITLE, this);
			table.setColumnHeader(TITLE, $("FolderRetentionRuleField.title"));

			table.addGeneratedColumn(DESCRIPTION, this);
			table.setColumnHeader(DESCRIPTION, $("FolderRetentionRuleField.description"));
			table.setColumnExpandRatio(DESCRIPTION, 1);

			return table;
		}

		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			if (columnId == null) {
				return null;
			}
			RetentionRuleVO rule = (RetentionRuleVO) itemId;
			switch ((String) columnId) {
			case SELECTOR:
				return generateSelectorCell(rule);
			case CODE:
				return generateCodeCell(rule);
			case TITLE:
				return generateTitleCell(rule);
			case DESCRIPTION:
				return generateDescriptionCell(rule);
			}
			return null;
		}

		private Object generateSelectorCell(final RetentionRuleVO rule) {
			final CheckBox box = new CheckBox();
			if (rule.getId().equals(getInternalValue())) {
				box.setValue(true);
				box.setEnabled(false);
			}
			box.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					if (box.getValue()) {
						setValue(rule.getId());
					}
				}
			});
			return box;
		}

		private Object generateCodeCell(RetentionRuleVO rule) {
			return rule.getCode();
		}

		private Object generateTitleCell(RetentionRuleVO rule) {
			return rule.getTitle();
		}

		private Object generateDescriptionCell(RetentionRuleVO rule) {
			return rule.getDescription();
		}
	}
}
