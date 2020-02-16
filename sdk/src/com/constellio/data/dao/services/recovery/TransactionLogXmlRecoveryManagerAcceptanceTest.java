package com.constellio.data.dao.services.recovery;

import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TransactionLogXmlRecoveryManagerAcceptanceTest extends ConstellioTest {
	TransactionLogRecovery transactionLogXmlRecoveryManager;

	DataLayerFactory dataLayerFactory;

	@Before
	public void setUp()
			throws Exception {
		notAUnitItest = true;
		givenTransactionLogIsEnabled();
		givenDisabledAfterTestValidations();

		dataLayerFactory = getDataLayerFactory();
		transactionLogXmlRecoveryManager = spy(dataLayerFactory.getTransactionLogXmlRecoveryManager());

	}

	@Test
	public void whenIsInRollbackModeThenOk()
			throws Exception {
		assertThat(transactionLogXmlRecoveryManager.isInRollbackMode()).isFalse();
		transactionLogXmlRecoveryManager.startRollbackMode();
		assertThat(transactionLogXmlRecoveryManager.isInRollbackMode()).isTrue();
		transactionLogXmlRecoveryManager.rollback(null);
		assertThat(transactionLogXmlRecoveryManager.isInRollbackMode()).isFalse();
		transactionLogXmlRecoveryManager.startRollbackMode();
		assertThat(transactionLogXmlRecoveryManager.isInRollbackMode()).isTrue();
		transactionLogXmlRecoveryManager.stopRollbackMode();
		assertThat(transactionLogXmlRecoveryManager.isInRollbackMode()).isFalse();
	}

	@Test
	public void givenNotInRollBackModeThenNoCallForStopRollbackNorForRollbackButStart()
			throws Exception {
		transactionLogXmlRecoveryManager.stopRollbackMode();
		verify(transactionLogXmlRecoveryManager, times(0)).realStopRollback();

		Throwable t = new RuntimeException();
		transactionLogXmlRecoveryManager.rollback(t);
		verify(transactionLogXmlRecoveryManager, times(0)).realRollback(t);

		transactionLogXmlRecoveryManager.startRollbackMode();
		verify(transactionLogXmlRecoveryManager, times(1)).realStartRollback();
	}

	@Test
	public void givenInRollBackModeThenNoCallForStartButStopRollbackAndRollbackYes()
			throws Exception {
		transactionLogXmlRecoveryManager.startRollbackMode();
		transactionLogXmlRecoveryManager.startRollbackMode();
		verify(transactionLogXmlRecoveryManager, times(1)).realStartRollback();

		transactionLogXmlRecoveryManager.stopRollbackMode();
		verify(transactionLogXmlRecoveryManager, times(1)).realStopRollback();

		transactionLogXmlRecoveryManager.startRollbackMode();
		Throwable t = new RuntimeException();
		transactionLogXmlRecoveryManager.rollback(t);
		verify(transactionLogXmlRecoveryManager, times(1)).realRollback(t);
	}

	@After
	public void afterTest() {
		dataLayerFactory.getRecordsVaultServer().unregisterListener(transactionLogXmlRecoveryManager);
	}

	@Override
	protected boolean checkRollback() {
		return false;
	}

}
