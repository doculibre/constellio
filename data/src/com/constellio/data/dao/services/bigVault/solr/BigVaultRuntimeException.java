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

	public static class TryingToRegisterListenerWithExistingId extends BigVaultRuntimeException {
		public TryingToRegisterListenerWithExistingId(String id) {
			super("Trying to register listener with existing id : " + id);
		}
	}
}
