package com.constellio.app.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.joda.time.LocalDate;
import org.jsoup.Jsoup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;

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

	String[] validArgumentsForFolderCreation;
	String[] missingArgumentsForFolderCreation;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records));

		category = records.getCategory_X100();
		retentionRule = records.getRule1();
		administrativeUnit = records.getUnit10a();

		validArgumentsForFolderCreation = new String[] {
				"title=Squatre",
				"categoryEntered=" + category.getCode(),
				"retentionRuleEntered=" + retentionRule.getCode(),
				"administrativeUnitEntered=" + administrativeUnit.getCode(),
				"createdBy=" + bobGratton,
				"createdOn=" + nowLocalDate,
				"openingDate=" + nowLocalDate
		};

		rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();
		bobToken = userServices.generateToken(bobGratton);
		aliceToken = userServices.generateToken(aliceWonderland);
		userServices.addUpdateUserCredential(users.bob().withServiceKey(bobServiceKey).withSystemAdminPermission());
		userServices.addUpdateUserCredential(users.alice().withServiceKey(aliceServiceKey));
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
			callCreateFolder(null, null, zeCollection, validArgumentsForFolderCreation);
			fail("Exception expected");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
		}

		try {
			callCreateFolder(bobServiceKey, null, zeCollection, validArgumentsForFolderCreation);
			fail("Exception expected");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
		}

		try {
			callCreateFolder(null, bobToken, zeCollection, validArgumentsForFolderCreation);
			fail("Exception expected");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
		}

		try {
			callCreateFolder(bobServiceKey, "pouet", zeCollection, validArgumentsForFolderCreation);
			fail("Exception expected");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
		}

		try {
			callCreateFolder(bobServiceKey, aliceToken, zeCollection, validArgumentsForFolderCreation);
			fail("Exception expected");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
		}

		try {
			callCreateFolder(aliceServiceKey, aliceToken, zeCollection, validArgumentsForFolderCreation);
			fail("Exception expected because alice is not sysadmin");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
		}

		try {
			callCreateFolder(bobServiceKey, bobToken, null, validArgumentsForFolderCreation);
			fail("Exception expected because collection parameter");
		} catch (FailingHttpStatusCodeException e) {
			assertThat(e.getStatusCode()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
		}

		String recordId = callCreateFolder(bobServiceKey, bobToken, zeCollection, validArgumentsForFolderCreation);
		assertThat(recordId).isNotEmpty();
	}

	@Test
	public void givenWebServiceIsEnabledAndValidArgumentsWhenExcuteThenCreateFolder()
			throws Exception {

		String recordId = callCreateFolder(bobServiceKey, bobToken, zeCollection, validArgumentsForFolderCreation);

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

		missingArgumentsForFolderCreation = new String[] {
				"createdBy=" + bobGratton,
				"createdOn=" + nowLocalDate,
		};

		String message = callCreateFolder(bobServiceKey, bobToken, zeCollection, missingArgumentsForFolderCreation);

		assertThat(message).contains("Métadonnée Date d'ouverture requise", "Métadonnée Titre requise");
	}

	private String callCreateFolder(String serviceKey, String token, String collection, String... otherParams)
			throws IOException {

		String url = "http://localhost:7070/constellio/createRecord?schema=folder_default";

		for (String otherParam : otherParams) {
			url += "&" + otherParam;
		}

		System.out.println(url);

		WebClient webClient = new WebClient();
		WebRequest webRequest = new WebRequest(new URL(url));
		if (serviceKey != null) {
			webRequest.setAdditionalHeader("serviceKey", serviceKey);
		}
		if (collection != null) {
			webRequest.setAdditionalHeader("collection", collection);
		}
		if (token != null) {
			webRequest.setAdditionalHeader("token", token);
		}
		Page page = webClient.getPage(webRequest);
		String html = page.getWebResponse().getContentAsString();
		String content = Jsoup.parse(html).text();
		return content;

	}
}
