package com.constellio.model.services.background;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.cache.PersistedIdsServices;
import com.constellio.model.services.records.cache.PersistedSortValuesServices;
import com.constellio.model.services.records.cache.PersistedSortValuesServices.SortValueList;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.search.SearchServices;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class BuildRecordIdListAndSortValuesBackgroundAction implements Runnable {

	private ModelLayerFactory modelLayerFactory;
	private SearchServices searchServices;

	private LocalDateTime lastIdExecution;
	private LocalDateTime lastSortValueExecution;

	public BuildRecordIdListAndSortValuesBackgroundAction(ModelLayerFactory modelLayerFactory) {
		this.searchServices = modelLayerFactory.newSearchServices();
		this.modelLayerFactory = modelLayerFactory;
	}

	@Override
	public synchronized void run() {
		boolean officeHours = TimeProvider.getLocalDateTime().getHourOfDay() >= 7
							  && TimeProvider.getLocalDateTime().getHourOfDay() <= 18;


		if (!officeHours && ReindexingServices.getReindexingInfos() == null && FoldersLocator.usingAppWrapper()) {
			//Old way
			writeIdsInLocalWorkFolder();

			if (lastIdExecution == null) {
				lastIdExecution = new PersistedIdsServices(modelLayerFactory).getLastVersionTimeStamp();
			}

			if (lastSortValueExecution == null) {
				lastSortValueExecution = new PersistedIdsServices(modelLayerFactory).getLastVersionTimeStamp();
			}

			//New way
			if (lastIdExecution == null || lastIdExecution.plusHours(15).isBefore(TimeProvider.getLocalDateTime())) {
				lastIdExecution = new PersistedIdsServices(modelLayerFactory).retreiveAndRewriteRecordIdsFile();
			}

			//My way
			if (lastSortValueExecution == null || lastSortValueExecution.plusHours(15).isBefore(TimeProvider.getLocalDateTime())) {
				SortValueList valueList = new PersistedSortValuesServices(modelLayerFactory).retreiveAndRewriteSortValuesFile();
				lastSortValueExecution = valueList.getTimestamp();
			}
		}
	}

	private void writeIdsInLocalWorkFolder() {
		File idsList = new File(new FoldersLocator().getWorkFolder(), "integer-ids.txt");

		if (idsList.exists() && new DateTime(idsList.lastModified()).isAfter(new DateTime().minusHours(15))) {
			//File exist and is valid
			return;
		}

		List<String> lines = loadRecordIds().stream().map(RecordId::stringValue).collect(toList());

		try {
			FileUtils.writeLines(idsList, lines);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	List<RecordId> loadRecordIds() {
		List<RecordId> recordIds = IteratorUtils.toList(searchServices.recordsIdIteratorExceptEvents());
		recordIds = recordIds.stream().filter(RecordId::isInteger).collect(toList());

		//Since the system is running, removing recent ids (more if distributed)
		if (!recordIds.isEmpty()) {
			int last = recordIds.get(recordIds.size() - 1).intValue();
			int removeHigherThan = last - (modelLayerFactory.getDataLayerFactory().isDistributed() ? 3000 : 1000);
			while (!recordIds.isEmpty() && recordIds.get(recordIds.size() - 1).intValue() > removeHigherThan) {
				recordIds.remove(recordIds.size() - 1);
			}
		}
		return recordIds;
	}

}
