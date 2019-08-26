package com.constellio.model.entities.records.wrappers;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.background.FlushOldEmailToSend;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;

public class EmailToSendAcceptanceTest extends ConstellioTest {
	Users users = new Users();
	RecordServices recordServices;
	SearchServices searchServices;
	SchemasRecordsServices schemas;
	private LocalDate now = LocalDate.now();
	FlushOldEmailToSend flushOldEmailToSendThread;
	MetadataSchema schema;
	Metadata sendOn;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withTasksModule().withAllTest(users));
		givenTimeIs(now);

		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		schemas = new SchemasRecordsServices(zeCollection, getModelLayerFactory());

		flushOldEmailToSendThread = new FlushOldEmailToSend(getModelLayerFactory());
		schema = schemas.schema(EmailToSend.DEFAULT_SCHEMA);
		sendOn = schemas.emailToSend.sendOn();
	}

	@Test
	public void givenNewEmailToSendThenTryingCountEqualsZero()
			throws Exception {
		EmailToSend emailToSend = schemas.newEmailToSend();
		assertThat(emailToSend.getTryingCount()).isEqualTo(0d);
	}

	@Test
	public void givenEmailIsNotOlderThanStandard() throws RecordServicesException {
		EmailToSend emailToSend1 = schemas.newEmailToSend();
		emailToSend1.setSendOn(TimeProvider.getLocalDateTime());

		EmailToSend emailToSend2 = schemas.newEmailToSend();
		emailToSend2.setSendOn(TimeProvider.getLocalDateTime()
				.minusDays(3).plusSeconds(1));

		EmailToSend emailToSend3 = schemas.newEmailToSend();
		emailToSend3.setSendOn(TimeProvider.getLocalDateTime().plusDays(3));

		schemas.executeTransaction(new Transaction(
				emailToSend1,
				emailToSend2,
				emailToSend3));

		flushOldEmailToSendThread.run();
		long resultsCount = getAllEmailToSend(schema, sendOn);
		assertThat(resultsCount).isEqualTo(3);
	}

	@Test
	public void givenEmailIsOlderThanStandard() throws RecordServicesException {
		EmailToSend emailToSend1 = schemas.newEmailToSend();
		emailToSend1.setSendOn(TimeProvider.getLocalDateTime());

		EmailToSend emailToSend2 = schemas.newEmailToSend();
		emailToSend2.setSendOn(TimeProvider.getLocalDateTime()
				.minusDays(3).minusSeconds(1));

		EmailToSend emailToSend3 = schemas.newEmailToSend();
		emailToSend3.setSendOn(TimeProvider.getLocalDateTime().minusDays(12));

		schemas.executeTransaction(new Transaction(
				emailToSend1,
				emailToSend2,
				emailToSend3));

		flushOldEmailToSendThread.run();
		long resultsCount = getAllEmailToSend(schema, sendOn);
		assertThat(resultsCount).isEqualTo(1);
	}

	private long getAllEmailToSend(MetadataSchema schema, Metadata sendOn) {
		return searchServices.getResultsCount(LogicalSearchQuery.query(from(schema).where(sendOn).isNotNull()));
	}
}
