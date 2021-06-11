package com.constellio.app.modules.rm.ui.pages.retentionRule.retentionRuleDocumentType;

import com.constellio.app.modules.rm.ui.components.retentionRule.RetentionRuleDocumentTypeEditableRecordTablePresenter;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.framework.components.table.field.EditableRecordsTableField;
import com.constellio.app.ui.pages.base.SessionContext;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DisplayRetentionRuleDocumentTypes extends CustomComponent {

	private final Supplier<List<String>> recordsIdsProvider;
	private final RetentionRuleDocumentTypeEditableRecordTablePresenter presenter;

	public DisplayRetentionRuleDocumentTypes(AppLayerFactory appLayerFactory,
											 SessionContext sessionContext, Supplier<List<String>> recordsIdsProvider) {
		this.recordsIdsProvider = recordsIdsProvider;
		presenter = buildPresenter(appLayerFactory, sessionContext);
	}

	@Override
	public void attach() {
		super.attach();

		setCompositionRoot(buildMainComponent());
	}

	public List<MetadataVO> getMetadataThatMustBeHiddenByDefault(List<MetadataVO> availableMetadatas) {
		return new ArrayList<>();
	}

	private Component buildMainComponent() {
		presenter.setUseTheseRecordIdsInstead(recordsIdsProvider.get());

		EditableRecordsTableField editableRecordsTableField = new EditableRecordsTableField(presenter);
		editableRecordsTableField.setReadOnly(true);
		editableRecordsTableField.setResizedIfRowsDoesNotFillHeight(true);

		return editableRecordsTableField;
	}

	@NotNull
	private RetentionRuleDocumentTypeEditableRecordTablePresenter buildPresenter(AppLayerFactory appLayerFactory,
																				 SessionContext sessionContext) {
		return new RetentionRuleDocumentTypeEditableRecordTablePresenter(appLayerFactory, sessionContext);
	}
}
