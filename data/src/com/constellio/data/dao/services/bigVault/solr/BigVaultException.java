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

import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;

@SuppressWarnings("serial")
public class BigVaultException extends Exception {

	private BigVaultException(String message) {
		super(message);
	}

	private BigVaultException(String message, Throwable t) {
		super(message, t);
	}

	public static class CouldNotExecuteQuery extends BigVaultException {

		public CouldNotExecuteQuery(String query, Throwable t) {
			super("Could not execute " + query, t);
		}

		public CouldNotExecuteQuery(String query, SolrParams params, Throwable t) {
			super("Could not execute " + query + " with params : " + SolrUtils.toString(params), t);
		}

		public CouldNotExecuteQuery(String query, SolrInputDocument document, Throwable t) {
			super("Could not execute " + query + " with document : " + SolrUtils.toIdString(document), t);
		}

	}

	public static class NoResult extends BigVaultException {

		public NoResult(SolrParams params) {
			super("No result for query '" + SolrUtils.toString(params) + "'");
		}

	}

	public static class NonUniqueResult extends BigVaultException {

		public NonUniqueResult(SolrParams params, SolrDocumentList list) {
			super("Non unique result for query '" + SolrUtils.toString(params) + "'. Found results : "
					+ SolrUtils.toIdString(list));
		}

	}

	public static class PreviousChangesCouldNotBeCommitted extends BigVaultException {

		public PreviousChangesCouldNotBeCommitted(Throwable t) {
			super("Previous changes could not be committed", t);
		}

	}

	public static class OptimisticLocking extends BigVaultException {

		final String id;

		final long version;

		public OptimisticLocking(Throwable t) {
			this(retreiveId(t.getMessage()), retreiveVersion(t.getMessage()), t);
		}

		public OptimisticLocking(String id, Long version, Throwable t) {
			super(getMessage(id, version), t);
			this.id = id;
			if (id == null){
				System.out.println("BigVaultException.OptimisticLocking.OptimisticLocking()");
			}
			this.version = version;
		}

		private static String getMessage(String id, Long version) {
			return "Optimistic locking while saving solr document with id '" + id + "' in version '" + version + "'";
		}

		public static Long retreiveVersion(String solrMessage) {
			if (checkIfDocNotFound(solrMessage) != null) {
				return 1L;
			}
			String phraseBefore = " expected=";
			String phraseAfter = " actual=";
			int idBeginIndex = solrMessage.indexOf(phraseBefore) + phraseBefore.length();
			int idEndIndex = solrMessage.indexOf(phraseAfter);
			return Long.valueOf(solrMessage.substring(idBeginIndex, idEndIndex));
		}

		public static String retreiveId(String solrMessage) {
			String errMsg = checkIfDocNotFound(solrMessage);
			if (errMsg != null)
				return errMsg;

			String phraseBefore = "version conflict for ";
			String phraseAfter = " expected=";
			int idBeginIndex = solrMessage.indexOf(phraseBefore) + phraseBefore.length();
			int idEndIndex = solrMessage.indexOf(phraseAfter);
			return solrMessage.substring(idBeginIndex, idEndIndex);
		}

		private static String checkIfDocNotFound(String solrMessage) {
			int docNotFoundIdx = -1;
			String docNotFoundMsg = "Document not found for update.  id=";
			if ((docNotFoundIdx = solrMessage.indexOf(docNotFoundMsg)) != -1) {
				return solrMessage.substring(docNotFoundIdx + docNotFoundMsg.length());
			}
			return null;
		}

		public String getId() {
			return id;
		}

		public long getVersion() {
			return version;
		}

	}

}
