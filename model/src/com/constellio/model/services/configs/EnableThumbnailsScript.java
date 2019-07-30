package com.constellio.model.services.configs;

import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.configs.SystemConfigurationScript;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class EnableThumbnailsScript implements SystemConfigurationScript<Boolean> {

	Logger LOGGER = LoggerFactory.getLogger(EnableThumbnailsScript.class);

	@Override
	public void onNewCollection(Boolean newValue, String collection, ModelLayerFactory modelLayerFactory) {
		//Nothing
	}

	@Override
	public void validate(Boolean newValue, ValidationErrors errors) {

	}

	@Override
	public void onValueChanged(Boolean previousValue, Boolean newValue, ModelLayerFactory modelLayerFactory) {

	}

	@Override
	public void onValueChanged(Boolean previousValue, Boolean newValue, ModelLayerFactory modelLayerFactory,
							   String collection) {

		Thread thread = new Thread() {
			@Override
			public void run() {
				if (Boolean.TRUE.equals(newValue) && Boolean.FALSE.equals(previousValue)) {

					for (MetadataSchemaType schemaType : modelLayerFactory.getMetadataSchemasManager()
							.getSchemaTypes(collection).getSchemaTypes()) {

						List<Metadata> contentMetadatas = schemaType.getAllMetadatas().onlyWithType(MetadataValueType.CONTENT);

						if (!contentMetadatas.isEmpty()) {
							activationConversionFlagForSchemaTypes(modelLayerFactory, schemaType, contentMetadatas);
						}
					}

				}
			}
		};
		thread.start();

	}

	private void activationConversionFlagForSchemaTypes(ModelLayerFactory modelLayerFactory,
														MetadataSchemaType schemaType,
														List<Metadata> contentMetadatas) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		String collection = schemaType.getCollection();
		LogicalSearchQuery query = new LogicalSearchQuery(from(schemaType).whereAny(contentMetadatas).isNotNull());

		long count = searchServices.getResultsCount(query);
		Iterator<List<String>> iterator = new BatchBuilderIterator<>(searchServices.recordsIdsIterator(query), 50000);

		SolrClient solrClient = modelLayerFactory.getDataLayerFactory().newRecordDao().getBigVaultServer().getNestedSolrServer();

		String task = "Building thumbnails of type '" + schemaType.getCode() + "' in collection '" + collection + "'";
		int migratedCount = 0;
		while (iterator.hasNext()) {
			LOGGER.info(task + " : " + migratedCount + "/" + count);
			List<String> ids = iterator.next();

			List<SolrInputDocument> inputDocuments = new ArrayList<>();
			for (String id : ids) {
				SolrInputDocument inputDocument = new SolrInputDocument();
				inputDocument.setField("id", id);

				Map<String, Object> incrementalSet = new HashMap<>();
				incrementalSet.put("set", "__TRUE__");
				inputDocument.setField(Schemas.MARKED_FOR_PREVIEW_CONVERSION.getDataStoreCode(), incrementalSet);

				inputDocuments.add(inputDocument);

				migratedCount++;
			}

			if (!inputDocuments.isEmpty()) {
				try {
					solrClient.add(inputDocuments);
				} catch (SolrServerException | IOException e) {
					throw new RuntimeException(e);
				}
			}


		}

		try {
			solrClient.commit(true, true, true);
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e);
		}

		LOGGER.info(task + " : " + count + "/" + count);
	}
}
