package com.constellio.app.modules.es.connectors.spi;

import java.util.List;

import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.services.crawler.DeleteEventOptions;
import com.constellio.model.services.factories.ModelLayerFactory;

/**
 * Threadsafe
 *
 * @author Nicolas
 *
 */
public interface ConnectorEventObserver {

	public void addUpdateEvents(List<ConnectorDocument> documents);

	public void addUpdateEvents(ConnectorDocument... documents);

	public void deleteEvents(List<ConnectorDocument> documents);

	public void deleteEvents(DeleteEventOptions deleteEventOptions, List<ConnectorDocument> documents);

	public void deleteEvents(ConnectorDocument... documents);

	public void deleteEvents(DeleteEventOptions deleteEventOptions, ConnectorDocument... documents);

	public void push(List<ConnectorDocument> documents);

	public void close();

	public void flush();

	public void cleanup();

	public ModelLayerFactory getModelLayerFactory();
}
