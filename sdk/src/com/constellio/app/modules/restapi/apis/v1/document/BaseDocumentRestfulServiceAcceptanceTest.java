package com.constellio.app.modules.restapi.apis.v1.document;

import com.constellio.app.modules.restapi.apis.v1.BaseRestfulServiceAcceptanceTest;
import com.constellio.app.modules.restapi.apis.v1.document.dto.ContentDto;
import com.constellio.app.modules.restapi.apis.v1.document.dto.DocumentDto;
import com.constellio.app.modules.restapi.apis.v1.document.dto.DocumentTypeDto;
import com.constellio.app.modules.restapi.apis.v1.document.dto.MixinContentDto;
import com.constellio.app.modules.restapi.apis.v1.document.dto.MixinDocumentDto;
import com.constellio.app.modules.restapi.apis.v1.document.dto.MixinDocumentTypeDto;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.HashingUtils;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.utils.MimeTypes;
import com.constellio.sdk.tests.CommitCounter;
import com.constellio.sdk.tests.QueryCounter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.junit.Before;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import java.io.File;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class BaseDocumentRestfulServiceAcceptanceTest extends BaseRestfulServiceAcceptanceTest {

	protected ContentManager contentManager;

	protected String id, version, physical, signature;
	protected String schemaType = SchemaTypes.DOCUMENT.name(),
			folderId = records.folder_A20, method = HttpMethod.GET, expiration = "2147483647";
	protected String date = DateUtils.formatIsoNoMillis(fakeDate);

	protected String fakeFilename = "fakeFile.txt";
	protected Document fakeDocument;
	protected DocumentType fakeDocumentType;

	protected ContentVersionDataSummary dataSummaryV1, dataSummaryV2;
	protected String fakeFileContentV1 = "This is the content", fakeFileContentV2 = "This is the new content";
	protected String expectedChecksumV1, expectedChecksumV2;
	protected String expectedMimeType = MimeTypes.MIME_TEXT_PLAIN;

	@Override
	protected SchemaTypes getSchemaType() {
		return SchemaTypes.DOCUMENT;
	}

	@Before
	public void setUp() throws Exception {
		setUpTest();

		contentManager = getModelLayerFactory().getContentManager();

		ObjectMapper mapper = new ObjectMapper().addMixIn(ContentDto.class, MixinContentDto.class)
				.addMixIn(DocumentDto.class, MixinDocumentDto.class)
				.addMixIn(DocumentTypeDto.class, MixinDocumentTypeDto.class);

		webTarget = newWebTarget("v1/documents", mapper);

		expectedChecksumV1 = HashingUtils.md5(fakeFileContentV1);
		expectedChecksumV2 = HashingUtils.md5(fakeFileContentV2);

		uploadFakeFile();
		createAuthorizations(fakeDocument.getWrappedRecord());
		id = fakeDocument.getId();

		commitCounter = new CommitCounter(getDataLayerFactory());
		queryCounter = new QueryCounter(getDataLayerFactory());
	}

	protected void uploadFakeFile() throws Exception {
		File fakeFileV1 = newTempFileWithContent(fakeFilename, fakeFileContentV1);
		dataSummaryV1 = contentManager.upload(fakeFileV1);
		waitForBatchProcess();

		File fakeFileV2 = newTempFileWithContent(fakeFilename, fakeFileContentV2);
		dataSummaryV2 = contentManager.upload(fakeFileV2);
		waitForBatchProcess();

		fakeDocumentType = rm.newDocumentType().setCode("documentTypeCode").setTitle("documentTypeTitle");
		recordServices.add(fakeDocumentType);

		fakeDocument = rm.newDocument().setFolder(folderId).setType(fakeDocumentType).setTitle("Title").setAuthor("Toto")
				.setContent(contentManager.createMajor(users.adminIn(zeCollection), fakeFilename, dataSummaryV1))
				.setKeywords(asList("keyword1", "keyword2")).setSubject("Subject").setCompany("Organization");
		recordServices.add(fakeDocument);

		fakeDocument.getContent().updateContent(users.adminIn(zeCollection), dataSummaryV2, true);
		recordServices.update(fakeDocument);
	}

	protected <T> void addUsrMetadata(MetadataValueType type, T value1, T value2) throws Exception {
		addUsrMetadata(id, Document.DEFAULT_SCHEMA, type, value1, value2);
	}

	protected MultiPart buildMultiPart(DocumentDto document) {
		return buildMultiPart(document, null);
	}

	protected MultiPart buildMultiPart(DocumentDto document, File file) {
		FormDataMultiPart multiPart = new FormDataMultiPart();
		multiPart.bodyPart(new FormDataBodyPart("document", document, APPLICATION_JSON_TYPE));
		if (file != null) {
			multiPart.bodyPart(new FileDataBodyPart("file", file, MediaType.APPLICATION_OCTET_STREAM_TYPE));
		}

		return multiPart;
	}

	protected void switchToCustomSchema(String id) throws Exception {
		Record record = recordServices.getDocumentById(id);
		record.changeSchema(records.getSchemas().defaultDocumentSchema(),
				records.getSchemas().documentSchemaFor(records.documentTypeForm()));
		recordServices.update(record);
	}

}
