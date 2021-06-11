package com.constellio.app.modules.rm.ui.components.retentionRule;

import com.constellio.app.modules.rm.ui.components.RMMetadataDisplayFactory;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.modules.rm.ui.pages.retentionRule.retentionRuleDocumentType.DisplayRetentionRuleDocumentTypes;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.VariableRetentionPeriodVO;
import com.constellio.data.utils.dev.Toggle;
import com.vaadin.ui.Component;

import java.util.List;
import java.util.Locale;

import static com.constellio.app.modules.rm.wrappers.RetentionRule.COPY_RETENTION_RULES;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.DOCUMENT_COPY_RETENTION_RULES;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.DOCUMENT_TYPES_DETAILS;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.PRINCIPAL_DEFAULT_DOCUMENT_COPY_RETENTION_RULE;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.SECONDARY_DEFAULT_DOCUMENT_COPY_RETENTION_RULE;

public class RetentionRuleDisplayFactory extends RMMetadataDisplayFactory {
	private final RetentionRuleDisplayPresenter presenter;
	private final List<VariableRetentionPeriodVO> openPeriodsDDVList;
	private Locale locale;

	public RetentionRuleDisplayFactory(RetentionRuleDisplayPresenter presenter, Locale locale) {
		this.presenter = presenter;
		this.openPeriodsDDVList = presenter.getOpenActivePeriodsDDVList();
		this.locale = locale;
	}

	@Override
	public Component build(RecordVO recordVO, MetadataValueVO metadataValueVO) {
		Component component;
		MetadataVO metadataVO = metadataValueVO.getMetadata();
		String metadataCode = MetadataVO.getCodeWithoutPrefix(metadataVO.getCode());
		RetentionRuleVO retentionRuleVO = (RetentionRuleVO) recordVO;
		if (COPY_RETENTION_RULES.equals(metadataCode)) {
			component = new FolderCopyRetentionRuleTable(retentionRuleVO, false, presenter, locale);
			component.setVisible(presenter.shouldDisplayFolderRetentionRules());
		} else if (DOCUMENT_COPY_RETENTION_RULES.equals(metadataCode)) {
			component = new DocumentCopyRetentionRuleTable(retentionRuleVO, false, presenter);
			component.setVisible(presenter.shouldDisplayDocumentRetentionRules());
		} else if (PRINCIPAL_DEFAULT_DOCUMENT_COPY_RETENTION_RULE.equals(metadataCode)) {
			component = new DocumentDefaultCopyRetentionRuleTable(retentionRuleVO, false, presenter);
			component.setVisible(presenter.shouldDisplayDefaultDocumentRetentionRules());
		} else if (SECONDARY_DEFAULT_DOCUMENT_COPY_RETENTION_RULE.equals(metadataCode)) {
			component = null;
		} else if (DOCUMENT_TYPES_DETAILS.equals(metadataCode)) {
			component = new RetentionRuleDocumentTypeDisplay(retentionRuleVO);
			component.setVisible(presenter.shouldDisplayDocumentTypeDetails() && !Toggle.DISPLAY_DOCUMENT_TYPE_AS_TABLE.isEnabled());
		} else if (RetentionRuleVO.RETENTION_RULE_DOCUMENT_TYPE.equals(metadataCode)) {
			component = buildRetentionRuleDocumentTypeComponent(metadataValueVO);
			component.setVisible(component.isVisible() && presenter.shouldDisplayDocumentTypeDetails() && Toggle.DISPLAY_DOCUMENT_TYPE_AS_TABLE.isEnabled());
		} else {
			component = super.build(recordVO, metadataValueVO);
		}
		return component;
	}

	private Component buildRetentionRuleDocumentTypeComponent(MetadataValueVO metadataValueVO) {

		final List<String> recordIds = metadataValueVO.getValue();

		DisplayRetentionRuleDocumentTypes displayRetentionRuleDocumentTypes = new DisplayRetentionRuleDocumentTypes(
				ConstellioFactories.getInstanceIfAlreadyStarted().getAppLayerFactory(),
				ConstellioUI.getCurrentSessionContext(),
				() -> recordIds);

		displayRetentionRuleDocumentTypes.setVisible(recordIds != null && !recordIds.isEmpty());

		return displayRetentionRuleDocumentTypes;
	}

	public interface RetentionRuleDisplayPresenter extends RetentionRuleTablePresenter {

		List<VariableRetentionPeriodVO> getOpenActivePeriodsDDVList();

		boolean shouldDisplayFolderRetentionRules();

		boolean shouldDisplayDocumentRetentionRules();

		boolean shouldDisplayDefaultDocumentRetentionRules();

		boolean shouldDisplayDocumentTypeDetails();
	}
}
