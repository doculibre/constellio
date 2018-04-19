package com.constellio.app.modules.rm.ui.components.document.fields;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleInRule;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.display.ReferenceDisplay;
import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;

public class DocumentCopyRuleFieldImpl extends CustomField<String> implements DocumentCopyRuleField {
	private Table table;
	private List<CopyRetentionRule> rules;
	private Locale locale;

	public DocumentCopyRuleFieldImpl(List<CopyRetentionRuleInRule> rules) {
		this.rules = toCopyRetentionRules(rules);
		this.locale = ConstellioUI.getCurrentSessionContext().getCurrentLocale();
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
	public void setFieldChoices(List<CopyRetentionRuleInRule> rules) {
		this.rules = toCopyRetentionRules(rules);
		updateTable();
	}

	@Override
	protected Component initContent() {
		table = new Generator().attachedTo(new Table());
		table.setWidth("100%");
		updateTable();
		this.setVisible(rules.size() >= 2);
		return table;
	}

	private List<CopyRetentionRule> toCopyRetentionRules(List<CopyRetentionRuleInRule> rules) {
		List<CopyRetentionRule> result = new ArrayList<>();
		for (CopyRetentionRuleInRule rule : rules) {
			result.add(rule.getCopyRetentionRule());
		}
		return result;
	}

	private void updateTable() {
		table.removeAllItems();
		for (CopyRetentionRule rule : rules) {
			table.addItem(rule);
		}
		table.setPageLength(rules.size());
		this.setVisible(rules.size() >= 2);
	}

	private class Generator implements ColumnGenerator {
		public static final String SELECTOR = "selector";
		public static final String TITLE = "title";
		public static final String NUMBER = "number";
		public static final String TYPE = "type";
		public static final String RULE = "rule";

		public Table attachedTo(Table table) {
			table.addGeneratedColumn(SELECTOR, this);
			table.setColumnHeader(SELECTOR, "");
			table.setColumnWidth(SELECTOR, 50);

			table.addGeneratedColumn(TITLE, this);
			table.setColumnHeader(TITLE, $("FolderCopyRuleField.title"));

			table.addGeneratedColumn(NUMBER, this);
			table.setColumnHeader(NUMBER, $("FolderCopyRuleField.number"));

			table.addGeneratedColumn(TYPE, this);
			table.setColumnHeader(TYPE, $("FolderCopyRuleField.type"));

			table.addGeneratedColumn(RULE, this);
			table.setColumnHeader(RULE, $("FolderCopyRuleField.rule"));

			return table;
		}

		@Override
		public Object generateCell(Table source, Object itemId, Object columnId) {
			if (columnId == null) {
				return null;
			}
			CopyRetentionRule rule = (CopyRetentionRule) itemId;
			switch ((String) columnId) {
			case SELECTOR:
				return generateSelectorCell(rule);
			case NUMBER:
				return generateNumberCell(rule);
			case TITLE:
				return generateTitleCell(rule);
			case TYPE:
				return generateTypeCell(rule);
			case RULE:
				return generateRuleCell(rule);
			}
			return null;
		}

		private Object generateSelectorCell(final CopyRetentionRule rule) {
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

		private Object generateNumberCell(CopyRetentionRule rule) {
			return rule.getCode();
		}

		private Object generateTitleCell(CopyRetentionRule rule) {
			return rule.getTitle();
		}

		private Object generateTypeCell(CopyRetentionRule rule) {
			return new ReferenceDisplay(rule.getTypeId());
		}

		private Object generateRuleCell(CopyRetentionRule rule) {
			return rule.toString();
		}
	}
}
