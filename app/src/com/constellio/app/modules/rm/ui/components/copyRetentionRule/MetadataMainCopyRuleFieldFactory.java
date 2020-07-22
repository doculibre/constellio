package com.constellio.app.modules.rm.ui.components.copyRetentionRule;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.copyRetentionRule.MetadataMainCopyRuleFieldImpl;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.fields.copyRetentionRule.MetadataMainCopyRuleInactiveFieldImpl;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.vaadin.ui.Field;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Locale;

@SuppressWarnings("serial")
public class MetadataMainCopyRuleFieldFactory extends MetadataFieldFactory {
	String retentionRule;
	List<CopyRetentionRule> copyRetentionRules;

	public MetadataMainCopyRuleFieldFactory(String retentionRule, List<CopyRetentionRule> copyRetentionRules) {
		super();
		this.retentionRule = retentionRule;
		this.copyRetentionRules = copyRetentionRules;
	}

	@Override
	public Field<?> build(MetadataVO metadata, String recordId, Locale locale) {
		Field<?> field;
		if (StringUtils.isBlank(retentionRule)) {
			field = new MetadataMainCopyRuleInactiveFieldImpl();
		} else {
			field = new MetadataMainCopyRuleFieldImpl(copyRetentionRules);
		}

		// FIXME Temporary workaround for inconsistencies
		if (metadata.getJavaType() == null) {
			field = null;
		}
		if (field != null) {
			postBuild(field, metadata);
		}
		return field;
	}
}
