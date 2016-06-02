package com.constellio.app.modules.complementary.esRmRobots.services;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class ESRMRobotsServices {
	
	private static final Logger LOGGER = Logger.getLogger(ESRMRobotsServices.class);

	ModelLayerFactory modelLayerFactory;

	public ESRMRobotsServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	public void deleteRobotFoldersAndDocuments(final User currentUser, final String robotId, final ProgressInfo progressInfo)
			throws Exception {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(currentUser.getCollection(), modelLayerFactory);
		final RecordDeleteOptions deleteOptions = new RecordDeleteOptions().setReferencesToNull(true);
		final LogicalSearchCondition documentsCondition = from(rm.documentSchemaType())
				.where(rm.document.schema().get(Document.CREATED_BY_ROBOT)).isEqualTo(robotId);
		final LogicalSearchCondition foldersCondition = from(rm.folder.schemaType())
				.where(rm.folder.schema().get(Folder.CREATED_BY_ROBOT)).isEqualTo(robotId);

		final RecordServices recordServices = modelLayerFactory.newRecordServices();
		final SearchServices searchServices = modelLayerFactory.newSearchServices();

		final long total = searchServices.getResultsCount(documentsCondition)
				+ searchServices.getResultsCount(foldersCondition);
		progressInfo.setEnd(total);
		if (total == 0) {
			progressInfo.setDone(true);
		} else {
			ActionExecutorInBatch batch = new ActionExecutorInBatch(searchServices, "Delete records", 10) {
				@Override
				public void doActionOnBatch(List<Record> records)
						throws Exception {

					for (Record record : records) {
						try {
							recordServices.getDocumentById(record.getId());
							recordServices.physicallyDeleteNoMatterTheStatus(record, currentUser, deleteOptions);

						} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
							//Already deleted
						} catch (Exception e) {
							LOGGER.warn("Error while deleting record", e);
							String stackTrace = ExceptionUtils.getStackTrace(e);
							progressInfo.getErrorMessages().add(stackTrace);
						}
					}

					progressInfo.setCurrentState(total
							- searchServices.getResultsCount(documentsCondition)
							- searchServices.getResultsCount(foldersCondition));

				}
			};
			batch.execute(documentsCondition);
			batch.execute(foldersCondition);
		}
	}

}
