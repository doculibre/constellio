package com.constellio.app.modules.rm.extensions;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.extensions.SystemCheckExtension;
import com.constellio.app.api.extensions.params.CollectionSystemCheckParams;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;

public class RMSystemCheckExtension extends SystemCheckExtension {

	private static Logger LOGGER = LoggerFactory.getLogger(RMSystemCheckExtension.class);

	String collection;

	AppLayerFactory appLayerFactory;

	RecordServices recordServices;

	public RMSystemCheckExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
	}

	@Override
	public void checkCollection(CollectionSystemCheckParams params) {
		boolean markedForReindexing = false;
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		for (AdministrativeUnit unit : rm.searchAdministrativeUnits(where(Schemas.LOGICALLY_DELETED_STATUS).isTrue())) {
			params.getResultsBuilder().markLogicallyDeletedRecordAsError(unit);
			if (params.isRepair()) {
				params.getResultsBuilder().markAsRepaired(unit.getId());
				try {
					recordServices.refresh(unit);
					if (!unit.getWrappedRecord().isDisconnected()) {
						recordServices.add(unit.set(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode(), false));

						if (recordServices.isLogicallyThenPhysicallyDeletable(unit.getWrappedRecord(), User.GOD)) {
							LOGGER.info("Deleting record " + unit.getId() + " physically");
							recordServices.logicallyDelete(unit.getWrappedRecord(), User.GOD);
							recordServices.physicallyDelete(unit.getWrappedRecord(), User.GOD);

						}
					} else {
						LOGGER.info("Record " + unit.getId() + " already deleted physically");
					}
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				} catch (NoSuchRecordWithId e) {
					//OK
				}
				markedForReindexing = true;
			}
		}

		List<Category> categories = rm.searchCategorys(where(Schemas.LOGICALLY_DELETED_STATUS).isTrue());

		for (Category category : categories) {
			params.getResultsBuilder().markLogicallyDeletedRecordAsError(category);
			if (params.isRepair()) {
				params.getResultsBuilder().markAsRepaired(category.getId());
				try {
					recordServices.refresh(category);
					if (!category.getWrappedRecord().isDisconnected()) {
						recordServices.add(category.set(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode(), false));
						if (recordServices.isLogicallyThenPhysicallyDeletable(category.getWrappedRecord(), User.GOD)) {
							recordServices.logicallyDelete(category.getWrappedRecord(), User.GOD);
							recordServices.physicallyDelete(category.getWrappedRecord(), User.GOD);
						}
					}

				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				} catch (NoSuchRecordWithId e) {
					//OK
				}
				markedForReindexing = true;
			}
		}

		for (Category category : categories) {

		}
	}
}
