package com.constellio.app.modules.rm.ui.components.folder;

import static com.constellio.app.modules.rm.wrappers.Folder.ACTUAL_DEPOSIT_DATE;
import static com.constellio.app.modules.rm.wrappers.Folder.ACTUAL_DESTRUCTION_DATE;
import static com.constellio.app.modules.rm.wrappers.Folder.ACTUAL_TRANSFER_DATE;
import static com.constellio.app.modules.rm.wrappers.Folder.ADMINISTRATIVE_UNIT_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.BORROWING_TYPE;
import static com.constellio.app.modules.rm.wrappers.Folder.BORROW_PREVIEW_RETURN_DATE;
import static com.constellio.app.modules.rm.wrappers.Folder.BORROW_RETURN_DATE;
import static com.constellio.app.modules.rm.wrappers.Folder.CATEGORY_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.CONTAINER;
import static com.constellio.app.modules.rm.wrappers.Folder.COPY_STATUS_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.LINEAR_SIZE;
import static com.constellio.app.modules.rm.wrappers.Folder.OPENING_DATE;
import static com.constellio.app.modules.rm.wrappers.Folder.PARENT_FOLDER;
import static com.constellio.app.modules.rm.wrappers.Folder.RETENTION_RULE_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.TYPE;
import static com.constellio.app.modules.rm.wrappers.Folder.UNIFORM_SUBDIVISION_ENTERED;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.ui.components.RMMetadataFieldFactory;
import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderActualDepositDateFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderActualDestructionDateFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderActualTransferDateFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderAdministrativeUnitFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderBorrpwingTypeFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCategoryFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderContainerFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCopyStatusEnteredFieldImpl;
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
import com.vaadin.ui.Field;

public class FolderFieldFactory extends RMMetadataFieldFactory {
	private final String collection;

	public FolderFieldFactory(String collection) {
		this.collection = collection;
	}

	@Override
	public Field<?> build(MetadataVO metadata) {
		Field<?> field;
		String metadataCode = metadata.getCode();
		String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
		String[] taxonomyCodes = metadata.getTaxonomyCodes();
		MetadataInputType inputType = metadata.getMetadataInputType();
		if (TYPE.equals(metadataCode) || TYPE.equals(metadataCodeWithoutPrefix)) {
			if (MetadataInputType.LOOKUP.equals(inputType)) {
				field = new FolderTypeFieldLookupImpl();
			} else if (MetadataInputType.RADIO_BUTTONS.equals(inputType)) {
				field = new FolderTypeFieldOptionGroupImpl();
			} else {
				field = new FolderTypeFieldComboBoxImpl();
			}
		} else if (PARENT_FOLDER.equals(metadataCode) || PARENT_FOLDER.equals(metadataCodeWithoutPrefix)) {
			field = new FolderParentFolderFieldImpl(taxonomyCodes);
		} else if (ADMINISTRATIVE_UNIT_ENTERED.equals(metadataCode) || ADMINISTRATIVE_UNIT_ENTERED
				.equals(metadataCodeWithoutPrefix)) {
			field = new FolderAdministrativeUnitFieldImpl();
		} else if (CATEGORY_ENTERED.equals(metadataCode) || CATEGORY_ENTERED.equals(metadataCodeWithoutPrefix)) {
			field = new FolderCategoryFieldImpl();
		} else if (UNIFORM_SUBDIVISION_ENTERED.equals(metadataCode) || UNIFORM_SUBDIVISION_ENTERED
				.equals(metadataCodeWithoutPrefix)) {
			field = new FolderUniformSubdivisionFieldImpl();
		} else if (RETENTION_RULE_ENTERED.equals(metadataCode) || RETENTION_RULE_ENTERED.equals(metadataCodeWithoutPrefix)) {
			field = new FolderRetentionRuleFieldImpl(collection);
		} else if (COPY_STATUS_ENTERED.equals(metadataCode) || COPY_STATUS_ENTERED.equals(metadataCodeWithoutPrefix)) {
			field = new FolderCopyStatusEnteredFieldImpl();
		} else if (ACTUAL_TRANSFER_DATE.equals(metadataCode) || ACTUAL_TRANSFER_DATE.equals(metadataCodeWithoutPrefix)) {
			field = new FolderActualTransferDateFieldImpl();
		} else if (ACTUAL_DEPOSIT_DATE.equals(metadataCode) || ACTUAL_DEPOSIT_DATE.equals(metadataCodeWithoutPrefix)) {
			field = new FolderActualDepositDateFieldImpl();
		} else if (ACTUAL_DESTRUCTION_DATE.equals(metadataCode) || ACTUAL_DESTRUCTION_DATE.equals(metadataCodeWithoutPrefix)) {
			field = new FolderActualDestructionDateFieldImpl();
		} else if (CONTAINER.equals(metadataCode) || CONTAINER.equals(metadataCodeWithoutPrefix)) {
			field = new FolderContainerFieldImpl();
		} else if (LINEAR_SIZE.equals(metadataCode) || LINEAR_SIZE.equals(metadataCodeWithoutPrefix)) {
			field = new FolderLinearSizeFieldImpl();
		} else if (BORROW_PREVIEW_RETURN_DATE.equals(metadataCode) || BORROW_PREVIEW_RETURN_DATE
				.equals(metadataCodeWithoutPrefix)) {
			field = new FolderPreviewReturnDateFieldImpl();
		} else if (BORROW_RETURN_DATE.equals(metadataCode) || BORROW_RETURN_DATE
				.equals(metadataCodeWithoutPrefix)) {
			field = new FolderReturnDateFieldImpl();
		} else if (BORROWING_TYPE.equals(metadataCode) || BORROWING_TYPE
				.equals(metadataCodeWithoutPrefix)) {
			field = new FolderBorrpwingTypeFieldImpl();
		} else if (OPENING_DATE.equals(metadataCode) || OPENING_DATE
				.equals(metadataCodeWithoutPrefix)) {
			field = new FolderOpeningDateFieldImpl();
		} else {
			field = super.build(metadata);
		}
		if (field instanceof CustomFolderField) {
			postBuild(field, metadata);
		}
		return field;
	}

}
