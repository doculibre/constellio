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
package com.constellio.data.dao.services.solr.serverFactories;

@SuppressWarnings("serial")
public class EmbeddedSolrServerFactoryRuntimeException extends RuntimeException {

	public EmbeddedSolrServerFactoryRuntimeException(String message) {
		super(message);
	}

	public EmbeddedSolrServerFactoryRuntimeException(Throwable cause) {
		super(cause);
	}

	public EmbeddedSolrServerFactoryRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class CannotCreateSolrServer extends EmbeddedSolrServerFactoryRuntimeException {

		public CannotCreateSolrServer(Throwable t) {
			super("Cannot create solr server", t);
		}
	}

}
