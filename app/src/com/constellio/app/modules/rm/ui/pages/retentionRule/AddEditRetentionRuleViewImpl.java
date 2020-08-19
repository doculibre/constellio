package com.constellio.app.modules.rm.ui.pages.retentionRule;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
import com.constellio.app.modules.rm.ui.components.retentionRule.DocumentCopyRetentionRuleTable;
import com.constellio.app.modules.rm.ui.components.retentionRule.DocumentDefaultCopyRetentionRuleTable;
import com.constellio.app.modules.rm.ui.components.retentionRule.FolderCopyRetentionRuleTable;
import com.constellio.app.modules.rm.ui.components.retentionRule.ListAddRemoveRetentionRuleDocumentTypeField;
import com.constellio.app.modules.rm.ui.components.retentionRule.RetentionRuleListAddRemoveAdministrativeUnitLookupField;
import com.constellio.app.modules.rm.ui.entities.RetentionRuleVO;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.framework.components.fields.enumWithSmallCode.EnumWithSmallCodeComboBox;
import com.constellio.app.ui.framework.components.fields.list.ListAddRemoveRecordLookupField;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.app.ui.util.ComponentTreeUtils;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

import java.util.List;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;

public class AddEditRetentionRuleViewImpl extends BaseViewImpl implements AddEditRetentionRuleView {

	private final AddEditRetentionRulePresenter presenter;
	private RetentionRuleVO rule;
	private RetentionRuleForm form;
	private Boolean delayedDisposalTypeVisibleForDocumentTypes;

	public AddEditRetentionRuleViewImpl() {
		presenter = new AddEditRetentionRulePresenter(this);
	}

	@Override
	protected void initBeforeCreateComponents(ViewChangeEvent event) {
		presenter.forParams(event.getParameters());
	}

	@Override
	public void setRetentionRule(RetentionRuleVO retentionRuleVO) {
		this.rule = retentionRuleVO;
	}

	@Override
	protected String getTitle() {
		return $(presenter.isAddView() ? "AddEditRetentionRuleView.addViewTitle" : "AddEditRetentionRuleView.editViewTitle");
	}

	@Override
	protected Component buildMainComponent(ViewChangeEvent event) {
		this.form = initForm();
		return form;
	}

	@Override
	protected void afterViewAssembled(ViewChangeEvent event) {
		presenter.viewAssembled();
	}

	@Override
	public void reloadForm() {
		RetentionRuleForm newForm = initForm();
		replaceComponent(this.form, newForm);
		this.form = newForm;
	}

	@Override
	protected boolean isFullWidthIfActionMenuAbsent() {
		return true;
	}

	private RetentionRuleForm initForm() {
		RetentionRuleForm form = new RetentionRuleForm(rule);
		if (delayedDisposalTypeVisibleForDocumentTypes != null) {
			setDisposalTypeVisibleForDocumentTypes(delayedDisposalTypeVisibleForDocumentTypes);
		}
		return form;
	}

	@Override
	public void setDisposalTypeVisibleForDocumentTypes(boolean visible) {
		if (form != null) {
			ListAddRemoveRetentionRuleDocumentTypeField documentTypesField = form.getDocumentTypesField();
			if (documentTypesField != null) {
				documentTypesField.setDisposalTypeVisible(visible);
			}
		} else {
			delayedDisposalTypeVisibleForDocumentTypes = visible;
		}
	}

	private class RetentionRuleForm extends RecordForm {
		public RetentionRuleForm(RecordVO record) {
			super(record, new RetentionRuleFieldFactory(), AddEditRetentionRuleViewImpl.this.getConstellioFactories());
		}

		@Override
		protected void saveButtonClick(RecordVO record) {
			presenter.saveButtonClicked();
		}

		@Override
		protected void cancelButtonClick(RecordVO viewObject) {
			presenter.cancelButtonClicked();
		}

		public ListAddRemoveRetentionRuleDocumentTypeField getDocumentTypesField() {
			return ComponentTreeUtils.getFirstChild(this, ListAddRemoveRetentionRuleDocumentTypeField.class);
		}
	}

	private class RetentionRuleFieldFactory extends RecordFieldFactory {

		private DocumentDefaultCopyRetentionRuleTable documentDefaultCopyRetentionRuleTable;

		private void initDocumentDefaultCopyRetentionRuleTable(RecordVO recordVO) {
			if (documentDefaultCopyRetentionRuleTable == null) {
				documentDefaultCopyRetentionRuleTable = new DocumentDefaultCopyRetentionRuleTable((RetentionRuleVO) recordVO,
						true, presenter) {
					@Override
					protected List<MetadataVO> getDateMetadataVOs() {
						return presenter.getDateMetadataVOs(null);
					}
				};
			}
		}

		@Override
		public Field<?> build(RecordVO recordVO, MetadataVO metadataVO, Locale locale) {
			Field<?> field;
			switch (metadataVO.getLocalCode()) {
				case RetentionRule.COPY_RETENTION_RULES:
					if (presenter.isFoldersCopyRetentionRulesVisible()) {
						field = new FolderCopyRetentionRuleTable((RetentionRuleVO) recordVO, true, presenter, getSessionContext().getCurrentLocale()) {
							@Override
							protected void onDisposalTypeChange(CopyRetentionRule copyRetentionRule) {
								presenter.disposalTypeChanged(copyRetentionRule);
							}

							@Override
							protected List<MetadataVO> getDateMetadataVOs(String documentTypeId) {
								return presenter.getFolderMetadataVOs();
							}
						};
						postBuild(field, recordVO, metadataVO);
					} else {
						field = null;
					}
					break;
				case RetentionRule.DOCUMENT_COPY_RETENTION_RULES:
					if (presenter.isDocumentsCopyRetentionRulesVisible()) {
						field = new DocumentCopyRetentionRuleTable((RetentionRuleVO) recordVO, true, presenter) {
							@Override
							protected List<MetadataVO> getDateMetadataVOs(String documentTypeId) {
								return presenter.getDateMetadataVOs(documentTypeId);
							}
						};
						postBuild(field, recordVO, metadataVO);
					} else {
						field = null;
					}
					break;
				case RetentionRule.PRINCIPAL_DEFAULT_DOCUMENT_COPY_RETENTION_RULE:
					if (presenter.isDefaultDocumentsCopyRetentionRulesVisible()) {
						initDocumentDefaultCopyRetentionRuleTable(recordVO);
						field = documentDefaultCopyRetentionRuleTable.getPrincipalCopyRetentionRuleField();
						postBuild(field, recordVO, metadataVO);
					} else {
						field = null;
					}
					break;
				case RetentionRule.SECONDARY_DEFAULT_DOCUMENT_COPY_RETENTION_RULE:
					if (presenter.isDefaultDocumentsCopyRetentionRulesVisible()) {
						initDocumentDefaultCopyRetentionRuleTable(recordVO);
						field = documentDefaultCopyRetentionRuleTable.getSecondaryCopyRetentionRuleField();
						postBuild(field, recordVO, metadataVO);
						field.setVisible(false);
					} else {
						field = null;
					}
					break;
				case RetentionRule.DOCUMENT_TYPES_DETAILS:
					field = presenter.shouldDisplayDocumentTypeDetails() ? new ListAddRemoveRetentionRuleDocumentTypeField() : null;
					if (field != null) {
						postBuild(field, recordVO, metadataVO);
					}
					break;
				case RetentionRuleVO.CATEGORIES:
					field = buildCategoriesField(recordVO, metadataVO);
					postBuild(field, recordVO, metadataVO);
					break;
				case RetentionRuleVO.UNIFORM_SUBDIVISIONS:
					field = buildCategoriesField(recordVO, metadataVO);
					postBuild(field, recordVO, metadataVO);
					field.setVisible(presenter.areSubdivisionUniformEnabled());
					field.setEnabled(presenter.areSubdivisionUniformEnabled());
					break;
				case RetentionRule.SCOPE:
					if (presenter.isScopeVisible()) {
						field = new EnumWithSmallCodeComboBox<>(RetentionRuleScope.class);
						field.addValueChangeListener(new ValueChangeListener() {
							@Override
							public void valueChange(ValueChangeEvent event) {
								RetentionRuleScope scope = (RetentionRuleScope) event.getProperty().getValue();
								presenter.scopeChanged(scope);
							}
						});
						postBuild(field, recordVO, metadataVO);
					} else {
						field = null;
					}
					break;
				case RetentionRule.ADMINISTRATIVE_UNITS:
					field = new RetentionRuleListAddRemoveAdministrativeUnitLookupField();
					postBuild(field, recordVO, metadataVO);
					break;
				default:
					field = super.build(recordVO, metadataVO, locale);
					break;
			}
			return field;
		}

		private Field<?> buildCategoriesField(RecordVO recordVO, MetadataVO metadataVO) {
			ListAddRemoveRecordLookupField field = (ListAddRemoveRecordLookupField) super.build(recordVO, metadataVO, null);
			if (field != null) {
				field.setIgnoreLinkability(true);
			}
			return field;
		}
	}
}
