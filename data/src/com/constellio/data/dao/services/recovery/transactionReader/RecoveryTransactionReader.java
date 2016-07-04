package com.constellio.data.dao.services.recovery.transactionReader;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.transactionLog.TransactionLogReadWriteServices;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.io.services.facades.IOServices;

public class RecoveryTransactionReader extends TransactionLogReadWriteServices {
	public RecoveryTransactionReader(boolean writeZZRecords, IOServices ioServices,
			DataLayerConfiguration configuration) {
		super(ioServices, configuration, new AcceptAllExtension());
	}

	private static class AcceptAllExtension extends DataLayerSystemExtensions {
		@Override
		public boolean isDocumentFieldLoggedInTransactionLog(String field, String schema, String collection,
				boolean defaultValue) {
			return true;
		}
	}
}
