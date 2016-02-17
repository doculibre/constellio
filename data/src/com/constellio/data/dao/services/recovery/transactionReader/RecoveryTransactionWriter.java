package com.constellio.data.dao.services.recovery.transactionReader;

import com.constellio.data.dao.services.transactionLog.writer1.TransactionWriterV1;
import com.constellio.data.extensions.DataLayerSystemExtensions;

public class RecoveryTransactionWriter extends TransactionWriterV1 {

	public RecoveryTransactionWriter(DataLayerSystemExtensions extensions) {
		super(extensions);
	}

	@Override
	protected void appendDeletedByQuery(StringBuilder stringBuilder, String deletedByQuery) {
		throw new RuntimeException("Delete by query not supported in recovery mode");
	}
}
