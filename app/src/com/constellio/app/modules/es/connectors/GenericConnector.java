package com.constellio.app.modules.es.connectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;

import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.connectors.spi.DefaultAbstractConnector;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;

public abstract class GenericConnector extends DefaultAbstractConnector {
	private static final Logger LOGGER = LogManager.getLogger(GenericConnector.class);

	private GenericConnectorServices connectorServices;
	private GenericConnectorInstance connectorInstance;
	private int maxJobPerBatch;
	private int documentsPerJob;
	private Map<String, GenericConnectorContext> contextsMappedByDocumentType;

	@Override
	public List<ConnectorJob> getJobs() {
		if (isFetchStarting()) {
			initFetch();
		} else if (isFetchEnded()) {
			prepareNextStartStart();
			return new ArrayList<>();
		}
		List<ConnectorJob> nextJobs = nextJobs();
		if (nextJobs.isEmpty()) {
			prepareNextStartStart();
		}
		return nextJobs;
	}

	private List<ConnectorJob> nextJobs() {
		List<ConnectorJob> returnJobs = new ArrayList<>();
		int i = 0;
		//high priority to jobs to delete
		boolean moreJobs = true;
		while (i < maxJobPerBatch && moreJobs) {
			ConnectorJob addJob = createRemoveJob();
			if (addJob != null) {
				i++;
				returnJobs.add(addJob);
			} else {
				moreJobs = false;
			}
		}
		//priority to new jobs
		moreJobs = true;
		while (i < maxJobPerBatch && moreJobs) {
			ConnectorJob addJob = createAddJob();
			if (addJob != null) {
				i++;
				returnJobs.add(addJob);
			} else {
				moreJobs = false;
			}
		}
		//low priority to jobs to update
		moreJobs = true;
		while (i < maxJobPerBatch && moreJobs) {
			ConnectorJob updateJob = createUpdateJob();
			if (updateJob != null) {
				i++;
				returnJobs.add(updateJob);
			} else {
				moreJobs = false;
			}
		}
		return returnJobs;
	}

	private void prepareNextStartStart() {
		for (GenericConnectorContext context : contextsMappedByDocumentType.values()) {
			context.prepareNextStartStart();
		}
	}

	private boolean isFetchStarting() {
		GenericConnectorContext anyContext = contextsMappedByDocumentType.values().iterator().next();
		return anyContext.isFetchStarting();
	}

	void initFetch() {
		maxJobPerBatch = connectorInstance.getNumberOfJobsInParallel();
		documentsPerJob = connectorInstance.getDocumentsPerJobs();
		if (documentsPerJob == 0) {
			throw new RuntimeException("At least one document per job");
		}
		if (maxJobPerBatch == 0) {
			throw new RuntimeException("At least one job per batch");
		}

		contextsMappedByDocumentType = new HashMap<>();
		for (String documentType : this.getConnectorDocumentTypes()) {
			GenericConnectorContext connectorContext = new GenericConnectorContext(documentType);
			contextsMappedByDocumentType.put(documentType, new GenericConnectorContext(documentType));
			connectorContext.initFetch();
		}
	}

	private boolean isFetchEnded() {
		for (GenericConnectorContext context : contextsMappedByDocumentType.values()) {
			if (!context.isFetchEnded()) {
				return false;
			}
		}
		return true;
	}

	private ConnectorJob createRemoveJob() {
		for (GenericConnectorContext connectorContext : contextsMappedByDocumentType.values()) {
			if (connectorContext.hasDocumentsToDelete()) {
				List<String> documentsToDeleteConstellioIds = connectorContext.getDocumentsToDeleteAndUpdateContext(
						this.documentsPerJob);
				return new ConnectorDeleterJob(this, documentsToDeleteConstellioIds);
			}
		}
		return null;
	}

	private ConnectorJob createUpdateJob() {
		for (Entry<String, GenericConnectorContext> entry : contextsMappedByDocumentType.entrySet()) {
			GenericConnectorContext connectorContext = entry.getValue();
			if (connectorContext.hasNewDocumentsToUpdate()) {
				List<String> documentsToCrawlRemoteIds = connectorContext.getDocumentsToUpdateAndUpdateContext(
						this.documentsPerJob);
				return newConnectorUpdaterJob(this, connectorInstance, documentsToCrawlRemoteIds, entry.getKey());
			}
		}
		return null;
	}

	private ConnectorJob createAddJob() {
		for (Entry<String, GenericConnectorContext> entry : contextsMappedByDocumentType.entrySet()) {
			GenericConnectorContext connectorContext = entry.getValue();
			if (connectorContext.hasNewDocumentsToFetch()) {
				List<String> documentsToCrawlRemoteIds = connectorContext.getNewDocumentsToCrawlAndUpdateContext(
						this.documentsPerJob);
				return newConnectorCrawlerJob(this, connectorInstance, documentsToCrawlRemoteIds, entry.getKey());
			}
		}
		return null;
	}

	@Override
	protected void initialize(Record instance) {

	}

	@Override
	public List<String> getConnectorDocumentTypes() {
		return null;
	}

	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}

	@Override
	public void afterJobs(List<ConnectorJob> jobs) {

	}

	@Override
	public void resume() {

	}

	@Override
	public void onAllDocumentsDeleted() {

	}

	protected abstract ConnectorJob newConnectorUpdaterJob(GenericConnector genericConnector,
			GenericConnectorInstance connectorInstance,
			List<String> documentsToCrawlRemoteIds, String documentType);

	protected abstract ConnectorJob newConnectorCrawlerJob(GenericConnector genericConnector,
			GenericConnectorInstance connectorInstance,
			List<String> documentsToCrawlRemoteIds, String documentType);

	interface GenericConnectorInstance {
		int getNumberOfJobsInParallel();

		int getDocumentsPerJobs();

		Duration getMaxDurationBetweenTraversals();
	}

	interface GenericConnectorServices {
		List<String> getAllRemoteIds(String documentType);

		String getRemoteId(String recordId);
	}

	interface ConstellioDocumentInfo {
		String getConstellioRecordId();

		String getRemoteSystemDocumentId();

		LocalDateTime getLastFetch();
	}

	//TODO save context and read context when required (ex. use solr to save it)
	class GenericConnectorContext {
		LocalDateTime traversalStart;
		String documentType;
		private List<String> allNewObjectsToFetch;
		private List<String> allObjectsToUpdate;
		private List<String> allObjectsToRemoveConstellioIds;

		public GenericConnectorContext(String objectType) {
			this.documentType = documentType;
		}

		private boolean isFetchEnded() {
			if (this.allNewObjectsToFetch.isEmpty()
					&& this.allObjectsToUpdate.isEmpty()
					&& this.allObjectsToRemoveConstellioIds.isEmpty()) {
				return true;
			} else {
				return false;
			}
		}

		private boolean isFetchStarting() {
			if (this.allNewObjectsToFetch == null) {
				return true;
			} else {
				return false;
			}
		}

		private void prepareNextStartStart() {
			this.allNewObjectsToFetch = null;
		}

		void initFetch() {
			traversalStart = TimeProvider.getLocalDateTime();
			allObjectsToUpdate = new ArrayList<>();
			this.allNewObjectsToFetch = new ArrayList<>();
			this.allObjectsToRemoveConstellioIds = new ArrayList<>();
			List<String> allRemoteObjectsIds = new ArrayList<>();
			List<String> remoteIdsInConstellio = new ArrayList<>();
			allRemoteObjectsIds.addAll(connectorServices.getAllRemoteIds(this.documentType));

			List<ConstellioDocumentInfo> existingConnectorInstanceDocuments = getAllConnectorInstanceDocumentOfType(
					connectorInstance, this.documentType);
			for (ConstellioDocumentInfo documentInfo : existingConnectorInstanceDocuments) {
				String remoteId = documentInfo.getRemoteSystemDocumentId();
				remoteIdsInConstellio.add(remoteId);
				if (!allRemoteObjectsIds.contains(remoteId)) {
					this.allObjectsToRemoveConstellioIds.add(documentInfo.getConstellioRecordId());
				} else {
					LocalDateTime documentExpirationTime = documentInfo.getLastFetch()
							.plus(connectorInstance.getMaxDurationBetweenTraversals());
					if (documentExpirationTime.isBefore(traversalStart) || documentExpirationTime.equals(traversalStart)) {
						this.allObjectsToUpdate.add(remoteId);
					}
				}
			}
			this.allNewObjectsToFetch.addAll(CollectionUtils.subtract(allRemoteObjectsIds, remoteIdsInConstellio));
		}

		public boolean hasNewDocumentsToFetch() {
			return !this.allNewObjectsToFetch.isEmpty();
		}

		public List<String> getNewDocumentsToCrawlAndUpdateContext(int documentsPerJob) {
			List<String> newDocumentsRemoteIds = this.allNewObjectsToFetch
					.subList(0, Math.min(documentsPerJob, this.allNewObjectsToFetch.size()));
			if (documentsPerJob < this.allNewObjectsToFetch.size()) {
				this.allNewObjectsToFetch = this.allNewObjectsToFetch.subList(documentsPerJob, this.allNewObjectsToFetch.size());
			} else {
				this.allNewObjectsToFetch = new ArrayList<>();
			}
			return newDocumentsRemoteIds;
		}

		public boolean hasDocumentsToDelete() {
			return !this.allObjectsToRemoveConstellioIds.isEmpty();
		}

		public List<String> getDocumentsToDeleteAndUpdateContext(int documentsPerJob) {
			List<String> documentToDeleteConstellioIds = this.allObjectsToRemoveConstellioIds
					.subList(0, Math.min(documentsPerJob, this.allObjectsToRemoveConstellioIds.size()));
			if (documentsPerJob < this.allObjectsToRemoveConstellioIds.size()) {
				this.allObjectsToRemoveConstellioIds = this.allObjectsToRemoveConstellioIds.subList(documentsPerJob,
						this.allObjectsToRemoveConstellioIds.size());
			} else {
				this.allObjectsToRemoveConstellioIds = new ArrayList<>();
			}
			return documentToDeleteConstellioIds;
		}

		public boolean hasNewDocumentsToUpdate() {
			return !this.allObjectsToUpdate.isEmpty();
		}

		public List<String> getDocumentsToUpdateAndUpdateContext(int documentsPerJob) {
			List<String> documentsToUpdateRemoteIds = this.allObjectsToUpdate
					.subList(0, Math.min(documentsPerJob, this.allObjectsToUpdate.size()));
			if (documentsPerJob < this.allObjectsToUpdate.size()) {
				this.allObjectsToUpdate = this.allObjectsToUpdate.subList(documentsPerJob, this.allObjectsToUpdate.size());
			} else {
				this.allObjectsToUpdate = new ArrayList<>();
			}
			return documentsToUpdateRemoteIds;
		}
	}

	private List<ConstellioDocumentInfo> getAllConnectorInstanceDocumentOfType(GenericConnectorInstance connectorInstance,
			String documentType) {
		//TODO write query svp
		//LogicalSearchQuery query = es.connectorDocumentsToFetchQuery((ConnectorInstance<?>) connectorInstance, documentType);
		//return es.searchConnectorDocuments(query);
		return new ArrayList<>();
	}

}
