package com.constellio.app.api.benchmarks;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import com.constellio.sdk.tests.annotations.SlowTest;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

@SlowTest
public class BenchmarkWebServiceAcceptTest extends ConstellioTest {

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

	String[] validArgumentsForBenchmarkSearchAction;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records));

		category = records.getCategory_X100();
		retentionRule = records.getRule1();
		administrativeUnit = records.getUnit10a();

		validArgumentsForBenchmarkSearchAction = new String[] {
				"action=search",
				"freeTextSearch=test"
		};

		rmSchemasRecordsServices = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
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

		assertThat(callBenchmark(null, null, zeCollection, validArgumentsForBenchmarkSearchAction)).isEqualTo(SC_UNAUTHORIZED);

		assertThat(callBenchmark(bobServiceKey, null, zeCollection, validArgumentsForBenchmarkSearchAction))
				.isEqualTo(SC_UNAUTHORIZED);

		assertThat(callBenchmark(null, bobToken, zeCollection, validArgumentsForBenchmarkSearchAction))
				.isEqualTo(SC_UNAUTHORIZED);

		assertThat(callBenchmark(bobServiceKey, "pouet", zeCollection, validArgumentsForBenchmarkSearchAction))
				.isEqualTo(SC_UNAUTHORIZED);

		assertThat(callBenchmark(bobServiceKey, aliceToken, zeCollection, validArgumentsForBenchmarkSearchAction))
				.isEqualTo(SC_UNAUTHORIZED);

		assertThat(callBenchmark(aliceServiceKey, aliceToken, zeCollection, validArgumentsForBenchmarkSearchAction))
				.isEqualTo(SC_UNAUTHORIZED);

		int response = callBenchmark(bobServiceKey, bobToken, zeCollection, validArgumentsForBenchmarkSearchAction);
		assertThat(response).isEqualTo(SC_OK);
	}

	@Test
	public void givenSearchBenchmarkCallThenFailIfInvalidParameters()
			throws Exception {

		assertThat(callBenchmark(bobServiceKey, bobToken, zeCollection, "action=search")).isEqualTo(SC_BAD_REQUEST);
		assertThat(callBenchmark(bobServiceKey, bobToken, zeCollection, "action=search", "freeTextSearch=*", "qtyOfResults=ze"))
				.isEqualTo(SC_BAD_REQUEST);
		assertThat(callBenchmark(bobServiceKey, bobToken, zeCollection, "action=search", "freeTextSearch=*")).isEqualTo(SC_OK);
		assertThat(callBenchmark(bobServiceKey, bobToken, zeCollection, "action=search", "freeTextSearch=*", "qtyOfResults=10"))
				.isEqualTo(SC_OK);
		assertThat(callBenchmark(bobServiceKey, bobToken, zeCollection, "action=search", "freeTextSearch=test")).isEqualTo(SC_OK);
	}

	private int callBenchmark(String serviceKey, String token, String collection, String... otherParams)
			throws IOException {

		String url = "http://localhost:7070/constellio/benchmark?";

		if (serviceKey != null) {
			if (!url.endsWith("?")) {
				url += "&";
			}
			url += "serviceKey=" + serviceKey;
		}
		if (token != null) {
			if (!url.endsWith("?")) {
				url += "&";
			}
			url += "token=" + token;
		}

		if (collection != null) {
			if (!url.endsWith("?")) {
				url += "&";
			}
			url += "collection=" + collection;
		}

		for (String otherParam : otherParams) {
			url += "&" + otherParam;
		}

		System.out.println(url);

		WebClient webClient = new WebClient();

		try {
			Page page = webClient.getPage(url);
			return page.getWebResponse().getStatusCode();
		} catch (FailingHttpStatusCodeException e) {
			return e.getStatusCode();
		}
	}
}
