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
package com.constellio.data.dao.services.bigVault.solr;

import org.apache.solr.common.params.SolrParams;

@SuppressWarnings("serial")
public class BigVaultRuntimeException extends RuntimeException {

	public BigVaultRuntimeException(String message) {
		super(message);
	}

	public BigVaultRuntimeException(String message, Throwable t) {
		super(message, t);
	}

	public static class CannotListDocuments extends BigVaultRuntimeException {

		public CannotListDocuments(Throwable t) {
			super("Cannot list documents", t);
		}
	}

	public static class CannotQuerySingleDocument extends BigVaultRuntimeException {

		public CannotQuerySingleDocument(Throwable t) {
			super("Cannot list documents", t);
		}
	}

	public static class BadRequest extends BigVaultRuntimeException {
		public BadRequest(BigVaultServerTransaction transaction, Exception e) {
			super("Bad request caused by transaction : \n" + SolrUtils.toString(transaction), e);
		}

		public BadRequest(SolrParams params, Exception e) {
			super("Bad request : '" + SolrUtils.toString(params) + "'", e);
		}
	}

	public static class SolrInternalError extends BigVaultRuntimeException {
		public SolrInternalError(Exception e) {
			super("Solr internal error", e);
		}

		public SolrInternalError(BigVaultServerTransaction transaction, Exception e) {
			super("Solr internal error when handling update request : \n" + SolrUtils
					.toString(transaction), e);
		}
	}
}
