package com.constellio.app.servlet;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.schemas.bulkImport.BulkImportProgressionListener;
import com.constellio.app.services.schemas.bulkImport.LoggerBulkImportProgressionListener;
import com.constellio.app.services.schemas.bulkImport.RecordsImportServices;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.constellio.data.conf.HashingEncoding.BASE64_URL_ENCODED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ConstellioImportRecordsServletAcceptanceTest extends ConstellioTest {


	private Users users = new Users();
	private RMSchemasRecordsServices rm;
	private RMTestRecords records = new RMTestRecords(zeCollection);

	private BulkImportProgressionListener progressionListener = new LoggerBulkImportProgressionListener();
	private RecordsImportServices importServices;
	private User admin;

	private Folder folder1;
	private Folder folder2;
	private Folder folder3;
	private Folder folder4;
	private Folder folder5;
	private Folder folder6;
	private Folder folder7;
	private Folder folder8;

	String adminServiceKey = "admin-key";
	String adminToken;

	private LocalDateTime now = new LocalDateTime().minusHours(3);

	private String url;

	@Before
	public void setUp()
			throws Exception {
		givenHashingEncodingIs(BASE64_URL_ENCODED);
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
		);

		url = startApplication();
		if (url.endsWith("/")) {
			url = StringUtils.substringBeforeLast(url, "/");
		}
		givenTimeIs(now);

		importServices = new RecordsImportServices(getModelLayerFactory());

		admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		getModelLayerFactory().newUserServices().addUpdateUserCredential(users.adminAddUpdateRequest().setServiceKey(adminServiceKey));
		adminToken = getModelLayerFactory().newUserServices().generateToken("admin");
	}

	@After
	public void tearDown() throws Exception {
		stopApplication();
	}

	//	private void importInZeCollectionUsingWebService(File file, File responseFileIfNotOK) {
	//		String collection = zeCollection;
	//
	//		HttpEntity entity = MultipartEntityBuilder.create()
	//				.addPart("file", new FileBody(file))
	//				.build();
	//
	//		HttpPost request = new HttpPost(url + "/rm/uploadRecords");
	//		request.addHeader("serviceKey", adminServiceKey);
	//		request.addHeader("token", adminToken);
	//		request.addHeader("collection", collection);
	//		request.addHeader("dataType", "xlsx");
	//		request.setEntity(entity);
	//
	//		HttpClient client = HttpClientBuilder.create().build();
	//		try {
	//			HttpResponse response = client.execute(request);
	//
	//			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
	//				try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(responseFileIfNotOK))) {
	//					response.getEntity().writeTo(outputStream);
	//				}
	//			} else {
	//				String content = EntityUtils.toString(entity);
	//				if ("SUCCESS".equals(content)) {
	//					//Jetty return OK status if the webservice is not configured
	//					throw new RuntimeException("Import failed : the web service is not available");
	//				}
	//			}
	//		} catch (IOException e) {
	//			throw new RuntimeException(e);
	//		}
	//
	//	}

	private void importInZeCollectionUsingWebService(File file, File responseFileIfNotOK) {
		HttpConfigurations httpConfigurations = new HttpConfigurations(url);
		httpConfigurations.setServiceKey(adminServiceKey);
		httpConfigurations.setToken(adminToken);
		httpConfigurations.setUsername("admin");
		httpConfigurations.setCollection(zeCollection);
		RecordImportServiceClient client = new RecordImportServiceClient(httpConfigurations);
		String message = client.uploadXLSXFile(file.getAbsolutePath());
		if (StringUtils.isNotBlank(message)) {
			try {
				FileUtils.write(responseFileIfNotOK, message, "UTF-8");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	@Test
	public void whenImportingAnExcel2007FileUsingServletThenImportedCorrectly()
			throws Exception {

		ContentManager contentManager = getModelLayerFactory().getContentManager();
		String hash1 = contentManager.upload(newTempFileWithContent("file.txt", "I am the first value")).getHash();
		String hash2 = contentManager.upload(newTempFileWithContent("file.txt", "I am the second value")).getHash();
		String hash3 = contentManager.upload(newTempFileWithContent("file.txt", "I am the third value")).getHash();
		String hash4 = contentManager.upload(newTempFileWithContent("file.txt", "I am the fourth value")).getHash();
		String hash5 = contentManager.upload(newTempFileWithContent("file.txt", "I am the fifth value")).getHash();
		String hash6 = contentManager.upload(newTempFileWithContent("file.txt", "I am the sixth value")).getHash();

		File excelFile = getTestResourceFile("datas.xlsx");
		File excelFileModified = getTestResourceFile("datasModified.xlsx");
		File responseFileIfNotOk = new File(newTempFolder(), "response.txt");

		importInZeCollectionUsingWebService(getTestResourceFile("datas.xlsx"), responseFileIfNotOk);

		//		boolean l = true;
		//		while (l) {
		//			Thread.sleep(1000);
		//		}

		if (responseFileIfNotOk.exists()) {
			String message = FileUtils.readFileToString(responseFileIfNotOk, "UTF-8");
			System.out.println(message);
			fail(message);
		}
		assertThat(responseFileIfNotOk).doesNotExist();


		Document document1 = rm.getDocumentByLegacyId("1");
		assertThat(document1.getContent()).isNotNull();
		assertThat(document1.getContent().getCurrentVersion().getHash()).isEqualTo(hash1);
		assertThat(document1.getContent().getCurrentVersion().getFilename()).isEqualTo("fichier1.txt");

		Document document2 = rm.getDocumentByLegacyId("2");
		assertThat(document2.getContent()).isNotNull();
		assertThat(document2.getContent().getCurrentVersion().getHash()).isEqualTo(hash2);
		assertThat(document2.getContent().getCurrentVersion().getFilename()).isEqualTo("fichier2.txt");

		Document document3 = rm.getDocumentByLegacyId("3");
		assertThat(document3.getContent()).isNotNull();
		assertThat(document3.getContent().getCurrentVersion().getHash()).isEqualTo(hash3);
		assertThat(document3.getContent().getCurrentVersion().getFilename()).isEqualTo("fichier3.txt");

	}

	@Test
	public void whenImportingAnExcel2007FileWithMissingContentUsingServletThenErrors()
			throws Exception {

		ContentManager contentManager = getModelLayerFactory().getContentManager();
		String hash1 = contentManager.upload(newTempFileWithContent("file.txt", "I am the first value")).getHash();
		String hash2 = contentManager.upload(newTempFileWithContent("file.txt", "I am the second value")).getHash();

		File excelFile = getTestResourceFile("datas.xlsx");
		File responseFileIfNotOk = new File(newTempFolder(), "response.txt");
		importInZeCollectionUsingWebService(getTestResourceFile("datas.xlsx"), responseFileIfNotOk);
		assertThat(responseFileIfNotOk).hasContent("Document 3 : Le contenu «PDnNE4i3IjJ9FFN1HzE_5FXROBs=» n'existe pas dans la voûte");

		Document document1 = rm.getDocumentByLegacyId("1");
		assertThat(document1).isNull();

	}

}
