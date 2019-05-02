package com.constellio.app.modules.restapi.document;

import com.constellio.app.modules.restapi.ace.AceService;
import com.constellio.app.modules.restapi.core.exception.RecordNotFoundException;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.HttpMethods;
import com.constellio.app.modules.restapi.document.dao.DocumentDao;
import com.constellio.app.modules.restapi.document.dto.DocumentDto;
import com.constellio.app.modules.restapi.resource.dto.AceDto;
import com.constellio.app.modules.restapi.resource.dto.AceListDto;
import com.constellio.app.modules.restapi.resource.dto.ExtendedAttributeDto;
import com.constellio.app.modules.restapi.validation.ValidationService;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.schemas.MetadataList;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DocumentServiceTest {

	@Mock ValidationService validationService;
	@Mock AceService aceService;
	@Mock DocumentDao documentDao;

	@Mock User user;
	@Mock Record documentRecord;
	@Mock Record documentTypeRecord;
	@Mock MetadataSchema metadataSchema;
	@Mock MetadataList metadataList;

	@InjectMocks DocumentService documentService;

	private String documentId = "id";
	private String documentTitle = "title";
	private String eTag = "12345";

	private String host = "localhost";
	private String serviceKey = "serviceKey";
	private String method = HttpMethods.GET;
	private String date = DateUtils.formatIsoNoMillis(new DateTime());
	private int expiration = 3600;
	private String signature = "signature";

	@Before
	public void setUp() {
		initMocks(this);

		when(documentDao.getUser(anyString(), anyString())).thenReturn(user);
		when(documentDao.getRecordById(documentId)).thenReturn(documentRecord);
		when(documentDao.getMetadataSchema(documentRecord)).thenReturn(metadataSchema);
		when(aceService.getAces(documentRecord)).thenReturn(AceListDto.builder()
				.directAces(Collections.<AceDto>emptyList())
				.inheritedAces(Collections.<AceDto>emptyList())
				.build());

		when(documentRecord.getId()).thenReturn(documentId);
		when(documentRecord.getTitle()).thenReturn(documentTitle);
		when(documentRecord.getVersion()).thenReturn(Long.valueOf(eTag));
		when(documentRecord.get(any(Metadata.class))).thenReturn(null);
		when(documentTypeRecord.get(any(Metadata.class))).thenReturn(null);

		when(metadataSchema.getMetadatas()).thenReturn(metadataList);
		when(metadataList.onlyUSR()).thenReturn(new MetadataList());
	}

	@Test
	public void testGet() throws Exception {
		DocumentDto documentDto = documentService.get(host, documentId, serviceKey, method, date, expiration,
				signature, Collections.<String>emptySet());

		assertThat(documentDto).isNotNull().isEqualTo(DocumentDto.builder()
				.id(documentId)
				.title(documentTitle)
				.directAces(Collections.<AceDto>emptyList())
				.inheritedAces(Collections.<AceDto>emptyList())
				.extendedAttributes(Collections.<ExtendedAttributeDto>emptyList())
				.eTag(eTag)
				.build());
	}

	@Test(expected = RecordNotFoundException.class)
	public void testGetWithInvalidId() throws Exception {
		when(documentDao.getRecordById(documentId)).thenReturn(null);

		documentService.get(host, documentId, serviceKey, method, date, expiration, signature, Collections.<String>emptySet());
	}

}
