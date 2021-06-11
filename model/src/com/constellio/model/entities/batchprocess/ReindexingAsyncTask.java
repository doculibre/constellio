package com.constellio.model.entities.batchprocess;

import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.records.reindexing.ReindexationMode;
import com.constellio.model.services.records.reindexing.ReindexationParams;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import lombok.AllArgsConstructor;

import static org.apache.ignite.internal.util.lang.GridFunc.asList;

@AllArgsConstructor
public class ReindexingAsyncTask implements AsyncTask {

	static int nbExecs;

	String collection;
	String id;

	@Override
	public void execute(AsyncTaskExecutionParams params) throws ValidationException, Exception {
		System.out.println("Executing ReindexingAsyncTask #" + (++nbExecs) + " : " + id);

		params.getModelLayerFactory().getBatchProcessesManager().cancelPendingBatchProcesses(
				batchProcess -> batchProcess.getTitle().equals(params.getBatchProcess().getTitle()));

		ReindexingServices reindexingServices = new ReindexingServices(params.getModelLayerFactory());

		ReindexationParams reindexationParams = new ReindexationParams(ReindexationMode.RECALCULATE)
				.setRepopulate(false)
				.setMultithreading(false)
				.setLimitToHierarchyOf(asList(RecordId.id(id)));

		reindexingServices.reindexCollection(collection, reindexationParams);


	}

	@Override
	public Object[] getInstanceParameters() {
		return new Object[]{collection, id};
	}
}
