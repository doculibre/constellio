package com.constellio.data.backup;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.CollectionAdminRequest.Delete;
import org.apache.solr.client.solrj.request.CollectionAdminRequest.Restore;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.common.params.CollectionAdminParams;

public class SolrCollectionsBackupRestoreManagementTool implements Closeable {
	private final CloudSolrClient solrClient;

	public SolrCollectionsBackupRestoreManagementTool(String solrZkEnsemble) {
		solrClient = (new CloudSolrClient.Builder()).withZkHost(solrZkEnsemble).build();
	}

	public CollectionAdminResponse createSnapshot(String collectionName, String snapshotName)
			throws SolrServerException, IOException {
		return new CollectionAdminRequest.CreateSnapshot(collectionName, snapshotName).process(solrClient);
	}

	public CollectionAdminResponse deleteSnapshot(String collectionName, String snapshotName)
			throws SolrServerException, IOException {
		return new CollectionAdminRequest.DeleteSnapshot(collectionName, snapshotName).process(solrClient);
	}

	public CollectionAdminResponse listSnapshots(String collectionName)
			throws SolrServerException, IOException {
		return new CollectionAdminRequest.ListSnapshots(collectionName).process(solrClient);
	}

	public CollectionAdminResponse backupCollectionMetaData(String collectionName, String snapshotName, String backupLoc)
			throws SolrServerException, IOException {
		// Backup the collection meta-data
		CollectionAdminRequest.Backup backup = new CollectionAdminRequest.Backup(collectionName, snapshotName);
		backup.setIndexBackupStrategy(CollectionAdminParams.NO_INDEX_BACKUP_STRATEGY);
		backup.setLocation(backupLoc);
		return backup.process(solrClient);
	}

	@SuppressWarnings("deprecation")
	public CollectionAdminResponse exportSnapshot(String collectionName, String snapshotName, String destPath,
			Optional<String> repositoryName, Optional<String> asyncReqId)
			throws SolrServerException, IOException {
		CollectionAdminRequest.Backup backup = new CollectionAdminRequest.Backup(collectionName, snapshotName);
		backup.setIndexBackupStrategy(CollectionAdminParams.COPY_FILES_STRATEGY);
		backup.setLocation(destPath);
		if (repositoryName.isPresent()) {
			backup.setRepositoryName(repositoryName.get());
		}
		if (asyncReqId.isPresent()) {
			backup.setAsyncId(asyncReqId.get());
		}

		return backup.process(solrClient);
	}

	public CollectionAdminResponse exportSnapshot(String collectionName, String snapshotName, String destPath,
			Optional<String> repositoryName)
			throws SolrServerException, IOException {
		return exportSnapshot(collectionName, snapshotName, destPath, repositoryName, Optional.<String> ofNullable(null));
	}

	public CollectionAdminResponse exportSnapshot(String collectionName, String snapshotName, String destPath)
			throws SolrServerException, IOException {
		return exportSnapshot(collectionName, snapshotName, destPath, Optional.<String> ofNullable(null));
	}

	public CollectionAdminResponse restoreSnapshot(String collectionName, String snapshotName, String directory,
			Optional<String> repositoryName)
			throws SolrServerException, IOException {
		CollectionAdminRequest.Restore restore = new Restore(collectionName, snapshotName);
		restore.setLocation(directory);

		if (repositoryName.isPresent()) {
			restore.setRepositoryName(repositoryName.get());
		}

		return restore.process(solrClient);
	}

	public CollectionAdminResponse restoreSnapshot(String collectionName, String snapshotName, String directory)
			throws SolrServerException, IOException {
		return restoreSnapshot(collectionName, snapshotName, directory, Optional.<String> ofNullable(null));
	}
	
	public CollectionAdminResponse deleteCollection(String collectionName)
			throws SolrServerException, IOException {
		Delete delete = CollectionAdminRequest.deleteCollection(collectionName);
		return delete.process(solrClient);
	}

	@Override
	public void close()
			throws IOException {
		solrClient.close();
	}
}
