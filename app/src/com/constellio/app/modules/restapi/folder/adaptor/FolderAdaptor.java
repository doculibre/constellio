package com.constellio.app.modules.restapi.folder.adaptor;

import com.constellio.app.modules.restapi.ace.AceService;
import com.constellio.app.modules.restapi.folder.dao.FolderDao;
import com.constellio.app.modules.restapi.folder.dto.FolderDto;
import com.constellio.app.modules.restapi.folder.dto.FolderTypeDto;
import com.constellio.app.modules.restapi.resource.adaptor.ResourceAdaptor;
import com.constellio.app.modules.restapi.resource.dto.AceListDto;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.FolderMediaType;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.google.common.base.Strings;
import org.joda.time.LocalDate;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

public class FolderAdaptor extends ResourceAdaptor<FolderDto> {

	@Inject
	private FolderDao folderDao;
	@Inject
	private AceService aceService;

	@Override
	public FolderDto adapt(FolderDto resource, String host, Record record, MetadataSchema schema, boolean modified,
						   Set<String> filters) {
		if (resource == null) {
			resource = FolderDto.builder().build();
		}

		if (!modified) {
			resource.setETag(String.valueOf(record.getVersion()));
		}

		resource.setId(record.getId());
		resource.setParentFolderId(!filters.contains("parentFolderId") ? this.<String>getValue(record, Folder.PARENT_FOLDER) : null);
		resource.setCategory(!filters.contains("category") ? this.<String>getValue(record, Folder.CATEGORY_ENTERED, Folder.CATEGORY) : null);
		resource.setRetentionRule(!filters.contains("retentionRule") ?
								  this.<String>getValue(record, Folder.RETENTION_RULE_ENTERED, Folder.RETENTION_RULE) : null);
		resource.setAdministrativeUnit(!filters.contains("administrativeUnit") ?
									   this.<String>getValue(record, Folder.ADMINISTRATIVE_UNIT_ENTERED, Folder.ADMINISTRATIVE_UNIT) :
									   null);
		resource.setMainCopyRule(!filters.contains("mainCopyRule") ? getMainCopyRuleId(record) : null);
		resource.setCopyStatus(!filters.contains("copyStatus") ? getCopyStatus(record) : null);
		resource.setMediumTypes(!filters.contains("mediumTypes") ? this.<List<String>>getValue(record, Folder.MEDIUM_TYPES) : null);
		resource.setMediaType(!filters.contains("mediaType") ? getFolderMediaType(record) : null);
		resource.setContainer(!filters.contains("container") ? this.<String>getValue(record, Folder.CONTAINER) : null);
		resource.setTitle(!filters.contains("title") ? record.getTitle() : null);
		resource.setDescription(!filters.contains("description") ? this.<String>getValue(record, Folder.DESCRIPTION) : null);
		resource.setKeywords(!filters.contains("keywords") ? this.<List<String>>getValue(record, Folder.KEYWORDS) : null);
		resource.setOpeningDate(!filters.contains("openingDate") ? this.<LocalDate>getValue(record, Folder.OPENING_DATE) : null);
		resource.setClosingDate(!filters.contains("closingDate") ? this.<LocalDate>getValue(record, Folder.ENTERED_CLOSING_DATE, Folder.CLOSING_DATE) : null);
		resource.setActualDepositDate(!filters.contains("actualDepositDate") ? this.<LocalDate>getValue(record, Folder.ACTUAL_DEPOSIT_DATE) : null);
		resource.setActualDestructionDate(!filters.contains("actualDestructionDate") ? this.<LocalDate>getValue(record, Folder.ACTUAL_DESTRUCTION_DATE) : null);
		resource.setActualTransferDate(!filters.contains("actualTransferDate") ? this.<LocalDate>getValue(record, Folder.ACTUAL_TRANSFER_DATE) : null);
		resource.setExpectedDepositDate(!filters.contains("expectedDepositDate") ? this.<LocalDate>getValue(record, Folder.EXPECTED_DEPOSIT_DATE) : null);
		resource.setExpectedDestructionDate(!filters.contains("expectedDestructionDate") ? this.<LocalDate>getValue(record, Folder.EXPECTED_DESTRUCTION_DATE) : null);
		resource.setExpectedTransferDate(!filters.contains("expectedTransferDate") ? this.<LocalDate>getValue(record, Folder.EXPECTED_TRANSFER_DATE) : null);
		resource.setUrlToFolder(!filters.contains("urlToFolder") ? host + "/constellio/#!displayFolder/" + record.getId() : null);


		if (!filters.contains("type")) {
			String folderTypeId = getValue(record, Folder.TYPE);
			if (!Strings.isNullOrEmpty(folderTypeId)) {
				Record folderTypeRecord = folderDao.getRecordById(folderTypeId);

				resource.setType(folderTypeRecord == null ? null :
								 FolderTypeDto.builder()
										 .id(folderTypeRecord.getId())
										 .code(this.<String>getValue(folderTypeRecord, FolderType.CODE))
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
		return (enteredValue != null) ? enteredValue : this.<T>getValue(record, metadataCode);
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
