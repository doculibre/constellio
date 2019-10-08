package com.constellio.app.modules.rm.ui.components.folder.fields;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.ui.framework.components.BaseLabel;
import com.constellio.app.ui.framework.components.converters.RecordIdListToStringConverter;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.constellio.app.ui.framework.components.mouseover.NiceTitle;
import com.constellio.app.ui.i18n.i18n;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class FolderCopyRuleFieldImpl extends CustomField<String> implements FolderCopyRuleField {

	protected Generator generator;
	protected Table table;
	private List<CopyRetentionRule> copyRetentionRules;
	private RecordIdListToStringConverter recordIdListToStringConverter = new RecordIdListToStringConverter("<br />");

	public FolderCopyRuleFieldImpl(List<CopyRetentionRule> copyRetentionRules) {
		this.copyRetentionRules = copyRetentionRules;
	}

	@Override
	public Class<? extends String> getType() {
		return String.class;
	}

	@Override
	public String getFieldValue() {
		return getValue();
	}

	@Override
	public void setFieldValue(Object value) {
		setValue((String) value);
	}

	@Override
	public void setFieldChoices(List<CopyRetentionRule> rules) {
		this.copyRetentionRules = rules;
		if (table != null) {
			updateTable();
		}
	}

	@Override
	protected Component initContent() {
		generator = new Generator();
		table = generator.attachedTo(new Table());
		table.setWidth("100%");
		updateTable();
		return table;
	}

	protected void updateTable() {
		table.removeAllItems();
		boolean showTitleColumn = false;
		for (CopyRetentionRule copyRetentionRule : copyRetentionRules) {
			if (StringUtils.isNotBlank(copyRetentionRule.getTitle())) {
				showTitleColumn = true;
				break;
			}
		}
		if (!showTitleColumn) {
			table.setVisibleColumns(generator.noTitle());
		} else {
			table.setVisibleColumns(generator.allColumns());
		}
		for (CopyRetentionRule rule : copyRetentionRules) {
			table.addItem(rule);
		}
		table.setPageLength(copyRetentionRules.size());
	}

	protected class Generator implements ColumnGenerator {
		public static final String SELECTOR = "selector";
		public static final String RULE = "rule";
		public static final String TITLE = "title";
		public static final String TYPE = "type";
		public static final String MEDIUM_TYPES = "mediumTypes";

		private String[] allColumns() {
			return new String[]{SELECTOR, RULE, TITLE, TYPE, MEDIUM_TYPES};
		}

		private String[] noTitle() {
			return new String[]{SELECTOR, RULE, TYPE, MEDIUM_TYPES};
		}

		public Table attachedTo(Table table) {
			table.addGeneratedColumn(SELECTOR, this);
			table.setColumnHeader(SELECTOR, "");
			table.setColumnWidth(SELECTOR, 50);

			table.addGeneratedColumn(RULE, this);
			table.setColumnHeader(RULE, $("FolderCopyRuleField.rule"));
			table.setColumnWidth(RULE, 200);

			table.addGeneratedColumn(TITLE, this);
			table.setColumnHeader(TITLE, $("FolderCopyRuleField.title"));
			table.setColumnWidth(TITLE, 250);

			table.addGeneratedColumn(TYPE, this);
			table.setColumnHeader(TYPE, $("FolderCopyRuleField.type"));

			table.addGeneratedColumn(MEDIUM_TYPES, this);
			table.setColumnHeader(MEDIUM_TYPES, $("FolderCopyRuleField.mediumTypes"));

			return table;
		}

		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			if (columnId == null) {
				return null;
			}
			CopyRetentionRule copyRetentionRule = (CopyRetentionRule) itemId;
			switch ((String) columnId) {
				case SELECTOR:
					return generateSelectorCell(copyRetentionRule);
				case RULE:
					return generateRuleCell(copyRetentionRule);
				case TITLE:
					return generateTitleCell(copyRetentionRule);
				case MEDIUM_TYPES:
					return generateMediumTypesCell(copyRetentionRule);
				case TYPE:
					return generateTypeCell(copyRetentionRule);
			}
			return null;
		}

		protected Object generateSelectorCell(final CopyRetentionRule copyRetentionRule) {
			final CheckBox box = new CheckBox();
			if (copyRetentionRule.getId().equals(getInternalValue())) {
				box.setValue(true);
				box.setEnabled(false);
			}
			box.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					if (box.getValue()) {
						onValueChange(copyRetentionRule.getId());
					}
				}
			});
			return box;
		}

		private Object generateRuleCell(CopyRetentionRule copyRetentionRule) {
			return copyRetentionRule.toString();
		}

		private Object generateTitleCell(CopyRetentionRule copyRetentionRule) {
			BaseLabel titleLabel = new BaseLabel(copyRetentionRule.getTitle());

			StringBuilder niceTitle = new StringBuilder();
			if (StringUtils.isNotBlank(copyRetentionRule.getDescription())) {
				niceTitle.append(copyRetentionRule.getDescription());
				niceTitle.append("<br/><br/>");
			}
			if (copyRetentionRule.isIgnoreActivePeriod()) {
				niceTitle.append($("DetailsFieldGroup.ignoreActivePeriod"));
				niceTitle.append(":");
				niceTitle.append($("" + copyRetentionRule.isIgnoreActivePeriod()));
			}

			titleLabel.addStyleName(ValoTheme.BUTTON_LINK);
			titleLabel.addExtension(new NiceTitle(niceTitle.toString()));
			return titleLabel;
		}

		private Object generateTypeCell(CopyRetentionRule copyRetentionRule) {
			return new ReferenceDisplay(copyRetentionRule.getTypeId(), false);
		}

		private Object generateMediumTypesCell(CopyRetentionRule copyRetentionRule) {
			List<String> mediumTypeIds = copyRetentionRule.getMediumTypeIds();
			String mediumTypes = recordIdListToStringConverter.convertToPresentation(mediumTypeIds, String.class, i18n.getLocale());
			return new Label(mediumTypes, ContentMode.HTML);
		}

	}

	protected void onValueChange(String copyRetentionRuleId) {
		setValue(copyRetentionRuleId);
	}
}
