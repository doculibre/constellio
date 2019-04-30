package com.constellio.app.modules.restapi.document.adaptor;

import com.constellio.app.modules.restapi.ace.AceService;
import com.constellio.app.modules.restapi.core.adaptor.ResourceAdaptor;
import com.constellio.app.modules.restapi.document.dao.DocumentDao;
import com.constellio.app.modules.restapi.document.dto.AceListDto;
import com.constellio.app.modules.restapi.document.dto.ContentDto;
import com.constellio.app.modules.restapi.document.dto.DocumentDto;
import com.constellio.app.modules.restapi.document.dto.DocumentTypeDto;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.google.common.base.Strings;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

import static com.constellio.app.modules.restapi.document.enumeration.VersionType.MAJOR;
import static com.constellio.app.modules.restapi.document.enumeration.VersionType.MINOR;

public class DocumentAdaptor extends ResourceAdaptor<DocumentDto> {

	@Inject
	private DocumentDao documentDao;
	@Inject
	private AceService aceService;

	@Override
	public DocumentDto adapt(DocumentDto resource, Record record, MetadataSchema schema, boolean modified,
							 Set<String> filters) {
		if (resource == null) {
			resource = DocumentDto.builder().build();
		}

		if (!modified) {
			resource.setETag(String.valueOf(record.getVersion()));
		}

		resource.setId(record.getId());
		resource.setTitle(!filters.contains("title") ? record.getTitle() : null);
		resource.setFolderId(!filters.contains("folderId") ? documentDao.<String>getMetadataValue(record, Document.FOLDER) : null);
		resource.setKeywords(!filters.contains("keywords") ? documentDao.<List<String>>getMetadataValue(record, Document.KEYWORDS) : null);
		resource.setAuthor(!filters.contains("author") ? documentDao.<String>getMetadataValue(record, Document.AUTHOR) : null);
		resource.setOrganization(!filters.contains("organization") ? documentDao.<String>getMetadataValue(record, Document.COMPANY) : null);
		resource.setSubject(!filters.contains("subject") ? documentDao.<String>getMetadataValue(record, Document.SUBJECT) : null);

		if (!filters.contains("type")) {
			String documentTypeId = documentDao.getMetadataValue(record, Document.TYPE);
			if (!Strings.isNullOrEmpty(documentTypeId)) {
				Record documentTypeRecord = documentDao.getRecordById(documentTypeId);

				resource.setType(documentTypeRecord == null ? null :
								 DocumentTypeDto.builder()
										 .id(documentTypeRecord.getId())
										 .code(documentDao.<String>getMetadataValue(documentTypeRecord, DocumentType.CODE))
										 .title(documentTypeRecord.getTitle())
										 .build());
			}
		} else {
			resource.setType(null);
		}

		if (!filters.contains("content")) {
			Content content = documentDao.getMetadataValue(record, Document.CONTENT);
			if (content != null) {
				resource.setContent(ContentDto.builder()
						.filename(content.getCurrentVersion().getFilename())
						.versionType(content.getCurrentVersion().isMajor() ? MAJOR : MINOR)
						.version(content.getCurrentVersion().getVersion())
						.hash(content.getCurrentVersion().getHash())
						.build());
			}
		} else {
			resource.setContent(null);
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
			resource.setExtendedAttributes(documentDao.getExtendedAttributes(schema, record));
		}

		return resource;
	}
}
