package com.constellio.app.modules.restapi.apis.v2;

import com.constellio.app.modules.restapi.RestApiConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.enums.ParsingBehavior;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.CommitCounter;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.QueryCounter;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;

import static com.constellio.sdk.tests.QueryCounter.ON_COLLECTION;
import static org.assertj.core.api.Assertions.assertThat;

public class BaseRestfulServiceV2AcceptanceTest extends ConstellioTest {

	protected String host;
	protected String token = "validToken", expiredToken = "expiredToken", fakeToken = "fakeToken";
	protected String dateFormat, dateTimeFormat;

	protected RMTestRecords records = new RMTestRecords(zeCollection);
	protected Users users = new Users();
	protected RecordServices recordServices;
	protected UserServices userServices;
	protected RMSchemasRecordsServices rm;
	protected MetadataSchemasManager schemasManager;

	protected CommitCounter commitCounter;
	protected QueryCounter queryCounter;

	protected static final String NOT_NULL_MESSAGE = "javax.validation.constraints.NotNull.message";
	protected static final String NOT_EMPTY_MESSAGE = "org.hibernate.validator.constraints.NotEmpty.message";

	@Before
	public void setUp() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioRestApiModule().withAllTest(users)
				.withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent());
		givenConfig(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS);

		dateFormat = getModelLayerFactory().getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.DATE_FORMAT);
		dateTimeFormat = getModelLayerFactory().getSystemConfigurationsManager().getValue(ConstellioEIMConfigs.DATE_TIME_FORMAT);

		host = "localhost:7070";

		givenConfig(RestApiConfigs.REST_API_URLS, "localhost:7070");

		recordServices = getModelLayerFactory().newRecordServices();
		userServices = getModelLayerFactory().newUserServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();

		userServices.execute(users.adminAddUpdateRequest()
				.addAccessToken(token, TimeProvider.getLocalDateTime().plusYears(1))
				.addAccessToken(expiredToken, TimeProvider.getLocalDateTime().minusDays(1)));

		recordServices.getRecordsCaches().disableVolatileCache();

		commitCounter = new CommitCounter(getDataLayerFactory());
		queryCounter = new QueryCounter(getDataLayerFactory(), ON_COLLECTION("zeCollection"));
	}

	protected void assertNoSolrQueryAndCommitCalls() {
		assertThat(queryCounter.newQueryCalls()).isEqualTo(0);
		assertThat(commitCounter.newCommitsCall().isEmpty());
	}
}
