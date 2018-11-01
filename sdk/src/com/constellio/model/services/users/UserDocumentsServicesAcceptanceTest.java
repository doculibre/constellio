package com.constellio.model.services.users;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.enums.ParsingBehavior;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class UserDocumentsServicesAcceptanceTest extends ConstellioTest {

	private RMSchemasRecordsServices rm;
	private UserDocumentsServices userDocumentsServices;
	private RecordServices recordServices;
	private Users users = new Users();

	private Content content;
	private User zeChuck;

	@Before
	public void setUp() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users));
		givenBackgroundThreadsEnabled();
		givenConfig(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		userDocumentsServices = new UserDocumentsServices(getModelLayerFactory());

		zeChuck = users.chuckNorrisIn(zeCollection);

		File file = newTempFileWithContent("test.txt", "This is a test");
		ContentManager contentManager = getModelLayerFactory().getContentManager();
		ContentVersionDataSummary data = contentManager.upload(file);
		content = contentManager.createMajor(users.chuckNorrisIn(zeCollection), "test.txt", data);
	}

	@Test
	public void givenSpaceQuotaActivatedAndLimitReachedThenReturnsTrue() throws Exception {
		givenConfig(ConstellioEIMConfigs.SPACE_QUOTA_FOR_USER_DOCUMENTS, 0);
		waitForBatchProcess();

		long newUserDocumentSize = 500_000;
		boolean limitReached =
				userDocumentsServices.isSpaceLimitReached(zeChuck.getUsername(), zeCollection, newUserDocumentSize);
		assertThat(limitReached).isTrue();
	}

	@Test
	public void givenSpaceQuotaActivatedAndLimitNotReachedThenReturnFalse() throws Exception {
		givenConfig(ConstellioEIMConfigs.SPACE_QUOTA_FOR_USER_DOCUMENTS, 1);
		waitForBatchProcess();

		recordServices.add(rm.newUserDocumentWithId("userDoc").setContent(content).setUser(zeChuck));
		waitForBatchProcess();

		long newUserDocumentSize = 500_000;
		boolean limitReached =
				userDocumentsServices.isSpaceLimitReached(zeChuck.getUsername(), zeCollection, newUserDocumentSize);
		assertThat(limitReached).isFalse();
	}

	@Test
	public void givenSpaceQuotaIsEqualsToOneAndNoUserDocumentThenAvailableSpaceIsOne() throws Exception {
		givenConfig(ConstellioEIMConfigs.SPACE_QUOTA_FOR_USER_DOCUMENTS, 1);
		waitForBatchProcess();

		double size = userDocumentsServices.getAvailableSpaceInMegaBytes(zeChuck.getUsername(), zeCollection);
		assertThat(size).isEqualTo(1);
	}

	@Test
	public void givenSpaceQuotaIsEqualsToOneAndOneUserDocumentThenAvailableSpaceIsCorrectlyComputed() throws Exception {
		givenConfig(ConstellioEIMConfigs.SPACE_QUOTA_FOR_USER_DOCUMENTS, 1);
		waitForBatchProcess();

		recordServices.add(rm.newUserDocumentWithId("userDoc").setContent(content).setUser(zeChuck));
		waitForBatchProcess();

		double size = userDocumentsServices.getAvailableSpaceInMegaBytes(zeChuck.getUsername(), zeCollection);
		assertThat(size).isEqualTo(1 - convertToMegaByte(content.getCurrentVersion().getLength()));
	}

	@Test
	public void givenSpaceQuotaIsEqualsToOneAndMultipleUserDocumentThenAvailableSpaceIsCorrectlyComputed()
			throws Exception {
		givenConfig(ConstellioEIMConfigs.SPACE_QUOTA_FOR_USER_DOCUMENTS, 1);
		waitForBatchProcess();

		Transaction transaction = new Transaction();
		transaction.add(rm.newUserDocumentWithId("userDoc1").setContent(content).setUser(zeChuck));
		transaction.add(rm.newUserDocumentWithId("userDoc2").setContent(content).setUser(zeChuck));
		transaction.add(rm.newUserDocumentWithId("userDoc3").setContent(content).setUser(zeChuck));
		recordServices.execute(transaction);
		waitForBatchProcess();

		double size = userDocumentsServices.getAvailableSpaceInMegaBytes(zeChuck.getUsername(), zeCollection);
		assertThat(size).isEqualTo(1 - (3 * convertToMegaByte(content.getCurrentVersion().getLength())));
	}

	@Test
	public void givenSpaceQuotaIsNegativeThenActivatedReturnFalse() throws Exception {
		givenConfig(ConstellioEIMConfigs.SPACE_QUOTA_FOR_USER_DOCUMENTS, -1);
		waitForBatchProcess();

		assertThat(userDocumentsServices.isQuotaSpaceConfigActivated()).isFalse();
	}

	@Test
	public void givenSpaceQuotaIsPositiveThenActivatedReturnTrue() {
		givenConfig(ConstellioEIMConfigs.SPACE_QUOTA_FOR_USER_DOCUMENTS, 0);

		assertThat(userDocumentsServices.isQuotaSpaceConfigActivated()).isTrue();
	}

	private double convertToMegaByte(double valueInMegaBytes) {
		return valueInMegaBytes * Math.pow(10, -6);
	}

}
