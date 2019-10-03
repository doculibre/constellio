package com.constellio.app.modules.rm.ui.components.folder;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.ui.components.RMRecordFieldFactory;
import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderActualDepositDateFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderActualDestructionDateFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderActualTransferDateFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderAdministrativeUnitFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderAllowedDocumentTypeFieldLookupImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderAllowedTypeFieldLookupImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderBorrpwingTypeFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCategoryFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderContainerFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCopyRuleFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCopyStatusEnteredFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderDisposalTypeFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderLinearSizeFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderOpeningDateFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderParentFolderFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderPreviewReturnDateFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderRetentionRuleFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderReturnDateFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderTypeFieldComboBoxImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderTypeFieldLookupImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderTypeFieldOptionGroupImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderUniformSubdivisionFieldImpl;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.vaadin.ui.Field;

import java.util.List;
import java.util.Locale;

import static com.constellio.app.modules.rm.wrappers.Folder.ACTUAL_DEPOSIT_DATE;
import static com.constellio.app.modules.rm.wrappers.Folder.ACTUAL_DESTRUCTION_DATE;
import static com.constellio.app.modules.rm.wrappers.Folder.ACTUAL_TRANSFER_DATE;
import static com.constellio.app.modules.rm.wrappers.Folder.ADMINISTRATIVE_UNIT_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.ALLOWED_DOCUMENT_TYPES;
import static com.constellio.app.modules.rm.wrappers.Folder.ALLOWED_FOLDER_TYPES;
import static com.constellio.app.modules.rm.wrappers.Folder.BORROWING_TYPE;
import static com.constellio.app.modules.rm.wrappers.Folder.BORROW_PREVIEW_RETURN_DATE;
import static com.constellio.app.modules.rm.wrappers.Folder.BORROW_RETURN_DATE;
import static com.constellio.app.modules.rm.wrappers.Folder.CATEGORY_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.CONTAINER;
import static com.constellio.app.modules.rm.wrappers.Folder.COPY_STATUS_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.LINEAR_SIZE;
import static com.constellio.app.modules.rm.wrappers.Folder.MAIN_COPY_RULE_ID_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.MANUAL_DISPOSAL_TYPE;
import static com.constellio.app.modules.rm.wrappers.Folder.OPENING_DATE;
import static com.constellio.app.modules.rm.wrappers.Folder.PARENT_FOLDER;
import static com.constellio.app.modules.rm.wrappers.Folder.RETENTION_RULE_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.TYPE;
import static com.constellio.app.modules.rm.wrappers.Folder.UNIFORM_SUBDIVISION_ENTERED;

public class FolderFieldFactory extends RMRecordFieldFactory {
	private final String collection;
	private final List<CopyRetentionRule> rules;
	private final String retentionRule;
	private final String parent;

	public FolderFieldFactory(String collection, List<CopyRetentionRule> rules, String retentionRule, String parent) {
		this.collection = collection;
		this.rules = rules;
		this.retentionRule = retentionRule;
		this.parent = parent;
	}

	@Override
	public Field<?> build(RecordVO recordVO, MetadataVO metadataVO, Locale locale) {
		Field<?> field;
		String[] taxonomyCodes = metadataVO.getTaxonomyCodes();
		MetadataInputType inputType = metadataVO.getMetadataInputType();

		switch (metadataVO.getLocalCode()) {
			case TYPE:
				field = new FolderTypeFieldLookupImpl(parent);
				break;
			case PARENT_FOLDER:
				field = new FolderParentFolderFieldImpl(taxonomyCodes);
				break;
			case ADMINISTRATIVE_UNIT_ENTERED:
				field = new FolderAdministrativeUnitFieldImpl();
				break;
			case CATEGORY_ENTERED:
				field = new FolderCategoryFieldImpl();
				break;
			case UNIFORM_SUBDIVISION_ENTERED:
				field = new FolderUniformSubdivisionFieldImpl();
				break;
			case RETENTION_RULE_ENTERED:
				field = new FolderRetentionRuleFieldImpl(collection);
				break;
			case COPY_STATUS_ENTERED:
				field = new FolderCopyStatusEnteredFieldImpl();
				break;
			case ACTUAL_TRANSFER_DATE:
				field = new FolderActualTransferDateFieldImpl();
				break;
			case ACTUAL_DEPOSIT_DATE:
				field = new FolderActualDepositDateFieldImpl();
				break;
			case ACTUAL_DESTRUCTION_DATE:
				field = new FolderActualDestructionDateFieldImpl();
				break;
			case CONTAINER:
				field = new FolderContainerFieldImpl();
				break;
			case LINEAR_SIZE:
				field = new FolderLinearSizeFieldImpl();
				break;
			case BORROW_PREVIEW_RETURN_DATE:
				field = new FolderPreviewReturnDateFieldImpl();
				break;
			case BORROW_RETURN_DATE:
				field = new FolderReturnDateFieldImpl();
				break;
			case BORROWING_TYPE:
				field = new FolderBorrpwingTypeFieldImpl();
				break;
			case OPENING_DATE:
				field = new FolderOpeningDateFieldImpl();
				break;
			case MAIN_COPY_RULE_ID_ENTERED:
				field = new FolderCopyRuleFieldImpl(rules);
				break;
			case MANUAL_DISPOSAL_TYPE:
				field = new FolderDisposalTypeFieldImpl();
				break;
			case ALLOWED_DOCUMENT_TYPES:
				field = new FolderAllowedDocumentTypeFieldLookupImpl(retentionRule);
				break;
			case ALLOWED_FOLDER_TYPES:
				field = new FolderAllowedTypeFieldLookupImpl();
				break;
			default:
				field = super.build(recordVO, metadataVO, locale);
		}

		if (field instanceof CustomFolderField) {
			postBuild(field, recordVO, metadataVO);
		} else {
			callPostBuildExtensions(field, recordVO, metadataVO);
		}

		return field;
	}

}
