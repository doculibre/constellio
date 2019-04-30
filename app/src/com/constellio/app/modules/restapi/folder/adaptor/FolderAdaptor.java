package com.constellio.app.modules.restapi.folder.adaptor;

import com.constellio.app.modules.restapi.ace.AceService;
import com.constellio.app.modules.restapi.core.adaptor.ResourceAdaptor;
import com.constellio.app.modules.restapi.document.dto.AceListDto;
import com.constellio.app.modules.restapi.folder.dao.FolderDao;
import com.constellio.app.modules.restapi.folder.dto.FolderDto;
import com.constellio.app.modules.restapi.folder.dto.FolderTypeDto;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.FolderMediaType;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.google.common.base.Strings;

import javax.inject.Inject;
import java.util.Set;

public class FolderAdaptor extends ResourceAdaptor<FolderDto> {

	@Inject
	private FolderDao folderDao;
	@Inject
	private AceService aceService;

	@Override
	public FolderDto adapt(FolderDto resource, Record record, MetadataSchema schema, boolean modified,
						   Set<String> filters) {
		if (resource == null) {
			resource = FolderDto.builder().build();
		}

		if (!modified) {
			resource.setETag(String.valueOf(record.getVersion()));
		}

		resource.setId(record.getId());
		resource.setParentFolderId(!filters.contains("parentFolderId") ? getValue(record, Folder.PARENT_FOLDER) : null);
		resource.setCategory(!filters.contains("category") ? getValue(record, Folder.CATEGORY_ENTERED, Folder.CATEGORY) : null);
		resource.setRetentionRule(!filters.contains("retentionRule") ?
								  getValue(record, Folder.RETENTION_RULE_ENTERED, Folder.RETENTION_RULE) : null);
		resource.setAdministrativeUnit(!filters.contains("administrativeUnit") ?
									   getValue(record, Folder.ADMINISTRATIVE_UNIT_ENTERED, Folder.ADMINISTRATIVE_UNIT) :
									   null);
		resource.setMainCopyRule(!filters.contains("mainCopyRule") ? getMainCopyRuleId(record) : null);
		resource.setCopyStatus(!filters.contains("copyStatus") ? getCopyStatus(record) : null);
		resource.setMediumTypes(!filters.contains("mediumTypes") ? getValue(record, Folder.MEDIUM_TYPES) : null);
		resource.setMediaType(!filters.contains("mediaType") ? getFolderMediaType(record) : null);
		resource.setContainer(!filters.contains("container") ? getValue(record, Folder.CONTAINER) : null);
		resource.setTitle(!filters.contains("title") ? record.getTitle() : null);
		resource.setDescription(!filters.contains("description") ? getValue(record, Folder.DESCRIPTION) : null);
		resource.setKeywords(!filters.contains("keywords") ? getValue(record, Folder.KEYWORDS) : null);
		resource.setOpeningDate(!filters.contains("openingDate") ? getValue(record, Folder.OPENING_DATE) : null);
		resource.setClosingDate(!filters.contains("closingDate") ? getValue(record, Folder.ENTERED_CLOSING_DATE, Folder.CLOSING_DATE) : null);
		resource.setActualDepositDate(!filters.contains("actualDepositDate") ? getValue(record, Folder.ACTUAL_DEPOSIT_DATE) : null);
		resource.setActualDestructionDate(!filters.contains("actualDestructionDate") ? getValue(record, Folder.ACTUAL_DESTRUCTION_DATE) : null);
		resource.setActualTransferDate(!filters.contains("actualTransferDate") ? getValue(record, Folder.ACTUAL_TRANSFER_DATE) : null);
		resource.setExpectedDepositDate(!filters.contains("expectedDepositDate") ? getValue(record, Folder.EXPECTED_DEPOSIT_DATE) : null);
		resource.setExpectedDestructionDate(!filters.contains("expectedDestructionDate") ? getValue(record, Folder.EXPECTED_DESTRUCTION_DATE) : null);
		resource.setExpectedTransferDate(!filters.contains("expectedTransferDate") ? getValue(record, Folder.EXPECTED_TRANSFER_DATE) : null);

		if (!filters.contains("type")) {
			String folderTypeId = getValue(record, Folder.TYPE);
			if (!Strings.isNullOrEmpty(folderTypeId)) {
				Record folderTypeRecord = folderDao.getRecordById(folderTypeId);

				resource.setType(folderTypeRecord == null ? null :
								 FolderTypeDto.builder()
										 .id(folderTypeRecord.getId())
										 .code(getValue(folderTypeRecord, FolderType.CODE))
										 .title(folderTypeRecord.getTitle())
										 .build());
			}
		} else {
			resource.setType(null);
		}

		if (filters.contains("directAces") && filters.contains("indirectAces")) {
			resource.setDirectAces(null);
			resource.setInheritedAces(null);
		} else {
			AceListDto aces = aceService.getAces(record);
			resource.setDirectAces(filters.contains("directAces") ? null : aces.getDirectAces());
			resource.setInheritedAces(filters.contains("inheritedAces") ? null : aces.getInheritedAces());
		}

		if (filters.contains("extendedAttributes")) {
			resource.setExtendedAttributes(null);
		} else if (resource.getExtendedAttributes() == null) {
			resource.setExtendedAttributes(folderDao.getExtendedAttributes(schema, record));
		}

		return resource;
	}

	private <T> T getValue(Record record, String enteredMetadataCode, String metadataCode) {
		T enteredValue = getValue(record, enteredMetadataCode);
		return (enteredValue != null) ? enteredValue : getValue(record, metadataCode);
	}

	private <T> T getValue(Record record, String metadataCode) {
		return folderDao.getMetadataValue(record, metadataCode);
	}

	private String getMainCopyRuleId(Record record) {
		String mainCopyRuleIdEntered = getValue(record, Folder.MAIN_COPY_RULE_ID_ENTERED);
		if (mainCopyRuleIdEntered != null) {
			return mainCopyRuleIdEntered;
		}
		CopyRetentionRule rule = getValue(record, Folder.MAIN_COPY_RULE);
		return rule != null ? rule.getId() : null;
	}

	private String getCopyStatus(Record record) {
		CopyType copyTypeEntered = getValue(record, Folder.COPY_STATUS_ENTERED);
		if (copyTypeEntered != null) {
			return copyTypeEntered.getCode();
		}
		CopyType copyType = getValue(record, Folder.COPY_STATUS);
		return copyType != null ? copyType.getCode() : null;
	}

	private String getFolderMediaType(Record record) {
		FolderMediaType folderMediaType = getValue(record, Folder.MEDIA_TYPE);
		return folderMediaType != null ? folderMediaType.getCode() : null;
	}

}
