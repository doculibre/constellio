package com.constellio.data.dao.services.transactionLog.replay;

import java.util.List;

public interface TransactionLogReplay<Object>{

	void replayTransactionLogs(List<Object> logs);
}
