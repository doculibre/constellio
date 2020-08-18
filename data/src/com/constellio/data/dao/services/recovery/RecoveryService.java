package com.constellio.data.dao.services.recovery;

public interface RecoveryService {
	void startRollbackMode();

	void stopRollbackMode();

	void rollback(Throwable t);

	boolean isInRollbackMode();

}
