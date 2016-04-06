package com.constellio.data.dao.services.recovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.constellio.data.dao.services.factories.DataLayerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;
import com.sun.star.uno.RuntimeException;

public class TransactionLogRecoveryManagerAcceptanceTest extends ConstellioTest {
	TransactionLogRecoveryManager transactionLogRecoveryManager;

	DataLayerFactory dataLayerFactory;

	@Before
	public void setUp()
			throws Exception {
		notAUnitItest = true;
		givenTransactionLogIsEnabled();
		givenDisabledAfterTestValidations();

		dataLayerFactory = getDataLayerFactory();
		transactionLogRecoveryManager = spy(dataLayerFactory.getTransactionLogRecoveryManager());

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
		dataLayerFactory.getRecordsVaultServer().unregisterListener(transactionLogRecoveryManager);
	}

	@Override
	protected boolean checkRollback() {
		return false;
	}

}
