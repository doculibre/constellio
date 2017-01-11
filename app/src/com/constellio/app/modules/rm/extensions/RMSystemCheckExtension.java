package com.constellio.app.modules.rm.extensions;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.extensions.SystemCheckExtension;
import com.constellio.app.api.extensions.params.CollectionSystemCheckParams;
import com.constellio.app.api.extensions.params.TryRepairAutomaticValueParams;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
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

	RMSchemasRecordsServices rm;

	public final String METRIC_LOGICALLY_DELETED_ADM_UNITS = "rm.admUnits.logicallyDeleted";
	public final String METRIC_LOGICALLY_DELETED_CATEGORIES = "rm.categories.logicallyDeleted";

	public final String DELETED_ADM_UNITS = "rm.admUnit.deleted";
	public final String RESTORED_ADM_UNITS = "rm.admUnit.restored";
	public final String DELETED_CATEGORIES = "rm.category.deleted";
	public final String RESTORED_CATEGORIES = "rm.category.restored";

	public RMSystemCheckExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	@Override
	public boolean tryRepairAutomaticValue(TryRepairAutomaticValueParams params) {
		if (params.isMetadata(DecommissioningList.SCHEMA_TYPE, DecommissioningList.FOLDERS)) {
			DecommissioningList list = rm.wrapDecommissioningList(params.getRecord());
			for (String folderToRemove : params.getValuesToRemove()) {
				list.removeFolderDetail(folderToRemove);
			}

			return true;
		}
		return false;
	}

	@Override
	public void checkCollection(CollectionSystemCheckParams params) {
		boolean markedForReindexing = false;
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		for (AdministrativeUnit unit : rm.searchAdministrativeUnits(where(Schemas.LOGICALLY_DELETED_STATUS).isTrue())) {
			String label = unit.getCode() + " - " + unit.getTitle();
			params.getResultsBuilder().incrementMetric(METRIC_LOGICALLY_DELETED_ADM_UNITS);
			params.getResultsBuilder().markLogicallyDeletedRecordAsError(unit);
			if (params.isRepair()) {
				params.getResultsBuilder().markAsRepaired(unit.getId());
				try {
					recordServices.refresh(unit);
					if (!unit.getWrappedRecord().isDisconnected()) {
						recordServices.add(unit.set(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode(), false));

						if (recordServices.isLogicallyThenPhysicallyDeletable(unit.getWrappedRecord(), User.GOD)) {
							recordServices.logicallyDelete(unit.getWrappedRecord(), User.GOD);
							recordServices.physicallyDelete(unit.getWrappedRecord(), User.GOD);
							params.getResultsBuilder().addListItem(DELETED_ADM_UNITS, label);

						} else {
							params.getResultsBuilder().addListItem(RESTORED_ADM_UNITS, label);
						}
					} else {
						params.getResultsBuilder().addListItem(DELETED_ADM_UNITS, label);
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
			String label = category.getCode() + " - " + category.getTitle();
			params.getResultsBuilder().incrementMetric(METRIC_LOGICALLY_DELETED_CATEGORIES);
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
							params.getResultsBuilder().addListItem(DELETED_CATEGORIES, label);
						} else {
							params.getResultsBuilder().addListItem(RESTORED_CATEGORIES, label);
						}
					} else {
						params.getResultsBuilder().addListItem(DELETED_CATEGORIES, label);
					}

				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				} catch (NoSuchRecordWithId e) {
					//OK
				}
				markedForReindexing = true;
			}
		}

		if (markedForReindexing) {
			appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
		}
	}
}
