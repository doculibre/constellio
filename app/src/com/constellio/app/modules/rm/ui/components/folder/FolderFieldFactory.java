package com.constellio.app.modules.rm.ui.components.folder;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.ui.components.RMRecordFieldFactory;
import com.constellio.app.modules.rm.ui.components.folder.fields.*;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.vaadin.ui.Field;

import java.util.List;
import java.util.Locale;

import static com.constellio.app.modules.rm.wrappers.Folder.*;

public class FolderFieldFactory extends RMRecordFieldFactory {
	private final String collection;
	private final List<CopyRetentionRule> rules;

	public FolderFieldFactory(String collection, List<CopyRetentionRule> rules) {
		this.collection = collection;
		this.rules = rules;
	}

	@Override
	public Field<?> build(RecordVO recordVO, MetadataVO metadataVO, Locale locale) {
		Field<?> field;
		String[] taxonomyCodes = metadataVO.getTaxonomyCodes();
		MetadataInputType inputType = metadataVO.getMetadataInputType();

		switch (metadataVO.getLocalCode()) {
			case TYPE:
				if (MetadataInputType.LOOKUP.equals(inputType)) {
					field = new FolderTypeFieldLookupImpl();
				} else if (MetadataInputType.RADIO_BUTTONS.equals(inputType)) {
					field = new FolderTypeFieldOptionGroupImpl();
				} else {
					field = new FolderTypeFieldComboBoxImpl();
				}
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
			default:
				field = super.build(recordVO, metadataVO, locale);
		}

		if (field instanceof CustomFolderField) {
			postBuild(field, recordVO, metadataVO);
		}

		return field;
	}

}
