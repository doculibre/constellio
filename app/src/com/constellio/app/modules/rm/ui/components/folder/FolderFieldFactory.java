/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.ui.components.folder;

import static com.constellio.app.modules.rm.wrappers.Folder.ACTUAL_DEPOSIT_DATE;
import static com.constellio.app.modules.rm.wrappers.Folder.ACTUAL_DESTRUCTION_DATE;
import static com.constellio.app.modules.rm.wrappers.Folder.ACTUAL_TRANSFER_DATE;
import static com.constellio.app.modules.rm.wrappers.Folder.ADMINISTRATIVE_UNIT_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.BORROW_PREVIEW_RETURN_DATE;
import static com.constellio.app.modules.rm.wrappers.Folder.CATEGORY_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.CONTAINER;
import static com.constellio.app.modules.rm.wrappers.Folder.COPY_STATUS_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.FILING_SPACE_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.PARENT_FOLDER;
import static com.constellio.app.modules.rm.wrappers.Folder.RETENTION_RULE_ENTERED;
import static com.constellio.app.modules.rm.wrappers.Folder.TYPE;
import static com.constellio.app.modules.rm.wrappers.Folder.UNIFORM_SUBDIVISION_ENTERED;

import com.constellio.app.modules.rm.ui.components.RMMetadataFieldFactory;
import com.constellio.app.modules.rm.ui.components.folder.fields.CustomFolderField;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderActualDepositDateFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderActualDestructionDateFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderActualTransferDateFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderAdministrativeUnitFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCategoryFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderContainerFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCopyStatusEnteredFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderFilingSpaceFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderParentFolderFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderPreviewReturnDateFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderRetentionRuleFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderTypeFieldImpl;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderUniformSubdivisionFieldImpl;
import com.constellio.app.ui.entities.MetadataVO;
import com.vaadin.ui.Field;

public class FolderFieldFactory extends RMMetadataFieldFactory {

	@Override
	public Field<?> build(MetadataVO metadata) {
		Field<?> field;
		String metadataCode = metadata.getCode();
		String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
		String[] taxonomyCodes = metadata.getTaxonomyCodes();
		if (TYPE.equals(metadataCode) || TYPE.equals(metadataCodeWithoutPrefix)) {
			field = new FolderTypeFieldImpl();
		} else if (PARENT_FOLDER.equals(metadataCode) || PARENT_FOLDER.equals(metadataCodeWithoutPrefix)) {
			field = new FolderParentFolderFieldImpl(taxonomyCodes);
		} else if (ADMINISTRATIVE_UNIT_ENTERED.equals(metadataCode) || ADMINISTRATIVE_UNIT_ENTERED
				.equals(metadataCodeWithoutPrefix)) {
			field = new FolderAdministrativeUnitFieldImpl();
		} else if (FILING_SPACE_ENTERED.equals(metadataCode) || FILING_SPACE_ENTERED.equals(metadataCodeWithoutPrefix)) {
			field = new FolderFilingSpaceFieldImpl();
		} else if (CATEGORY_ENTERED.equals(metadataCode) || CATEGORY_ENTERED.equals(metadataCodeWithoutPrefix)) {
			field = new FolderCategoryFieldImpl();
		} else if (UNIFORM_SUBDIVISION_ENTERED.equals(metadataCode) || UNIFORM_SUBDIVISION_ENTERED
				.equals(metadataCodeWithoutPrefix)) {
			field = new FolderUniformSubdivisionFieldImpl();
		} else if (RETENTION_RULE_ENTERED.equals(metadataCode) || RETENTION_RULE_ENTERED.equals(metadataCodeWithoutPrefix)) {
			field = new FolderRetentionRuleFieldImpl();
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
		} else if (BORROW_PREVIEW_RETURN_DATE.equals(metadataCode) || BORROW_PREVIEW_RETURN_DATE
				.equals(metadataCodeWithoutPrefix)) {
			field = new FolderPreviewReturnDateFieldImpl();
		} else {
			field = super.build(metadata);
		}
		if (field instanceof CustomFolderField) {
			postBuild(field, metadata);
		}
		return field;
	}

}
