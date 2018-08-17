package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.builders.RetentionRuleToVOBuilder;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.table.BaseTable;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.vaadin.data.Property;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

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
		table = new BaseTable("FolderRetentionRuleTable");
		new Generator().attachedTo(table);
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
		for (RetentionRule rule : loadRetentionRules(options)) {
			table.addItem(builder.build(rule.getWrappedRecord(), VIEW_MODE.TABLE, sessionContext()));
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

	@Override
	protected void setInternalValue(String newValue) {
		super.setInternalValue(newValue);
		this.value = newValue;
	}

	private List<RetentionRule> loadRetentionRules(List<String> options) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory());
		List<RetentionRule> retentionRules = new ArrayList<>(rm.getRetentionRules(options));

		Collections.sort(retentionRules, new Comparator<RetentionRule>() {
			@Override
			public int compare(RetentionRule o1, RetentionRule o2) {
				return LangUtils.nullableNaturalCompare(o1.getCode(), o2.getCode());
			}
		});
		return retentionRules;
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

		private Object generateDescriptionCell(final RetentionRuleVO rule) {
			//			return rule.getDescription();
			final String descriptionCaption = rule.getMetadata(RetentionRule.DESCRIPTION).getLabel(sessionContext().getCurrentLocale());
			WindowButton descriptionPopUp = new WindowButton(StringUtils.defaultIfBlank(rule.getDescription(), ""), descriptionCaption, WindowButton.WindowConfiguration.modalDialog("50%", "40%")) {
				@Override
				protected Component buildWindowContent() {
					VerticalLayout mainLayout = new VerticalLayout();
					mainLayout.setSpacing(true);
					TextArea description = new TextArea(descriptionCaption);
					description.setSizeFull();
					description.setWidth("98%");
					description.setValue(rule.getDescription());
					description.setReadOnly(true);

					TextArea comments = new TextArea(rule.getMetadata(RetentionRule.COPY_RULES_COMMENT).getLabel(sessionContext().getCurrentLocale()));
					comments.setSizeFull();
					comments.setWidth("98%");
					StringBuilder sb = new StringBuilder();
					for (String stringValue : rule.getCopyRulesComment()) {
						if (sb.length() > 0) {
							sb.append("\n");
						}
						sb.append(stringValue);
					}
					comments.setValue(sb.toString());
					comments.setReadOnly(true);

					mainLayout.addComponents(description, comments);
					return mainLayout;
				}
			};
			descriptionPopUp.addStyleName(ValoTheme.BUTTON_BORDERLESS);
			return descriptionPopUp;
		}
	}
}
