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
package com.constellio.model.services.factories;

/**
 * Centralize all singletons in this class
 */
public class ConstellioGlobalValues {

	//	private ForkParsers forkParsers;
	//
	//	private SolrServers solrServers;
	//
	//	private SolrServerFactory solrServerFactory;
	//
	//	public ConstellioGlobalValues(SolrServerFactory solrServerFactory) {
	//		this.solrServerFactory = solrServerFactory;
	//	}
	//
	//	public synchronized ForkParsers getForkParsers() {
	//		if (forkParsers == null) {
	//			forkParsers = newForkParsers();
	//		}
	//		return forkParsers;
	//	}
	//
	//	ForkParsers newForkParsers() {
	//		return new ForkParsers(20);
	//	}
	//
	//	public void clear() {
	//		if (forkParsers != null) {
	//			forkParsers.close();
	//			forkParsers = null;
	//		}
	//		if (solrServers != null) {
	//			solrServers.close();
	//			solrServers = null;
	//		}
	//		if (solrServerFactory != null) {
	//			solrServerFactory.clear();
	//		}
	//	}
	//
	//	public SolrServers getSolrServers() {
	//		if (solrServers == null) {
	//			solrServers = newSolrServer();
	//		}
	//		return solrServers;
	//	}
	//
	//	SolrServers newSolrServer() {
	//		return new SolrServers(solrServerFactory, configs);
	//	}

}
