package com.constellio.app.modules.restapi.document;

import com.constellio.app.modules.restapi.RestApiConfigs;
import com.constellio.app.modules.restapi.core.util.DateUtils;
import com.constellio.app.modules.restapi.core.util.HashingUtils;
import com.constellio.app.modules.restapi.core.util.SchemaTypes;
import com.constellio.app.modules.restapi.document.dto.ContentDto;
import com.constellio.app.modules.restapi.document.dto.DocumentDto;
import com.constellio.app.modules.restapi.document.dto.DocumentTypeDto;
import com.constellio.app.modules.restapi.document.dto.MixinContentDto;
import com.constellio.app.modules.restapi.document.dto.MixinDocumentDto;
import com.constellio.app.modules.restapi.document.dto.MixinDocumentTypeDto;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.enums.ParsingBehavior;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.data.utils.MimeTypes;
import com.constellio.sdk.tests.CommitCounter;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.QueryCounter;
import com.constellio.sdk.tests.setups.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static com.constellio.sdk.tests.QueryCounter.ON_COLLECTION;
import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class BaseDocumentRestfulServiceAcceptanceTest extends ConstellioTest {

	protected RMSchemasRecordsServices rm;
	protected RecordServices recordServices;
	protected SearchServices searchServices;
	protected UserServices userServices;
	protected AuthorizationsServices authorizationsServices;
	protected ContentManager contentManager;
	protected RMTestRecords records = new RMTestRecords(zeCollection);
	protected Users users = new Users();

	protected String host, id, version, physical, signature;
	protected String serviceKey = "bobKey", token = "bobToken", schemaType = SchemaTypes.DOCUMENT.name(),
			folderId = records.folder_A20, method = HttpMethod.GET, expiration = "2147483647";
	protected LocalDateTime fakeDate = new LocalDateTime();
	protected String date = DateUtils.formatIsoNoMillis(fakeDate);
	protected String dateFormat, dateTimeFormat;

	protected String sasquatchServiceKey = "sasquatchKey", sasquatchToken = "sasquatchToken";

	protected String fakeFilename = "fakeFile.txt";
	protected Document fakeDocument;
	protected DocumentType fakeDocumentType;
	protected String fakeMetadata1 = "USRMetadata1", fakeMetadata2 = "USRMetadata2";

	protected ContentVersionDataSummary dataSummaryV1, dataSummaryV2;
	protected String fakeFileContentV1 = "This is the content", fakeFileContentV2 = "This is the new content";
	protected String expectedChecksumV1, expectedChecksumV2;
	protected String expectedMimeType = MimeTypes.MIME_TEXT_PLAIN;

	protected AuthorizationAddRequest authorization1, authorization2;

	protected WebTarget webTarget;
	protected CommitCounter commitCounter;
	protected QueryCounter queryCounter;

	protected static final String NOT_NULL_MESSAGE = "javax.validation.constraints.NotNull.message";
	protected static final String NOT_EMPTY_MESSAGE = "org.hibernate.validator.constraints.NotEmpty.message";
	protected static final String JERSEY_NOT_FOUND_MESSAGE = "HTTP 404 Not Found";
	protected static final String OPEN_BRACE = "{";
	protected static final String CLOSE_BRACE = "}";

	@Before
	public void setUp() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioRestApiModule().withAllTest(users)
				.withRMTest(records).withFoldersAndContainersOfEveryStatus());
		givenConfig(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		contentManager = getModelLayerFactory().getContentManager();
		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		searchServices = getModelLayerFactory().newSearchServices();

		host = "localhost:7070";
		dateFormat = getModelLayerFactory().getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.DATE_FORMAT);
		dateTimeFormat = getModelLayerFactory().getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.DATE_TIME_FORMAT);

		userServices.addUpdateUserCredential(users.bob().withServiceKey(serviceKey)
				.withAccessToken(token, TimeProvider.getLocalDateTime().plusYears(1)));
		userServices.addUpdateUserCredential(users.sasquatch().withServiceKey(sasquatchServiceKey)
				.withAccessToken(sasquatchToken, TimeProvider.getLocalDateTime().plusYears(1)));

		givenConfig(RestApiConfigs.REST_API_URLS, "localhost:7070; localhost2");
		givenTimeIs(fakeDate);

		ObjectMapper mapper = new ObjectMapper().addMixIn(ContentDto.class, MixinContentDto.class)
				.addMixIn(DocumentDto.class, MixinDocumentDto.class)
				.addMixIn(DocumentTypeDto.class, MixinDocumentTypeDto.class);
		webTarget = newWebTarget("v1/documents", mapper);

		expectedChecksumV1 = HashingUtils.md5(fakeFileContentV1);
		expectedChecksumV2 = HashingUtils.md5(fakeFileContentV2);

		uploadFakeFile();
		createAuthorizations();
		id = fakeDocument.getId();

		commitCounter = new CommitCounter(getDataLayerFactory());
		queryCounter = new QueryCounter(getDataLayerFactory(), ON_COLLECTION(SYSTEM_COLLECTION));
	}

	protected List<Authorization> filterInheritedAuthorizations(List<Authorization> authorizations, String recordId) {
		List<Authorization> filteredAuthorizations = Lists.newArrayList();
		for (Authorization authorization : authorizations) {
			if (authorization.getGrantedOnRecord().equals(recordId)) {
				filteredAuthorizations.add(authorization);
			}
		}
		return filteredAuthorizations;
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

	protected <T> void addUsrMetadata(final MetadataValueType type, T value1, T value2) throws Exception {
		addUsrMetadata(type, null, value1, value2);
	}

	protected <T> void addUsrMetadata(final MetadataValueType type, final String schemaCode, T value1, T value2)
			throws Exception {
		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaBuilder schemaBuilder = schemaCode != null ?
													  types.getSchema(schemaCode) : types.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema();

				if (type == MetadataValueType.REFERENCE) {
					schemaBuilder.create(fakeMetadata1).setType(type).defineReferencesTo(types.getSchemaType(User.SCHEMA_TYPE));
					schemaBuilder.create(fakeMetadata2).setType(type).setMultivalue(true).defineReferencesTo(types.getSchemaType(User.SCHEMA_TYPE));
				} else {
					schemaBuilder.create(fakeMetadata1).setType(type);
					schemaBuilder.create(fakeMetadata2).setType(type).setMultivalue(true);
				}
			}
		});

		if (value1 != null && value2 != null) {
			Document document = rm.getDocument(id);
			document.set(fakeMetadata1, value1);
			document.set(fakeMetadata2, value2);
			recordServices.update(document);
		}
	}

	protected void createAuthorizations() {
		AuthorizationsServices authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		authorization1 = authorizationInCollection(zeCollection).forUsers(users.bobIn(zeCollection))
				.on(fakeDocument).givingReadWriteDeleteAccess();
		authorizationsServices.add(authorization1, users.adminIn(zeCollection));
		authorization2 = authorizationInCollection(zeCollection).forUsers(users.aliceIn(zeCollection))
				.on(fakeDocument).givingReadWriteAccess();
		authorizationsServices.add(authorization2, users.adminIn(zeCollection));
	}

	protected byte[] readStreamEntity(Response response) throws Exception {
		InputStream inputStream = (InputStream) response.getEntity();
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		IOUtils.copy(inputStream, byteArray);
		return byteArray.toByteArray();
	}

	protected WebTarget buildQuery(WebTarget target, boolean calculateSignature, List<String> defaultParams,
								   String... excludedParam) throws Exception {
		List<String> excludedParams = asList(excludedParam);

		for (String param : defaultParams) {
			if (excludedParams.contains(param)) {
				continue;
			}

			if (param.equals("signature")) {
				String signature = calculateSignature ? calculateSignature(defaultParams.toArray(new String[0])) : "123";
				target = target.queryParam(param, signature);
			} else {
				Object value = getClass().getSuperclass().getDeclaredField(param).get(this);
				if (value == null) {
					continue;
				}
				target = target.queryParam(param, value);
			}
		}
		return target;
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

	protected String calculateSignature(String... params) throws Exception {
		String data = host;
		for (String param : params) {
			if (param.equals("signature")) {
				continue;
			}
			if (param.equals("method")) {
				data = data.concat(SchemaTypes.DOCUMENT.name());
			}
			String value = String.valueOf(getClass().getSuperclass().getDeclaredField(param).get(this));
			data = !value.equals("null") ? data.concat(value) : data;
		}
		return HashingUtils.hmacSha256Base64UrlEncoded(token, data);
	}

	protected void switchToCustomSchema(String id) throws Exception {
		DocumentType documentType = records.documentTypeForm();
		Record record = recordServices.getDocumentById(id);
		record.set(rm.document.type(), documentType.getId());
		recordServices.update(record);
	}

	protected LocalDate toLocalDate(String date) {
		return date != null ? DateUtils.parseLocalDate(date, dateFormat) : null;
	}

	protected LocalDateTime toLocalDateTime(String date) {
		return date != null ? DateUtils.parseLocalDateTime(date, dateTimeFormat) : null;
	}

	protected String toDateString(LocalDate date) {
		return date != null ? DateUtils.format(date, dateFormat) : null;
	}

	protected String toDateString(LocalDateTime date) {
		return date != null ? DateUtils.format(date, dateTimeFormat) : null;
	}

	protected void resetCounters() {
		commitCounter.reset();
		queryCounter.reset();
	}

	protected Set<String> toPrincipals(Collection<String> ids) {
		Set<String> principals = new HashSet<>();
		for (String id : ids) {
			Record record = recordServices.getDocumentById(id);
			if (record.isOfSchemaType(User.SCHEMA_TYPE)) {
				principals.add(record.<String>get(rm.user.username()));
			} else {
				principals.add(record.<String>get(rm.group.code()));
			}
		}
		return principals;
	}

	protected List<String> toPrincipalIds(Collection<String> principals) {
		List<String> principalIds = new ArrayList<>(principals.size());
		for (String principal : principals) {
			Record record = recordServices.getRecordByMetadata(rm.user.username(), principal);
			if (record == null) {
				record = recordServices.getRecordByMetadata(rm.group.code(), principal);
			}
			principalIds.add(record.getId());
		}
		return principalIds;
	}

}
