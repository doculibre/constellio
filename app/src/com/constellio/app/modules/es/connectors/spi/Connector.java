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
package com.constellio.app.modules.es.connectors.spi;

import java.util.List;

import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.entities.records.Record;

public abstract class Connector {

	protected ESSchemasRecordsServices es;
	protected ConnectorLogger logger;
	private Record instance;
	protected ConnectorEventObserver eventObserver;

	public abstract List<ConnectorJob> getJobs();

	protected abstract void initialize(Record instance);

	public abstract List<String> fetchTokens(String username);

	public abstract List<String> getConnectorDocumentTypes();

	public abstract void start();

	public abstract void resume();

	public Connector initialize(ConnectorLogger logger, Record instance, ConnectorEventObserver eventObserver,
			ESSchemasRecordsServices es) {
		this.logger = logger;
		this.instance = instance;
		this.eventObserver = eventObserver;
		this.es = es;
		initialize(instance);
		return this;
	}

	public ConnectorLogger getLogger() {
		return logger;
	}

	public ConnectorEventObserver getEventObserver() {
		return eventObserver;
	}

	public ESSchemasRecordsServices getEs() {
		return es;
	}
}
