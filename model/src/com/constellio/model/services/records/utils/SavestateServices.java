package com.constellio.model.services.records.utils;

import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromEveryTypesOfEveryCollection;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static com.constellio.model.services.search.query.logical.QueryExecutionMethod.USE_SOLR;

public class SavestateServices {

	private static final Logger LOGGER = LoggerFactory.getLogger(SavestateServices.class);

	ModelLayerFactory modelLayerFactory;

	public SavestateServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
	}

	public void buildSavestateV2FromPreviousBaseFile() {

	}


	public void createSavestateBaseFile(File file) {
		boolean writeZZRecords = modelLayerFactory.getSystemConfigs().isWriteZZRecordsInTlog();


		SavestateFileWriter savestateFileWriter = new SavestateFileWriter(modelLayerFactory, file);

		SearchServices searchServices = modelLayerFactory.newSearchServices();

		int loadedRecordMemoryInBytes = 100_000_000;
		int[] maxRecordSizeSteps = new int[]{1_000, 10_000, 100_000, 1_000_000, 10_000_000, 1_000_000_000};

		for (int maxRecordSize : maxRecordSizeSteps) {
			int batchSize = Math.max(1, loadedRecordMemoryInBytes / maxRecordSize / 2);
			LogicalSearchQuery query = new LogicalSearchQuery();
			query.setQueryExecutionMethod(USE_SOLR);

			LogicalSearchCondition condition =
					fromEveryTypesOfEveryCollection().where(Schemas.ESTIMATED_SIZE).isLessOrEqualThan(maxRecordSize);
			if (maxRecordSize == 1000) {
				condition = condition.orWhere(Schemas.ESTIMATED_SIZE).isNull();
			}

			if (writeZZRecords) {
				query.setCondition(condition);
			} else {

				query.setCondition(LogicalSearchQueryOperators.allConditions(
						condition,
						where(Schemas.IDENTIFIER).isNot(LogicalSearchQueryOperators.startingWithText("ZZ"))
				));
			}

			SearchResponseIterator<List<Record>> recordIterator = searchServices.recordsIterator(query, batchSize).inBatches();

			String taskName = "Exporting records with size smaller than " + maxRecordSize + " bytes  in batch of " + batchSize;
			int counter = 0;
			while (recordIterator.hasNext()) {
				LOGGER.info(taskName + " " + counter + " / " + recordIterator.getNumFound());
				List<Record> records = recordIterator.next();

				savestateFileWriter.write(records);
				counter++;
			}
			LOGGER.info(taskName + " " + counter + " / " + recordIterator.getNumFound());

		}
		savestateFileWriter.close();
	}
}
