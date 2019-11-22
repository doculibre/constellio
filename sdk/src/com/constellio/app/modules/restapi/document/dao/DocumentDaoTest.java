package com.constellio.app.modules.restapi.document.dao;

import com.constellio.app.modules.restapi.core.exception.OptimisticLockException;
import com.constellio.app.modules.restapi.core.exception.UnresolvableOptimisticLockException;
import com.constellio.app.modules.restapi.document.dto.DocumentContentDto;
import com.constellio.app.modules.restapi.document.dto.DocumentDto;
import com.constellio.app.modules.restapi.document.exception.DocumentContentNotFoundException;
import com.constellio.data.dao.services.bigVault.RecordDaoException;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.contents.ContentImplRuntimeException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentManagerRuntimeException;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.utils.MimeTypes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DocumentDaoTest {

	@Mock private RecordServices recordServices;
	@Mock private MetadataSchemasManager metadataSchemasManager;
	@Mock private ContentManager contentManager;

	@Mock private DocumentDto document;
	@Mock private User user;
	@Mock private Record record;
	@Mock private MetadataSchema metadataSchema;
	@Mock private Content content;
	@Mock private InputStream inputStream;
	@Mock private ContentVersion contentVersion;

	@Mock private RecordDaoException.OptimisticLocking recordDaoOptimisticLocking;

	@InjectMocks private DocumentDao documentDao;

	private String id = "id";
	private String version = "1.0";
	private String mimeType = MimeTypes.MIME_APPLICATION_PDF;

	@Before
	public void setUp() {
		initMocks(this);

		when(metadataSchemasManager.getSchemaOf(record)).thenReturn(metadataSchema);

		when(document.getExtendedAttributes()).thenReturn(null);
		when(record.getId()).thenReturn(id);
		when(documentDao.getRecordById(id)).thenReturn(record);
		when(documentDao.getRecordById(id, null)).thenReturn(record);

		when(content.getVersion(version)).thenReturn(contentVersion);
		when(contentManager.getContentInputStream(anyString(), anyString())).thenReturn(inputStream);
	}

	@Test
	public void testGetContent() {
		when(documentDao.getMetadataValue(record, anyString())).thenReturn(content).thenReturn(mimeType);

		DocumentContentDto contentDto = documentDao.getContent(record, version);

		assertThat(contentDto).isNotNull();
		assertThat(contentDto.getContent()).isEqualTo(inputStream);
		assertThat(contentDto.getMimeType()).isEqualTo(mimeType);
	}

	@Test(expected = DocumentContentNotFoundException.class)
	public void testGetContentContentNull() {
		when(documentDao.getMetadataValue(record, anyString())).thenReturn(null);

		documentDao.getContent(record, version);
	}

	@Test(expected = DocumentContentNotFoundException.class)
	public void testGetContentNoSuchVersionException() {
		when(content.getVersion(anyString()))
				.thenThrow(new ContentImplRuntimeException.ContentImplRuntimeException_NoSuchVersion(null));

		documentDao.getContent(record, version);
	}

	@Test(expected = DocumentContentNotFoundException.class)
	public void testGetContentNoSuchContentException() {
		when(contentManager.getContentInputStream(anyString(), anyString()))
				.thenThrow(new ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent(null));

		documentDao.getContent(record, version);
	}

	@Test(expected = OptimisticLockException.class)
	public void testUpdateDocumentOptimisticLockException() throws Exception {
		when(documentDao.getMetadataValue(record, anyString())).thenReturn(null);

		doThrow(new RecordServicesException.OptimisticLocking(null, recordDaoOptimisticLocking))
				.when(recordServices).execute(any(Transaction.class));

		documentDao.updateDocument(user, record, metadataSchema, document, content, true, "NOW");
	}

	@Test(expected = UnresolvableOptimisticLockException.class)
	public void testUpdateDocumentUnresolvableOptimisticLockException() throws Exception {
		when(documentDao.getMetadataValue(record, anyString())).thenReturn(null);

		doThrow(new RecordServicesException.UnresolvableOptimisticLockingConflict(""))
				.when(recordServices).execute(any(Transaction.class));

		documentDao.updateDocument(user, record, metadataSchema, document, content, true, "NOW");
	}

	// TODO test that content is deleted if add(document) throw an exception

}
