package com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.copyRetentionRule;

import com.constellio.app.ui.framework.components.fields.BaseTextField;

import static com.constellio.app.ui.i18n.i18n.$;

public class MetadataMainCopyRuleInactiveFieldImpl extends BaseTextField {

	public MetadataMainCopyRuleInactiveFieldImpl() {
		super();
		super.setEnabled(false);
		super.setCaption($("AddEditMetadataView.defaultValueForInactiveMainCopyRule"));
	}

	@Override
	public void setEnabled(boolean enabled) {

	}

	@Override
	public void setCaption(String caption) {

	}
}
