package com.constellio.model.services.search.services;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.Elevations;
import com.constellio.model.services.search.Elevations.QueryElevation;
import com.constellio.model.services.search.Elevations.QueryElevation.DocElevation;
import com.constellio.model.services.search.ElevationsView;
import org.apache.commons.lang.StringUtils;

import java.util.List;

//TODO make all methods thread safe
/* Currently, because the all the Constellio collections store in one solr collection, it is not possible
 * to change one Constellio collection configurations for the Elevation feature without any side effect on the
 * other Constellio collections.
 */
public class ElevationServiceImpl implements ElevationService {
	public static final String ELEVATE_FILE_NAME = "/elevate.xml";
	private BigVaultServer server;
	private ModelLayerFactory modelLayerFactory;

	public ElevationServiceImpl(BigVaultServer server, ModelLayerFactory modelLayerFactory) {
		this.server = server;
		this.modelLayerFactory = modelLayerFactory;
	}

	@Override
	public void elevate(Record record, String freeTextQuery) {
		if (StringUtils.isBlank(freeTextQuery)) {
			freeTextQuery = "*:*";
		}
		AtomicFileSystem solrFileSystem = server.getSolrFileSystem();

		DataWithVersion readData = solrFileSystem.readData(ELEVATE_FILE_NAME);
		ElevationsView anElevationsView = readData.getView(new ElevationsView());

		Elevations elevations = anElevationsView.getData();

		elevations.addOrUpdate(
				new QueryElevation().setQuery(freeTextQuery)
						.addDocElevation(new QueryElevation.DocElevation(record.getId(), false)));

		anElevationsView.setData(elevations);
		readData.setDataFromView(anElevationsView);

		solrFileSystem.writeData(ELEVATE_FILE_NAME, readData);
		server.reload();
	}

	@Override
	public void removeElevation(Record record, String freeTextQuery) {
		if (StringUtils.isBlank(freeTextQuery)) {
			freeTextQuery = "*:*";
		}
		AtomicFileSystem solrFileSystem = server.getSolrFileSystem();

		DataWithVersion readData = solrFileSystem.readData(ELEVATE_FILE_NAME);
		ElevationsView anElevationsView = readData.getView(new ElevationsView());

		Elevations elevations = anElevationsView.getData();
		elevations.removeElevation(freeTextQuery);
		anElevationsView.setData(elevations);
		readData.setDataFromView(anElevationsView);

		solrFileSystem.writeData(ELEVATE_FILE_NAME, readData);
		server.reload();
	}

	@Override
	public void removeCollectionElevation(String collection, String query) {
		if (StringUtils.isBlank(query)) {
			query = "*:*";
		}
		AtomicFileSystem solrFileSystem = server.getSolrFileSystem();

		DataWithVersion readData = solrFileSystem.readData(ELEVATE_FILE_NAME);
		ElevationsView anElevationsView = readData.getView(new ElevationsView());

		Elevations elevations = anElevationsView.getData();
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		elevations.removeCollectionElevation(schemas, query);
		anElevationsView.setData(elevations);
		readData.setDataFromView(anElevationsView);

		solrFileSystem.writeData(ELEVATE_FILE_NAME, readData);
		server.reload();
	}

	@Override
	public List<DocElevation> getCollectionElevation(String collection, String query) {
		if (StringUtils.isBlank(query)) {
			query = "*:*";
		}
		AtomicFileSystem solrFileSystem = server.getSolrFileSystem();

		DataWithVersion readData = solrFileSystem.readData(ELEVATE_FILE_NAME);
		ElevationsView anElevationsView = readData.getView(new ElevationsView());

		Elevations elevations = anElevationsView.getData();
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		return elevations.getCollectionElevation(schemas, query);
	}

	@Override
	public Elevations getCollectionElevations(String collection) {
		AtomicFileSystem solrFileSystem = server.getSolrFileSystem();

		DataWithVersion readData = solrFileSystem.readData(ELEVATE_FILE_NAME);
		ElevationsView anElevationsView = readData.getView(new ElevationsView());

		Elevations elevations = anElevationsView.getData();
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		return elevations.getCollectionElevations(schemas);
	}

	@Override
	public void removeCollectionElevations(String collection) {
		AtomicFileSystem solrFileSystem = server.getSolrFileSystem();

		DataWithVersion readData = solrFileSystem.readData(ELEVATE_FILE_NAME);
		ElevationsView anElevationsView = readData.getView(new ElevationsView());

		Elevations elevations = anElevationsView.getData();
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		elevations.removeCollectionElevations(schemas);
		anElevationsView.setData(elevations);
		readData.setDataFromView(anElevationsView);

		solrFileSystem.writeData(ELEVATE_FILE_NAME, readData);
		server.reload();
	}
}
