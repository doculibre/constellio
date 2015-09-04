/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.data.extensions;

import static com.constellio.data.frameworks.extensions.ExtensionUtils.getBooleanValue;

import org.apache.solr.common.params.SolrParams;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.frameworks.extensions.ExtensionUtils.BooleanCaller;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;

public class DataLayerSystemExtensions {

	//------------ Extension points -----------
	public VaultBehaviorsList<BigVaultServerExtension> bigVaultServerExtension = new VaultBehaviorsList<>();
	public VaultBehaviorsList<TransactionLogExtension> transactionLogExtensions = new VaultBehaviorsList<>();

	public void afterQuery(SolrParams params, long qtime) {
		for (BigVaultServerExtension extension : bigVaultServerExtension) {
			extension.afterQuery(params, qtime);
		}
	}

	public void afterUpdate(BigVaultServerTransaction transaction, long qtime) {
		for (BigVaultServerExtension extension : bigVaultServerExtension) {
			extension.afterUpdate(transaction, qtime);
		}
	}

	//----------------- Callers ---------------

	public boolean isDocumentFieldLoggedInTransactionLog(final String field, final String schema, final String collection,
			boolean defaultValue) {
		return getBooleanValue(transactionLogExtensions, defaultValue, new BooleanCaller<TransactionLogExtension>() {
			@Override
			public ExtensionBooleanResult call(TransactionLogExtension extension) {
				return extension.isDocumentFieldLoggedInTransactionLog(field, schema, collection);
			}
		});
	}

}
