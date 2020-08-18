package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeSaveEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.taxonomies.TaxonomiesSearchServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by constellios on 2017-04-11.
 */
public class RMEventRecordExtension extends RecordExtension {

	private final static Logger LOGGER = LoggerFactory.getLogger(RMEventRecordExtension.class);

	private final RMSchemasRecordsServices rmSchema;
	final String collection;

	final ModelLayerFactory modelLayerFactory;
	final RecordServices recordServices;
	final SearchServices searchServices;
	final TaxonomiesSearchServices taxonomiesSearchServices;
	final TaxonomiesManager taxonomyManager;
	final RMSchemasRecordsServices rm;
	final RMConfigs configs;

	public RMEventRecordExtension(String collection, ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		rmSchema = new RMSchemasRecordsServices(collection, modelLayerFactory);
		recordServices = modelLayerFactory.newRecordServices();
		searchServices = modelLayerFactory.newSearchServices();
		taxonomiesSearchServices = modelLayerFactory.newTaxonomiesSearchService();
		taxonomyManager = modelLayerFactory.getTaxonomiesManager();
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		this.configs = new RMConfigs(modelLayerFactory.getSystemConfigurationsManager());
	}

	@Override
	public void recordInCreationBeforeSave(RecordInCreationBeforeSaveEvent event) {
		if (event.isSchemaType(Event.SCHEMA_TYPE)) {
			Event wrappedEvent = rmSchema.wrapEvent(event.getRecord());

			if (wrappedEvent.getType().equals(EventType.FOLDER_DESTRUCTION) || wrappedEvent.getType().equals(EventType.FOLDER_DEPOSIT)
				|| wrappedEvent.getType().equals(EventType.FOLDER_RELOCATION) || wrappedEvent.getType().equals(EventType.RECEIVE_FOLDER)
				|| wrappedEvent.getType().equals(EventType.RECEIVE_CONTAINER)) {
				try {
					DecommissioningList decommissioningList = rm.getDecommissioningList(wrappedEvent.getRecordId());

					AdministrativeUnit administrativeUnit = rmSchema.getAdministrativeUnit(decommissioningList.getAdministrativeUnit());
					wrappedEvent.setEventPrincipalPath(administrativeUnit.getPaths().get(0) + "/" + event.getRecord());
				} catch (Exception e) {
					// When event are created before the DecommissioningList.
					LOGGER.warn("recordInCreationBeforeSave, When event are created before the DecommissioningList, Should not happen in production only in unit testing.");
				}
			}
		}
	}
}
