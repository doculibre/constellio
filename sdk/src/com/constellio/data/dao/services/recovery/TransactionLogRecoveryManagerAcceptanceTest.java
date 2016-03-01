package com.constellio.data.dao.services.recovery;

import static com.constellio.app.modules.rm.model.enums.CopyType.PRINCIPAL;
import static com.constellio.sdk.tests.SaveStateFeatureAcceptTest.verifySameContentOfUnzippedSaveState;
import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.tools.components.listAddRemove.ListAddRemoveDateFieldWebElement;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimistickLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.sun.star.uno.RuntimeException;

public class TransactionLogRecoveryManagerAcceptanceTest extends ConstellioTest {
	TransactionLogRecoveryManager transactionLogRecoveryManager;

	@Before
	public void setUp()
			throws Exception {
		givenTransactionLogIsEnabled();
		givenDisabledAfterTestValidations();

		transactionLogRecoveryManager = spy(getDataLayerFactory().getTransactionLogRecoveryManager());
	}

	@Test
	public void whenIsInRollbackModeThenOk()
			throws Exception {
		assertThat(transactionLogRecoveryManager.isInRollbackMode()).isFalse();
		transactionLogRecoveryManager.startRollbackMode();
		assertThat(transactionLogRecoveryManager.isInRollbackMode()).isTrue();
		transactionLogRecoveryManager.rollback(null);
		assertThat(transactionLogRecoveryManager.isInRollbackMode()).isFalse();
		transactionLogRecoveryManager.startRollbackMode();
		assertThat(transactionLogRecoveryManager.isInRollbackMode()).isTrue();
		transactionLogRecoveryManager.stopRollbackMode();
		assertThat(transactionLogRecoveryManager.isInRollbackMode()).isFalse();
	}

	@Test
	public void givenNotInRollBackModeThenNoCallForStopRollbackNorForRollbackButStart()
			throws Exception {
		transactionLogRecoveryManager.stopRollbackMode();
		verify(transactionLogRecoveryManager, times(0)).realStopRollback();

		Throwable t = new RuntimeException();
		transactionLogRecoveryManager.rollback(t);
		verify(transactionLogRecoveryManager, times(0)).realRollback(t);

		transactionLogRecoveryManager.startRollbackMode();
		verify(transactionLogRecoveryManager, times(1)).realStartRollback();
	}

	@Test
	public void givenInRollBackModeThenNoCallForStartButStopRollbackAndRollbackYes()
			throws Exception {
		transactionLogRecoveryManager.startRollbackMode();
		transactionLogRecoveryManager.startRollbackMode();
		verify(transactionLogRecoveryManager, times(1)).realStartRollback();

		transactionLogRecoveryManager.stopRollbackMode();
		verify(transactionLogRecoveryManager, times(1)).realStopRollback();

		transactionLogRecoveryManager.startRollbackMode();
		Throwable t = new RuntimeException();
		transactionLogRecoveryManager.rollback(t);
		verify(transactionLogRecoveryManager, times(1)).realRollback(t);
	}

	@After
	public void afterTest() {
		getDataLayerFactory().getRecordsVaultServer().unregisterListener(transactionLogRecoveryManager);
	}

	@Override
	protected boolean checkRollback() {
		return false;
	}

}
