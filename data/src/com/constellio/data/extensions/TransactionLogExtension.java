package com.constellio.data.extensions;

import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;

public class TransactionLogExtension {

	public ExtensionBooleanResult isDocumentFieldLoggedInTransactionLog(String field, String schema, String collection) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

}
