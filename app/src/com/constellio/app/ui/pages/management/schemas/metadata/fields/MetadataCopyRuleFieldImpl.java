package com.constellio.app.ui.pages.management.schemas.metadata.fields;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCopyRuleFieldImpl;
import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;

import java.util.List;

public class MetadataCopyRuleFieldImpl extends FolderCopyRuleFieldImpl {

	public MetadataCopyRuleFieldImpl(List<CopyRetentionRule> copyRetentionRules) {
		super(copyRetentionRules);
	}

	@Override
	protected Component initContent() {
		generator = new CopyRuleGenerator();
		table = generator.attachedTo(new Table());
		table.setWidth("100%");
		updateTable();
		return table;
	}

	@Override
	protected void onValueChange(String copyRetentionRuleId) {
		super.onValueChange(copyRetentionRuleId);
		if (table != null) {
			updateTable();
		}
	}

	protected class CopyRuleGenerator extends Generator {
		@Override
		protected Object generateSelectorCell(final CopyRetentionRule copyRetentionRule) {
			final CheckBox box = new CheckBox();
			if (copyRetentionRule.getId().equals(getInternalValue())) {
				box.setValue(true);
			}
			box.addValueChangeListener(new ValueChangeListener() {
				@Override
				public void valueChange(Property.ValueChangeEvent event) {
					if (box.getValue()) {
						onValueChange(copyRetentionRule.getId());
					}else{
						onValueChange(null);
					}
				}
			});
			return box;
		}
	}
}
