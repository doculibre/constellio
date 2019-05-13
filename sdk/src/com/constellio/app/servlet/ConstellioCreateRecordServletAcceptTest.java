package com.constellio.app.servlet;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import org.joda.time.LocalDate;
import org.jsoup.Jsoup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ConstellioCreateRecordServletAcceptTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices rmSchemasRecordsServices;
	RecordServices recordServices;
	UserServices userServices;
	Users users = new Users();

	String bobToken;
	String bobServiceKey = "bobServiceKey";

	String aliceToken;
	String aliceServiceKey = "aliceServiceKey";

	Category category;
	RetentionRule retentionRule;
	AdministrativeUnit administrativeUnit;
	LocalDate nowLocalDate = TimeProvider.getLocalDate();

	Map<String, Object> validArgumentsForFolderCreation;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records));

		category = records.getCategory_X100();
		retentionRule = records.getRule1();
		administrativeUnit = records.getUnit10a();

		validArgumentsForFolderCreation = makeFolderCreationArgumentsMap("Squatre");

		rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();
		bobToken = userServices.generateToken(bobGratton);
		aliceToken = userServices.generateToken(aliceWonderland);
		userServices.addUpdateUserCredential(users.bob().setServiceKey(bobServiceKey).setSystemAdminEnabled());
		userServices.addUpdateUserCredential(users.alice().setServiceKey(aliceServiceKey));
		startApplication();
	}

	@After
	public void tearDown()
			throws Exception {
		stopApplication();
	}

	@Test
	public void givenWebServiceIsEnabledThenOnlyExecutedWhenGoodAuthentication()
			throws Exception {

		try {
			callCreateFolders(null, null, zeCollection, HttpMethod.GET, validArgumentsForFolderCreation);
			fail("Exception expected");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
		}

		try {
			callCreateFolders(bobServiceKey, null, zeCollection, HttpMethod.GET, validArgumentsForFolderCreation);
			fail("Exception expected");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
		}

		try {
			callCreateFolders(null, bobToken, zeCollection, HttpMethod.GET, validArgumentsForFolderCreation);
			fail("Exception expected");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
		}

		try {
			callCreateFolders(bobServiceKey, "pouet", zeCollection, HttpMethod.GET, validArgumentsForFolderCreation);
			fail("Exception expected");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
		}

		try {
			callCreateFolders(bobServiceKey, aliceToken, zeCollection, HttpMethod.GET, validArgumentsForFolderCreation);
			fail("Exception expected");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
		}

		try {
			callCreateFolders(aliceServiceKey, aliceToken, zeCollection, HttpMethod.GET, validArgumentsForFolderCreation);
			fail("Exception expected because alice is not sysadmin");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
		}

		try {
			callCreateFolders(bobServiceKey, bobToken, null, HttpMethod.GET, validArgumentsForFolderCreation);
			fail("Exception expected because collection parameter");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
		}

		String recordId = callCreateFolders(bobServiceKey, bobToken, zeCollection, HttpMethod.GET,
				validArgumentsForFolderCreation);
		assertThat(recordId).isNotEmpty();
	}

	@Test
	public void givenWebServiceIsEnabledAndValidArgumentsWhenExcuteThenCreateFolder()
			throws Exception {

		String recordId = callCreateFolders(bobServiceKey, bobToken, zeCollection, HttpMethod.GET,
				validArgumentsForFolderCreation);

		Folder folder = rmSchemasRecordsServices.getFolder(recordId);
		assertThat(folder.getTitle()).isEqualTo("Squatre");
		assertThat(folder.getCategoryEntered()).isEqualTo(category.getId());
		assertThat(folder.getRetentionRuleEntered()).isEqualTo(retentionRule.getId());
		assertThat(folder.getAdministrativeUnitEntered()).isEqualTo(administrativeUnit.getId());
		assertThat(folder.getCreatedBy()).isEqualTo(userServices.getUserInCollection(bobGratton, zeCollection).getId());
		assertThat(folder.getCreatedOn().toLocalDate()).isEqualTo(nowLocalDate);
		assertThat(folder.getOpenDate()).isEqualTo(nowLocalDate);
	}

	@Test
	public void givenWebServiceIsEnabledAndGoodAuthenticationAndMissingRequiredFieldsThenGoodResponse()
			throws Exception {

		Map<String, Object> missingArgumentsForFolderCreation = new HashMap<String, Object>() {
			{
				put("createdBy", bobGratton);
				put("createdOn", nowLocalDate);

			}
		};

		String message = callCreateFolders(bobServiceKey, bobToken, zeCollection, HttpMethod.GET,
				missingArgumentsForFolderCreation);

		assertThat(message).contains("Métadonnée «Titre» requise");
	}

	@Test
	public void givenWebServiceIsEnabledAndValidArgumentsWhenExecuteThenCreateFolders()
			throws Exception {

		String resultText = callCreateFolders(bobServiceKey, bobToken, zeCollection, HttpMethod.POST,
				makeFolderCreationArgumentsMap("Squatre0"), makeFolderCreationArgumentsMap("Squatre1"),
				makeFolderCreationArgumentsMap("Squatre2"));

		String[] recordsId = resultText.split(" ");
		assertThat(recordsId).hasSize(3);

		for (int i = 0; i < 3; i++) {
			Folder folder = rmSchemasRecordsServices.getFolder(recordsId[i]);
			assertThat(folder.getTitle()).isEqualTo("Squatre" + i);
			assertThat(folder.getCategoryEntered()).isEqualTo(category.getId());
			assertThat(folder.getRetentionRuleEntered()).isEqualTo(retentionRule.getId());
			assertThat(folder.getAdministrativeUnitEntered()).isEqualTo(administrativeUnit.getId());
			assertThat(folder.getCreatedBy()).isEqualTo(userServices.getUserInCollection(bobGratton, zeCollection).getId());
			assertThat(folder.getCreatedOn().toLocalDate()).isEqualTo(nowLocalDate);
			assertThat(folder.getOpenDate()).isEqualTo(nowLocalDate);
		}
	}

	private Map<String, Object> makeFolderCreationArgumentsMap(final String title) {
		return new HashMap<String, Object>() {
			{
				put("title", title);
				put("categoryEntered", category.getCode());
				put("retentionRuleEntered", retentionRule.getCode());
				put("administrativeUnitEntered", administrativeUnit.getCode());
				put("createdBy", bobGratton);
				put("createdOn", nowLocalDate);
				put("openingDate", nowLocalDate);
			}
		};
	}

	private String callCreateFolders(String serviceKey, String token, String collection, HttpMethod httpMethod,
									 Map<String, Object>... otherParamsArray)
			throws IOException, InterruptedException {
		StringBuilder url = new StringBuilder("http://localhost:7070/constellio/createRecord?schema=folder_default");
		StringBuilder body = new StringBuilder("<Folders>");
		for (Map<String, Object> otherParams : otherParamsArray) {
			if (HttpMethod.GET.equals(httpMethod)) {
				for (Map.Entry<String, Object> element : otherParams.entrySet()) {
					url.append("&").append(element.getKey()).append("=").append(element.getValue());
				}
				break;
			} else if (HttpMethod.POST.equals(httpMethod)) {
				body.append("<Folder");
				for (Map.Entry<String, Object> entry : otherParams.entrySet()) {
					body.append(" ").append(entry.getKey()).append("='").append(entry.getValue()).append("'");
				}
				body.append("/>");
			}
		}
		body.append("</Folders>");

		System.out.println(url);

		WebClient webClient = new WebClient();

		WebRequest webRequest = new WebRequest(new URL(url.toString()), httpMethod);
		if (serviceKey != null) {
			webRequest.setAdditionalHeader("serviceKey", serviceKey);
		}
		if (collection != null) {
			webRequest.setAdditionalHeader("collection", collection);
		}
		if (token != null) {
			webRequest.setAdditionalHeader("token", token);
		}
		if (HttpMethod.POST.equals(httpMethod)) {
			webRequest.setAdditionalHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);
			webRequest.setRequestBody(body.toString());
		}
		Page page = webClient.getPage(webRequest);
		String html = page.getWebResponse().getContentAsString();
		return Jsoup.parse(html).text();

	}
}
