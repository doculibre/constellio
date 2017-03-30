package com.constellio.app.modules.complementary.esRmRobots.services;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.request.CollectionAdminRequest.Restore;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.core.snapshots.SolrSnapshotsTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class SolrCollectionsBackupRestoreManagementTool extends SolrSnapshotsTool {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final CloudSolrClient solrClient;

	public SolrCollectionsBackupRestoreManagementTool(String solrZkEnsemble) {
		super(solrZkEnsemble);

		solrClient = (new CloudSolrClient.Builder()).withZkHost(solrZkEnsemble).build();
	}

	public void restoreSnapshot(String collectionName, String snapshotName, String directory, Optional<String> repositoryName) {
		try {
			CollectionAdminRequest.Restore restore = new Restore(collectionName, snapshotName);
			restore.setLocation(directory);

			if (repositoryName.isPresent()) {
				restore.setRepositoryName(repositoryName.get());
			}

			CollectionAdminResponse resp = restore.process(solrClient);
			Preconditions.checkState(resp.getStatus() == 0, "The request failed. The status code is " + resp.getStatus());
		} catch (Exception e) {
			log.error("Failed to restore collection meta-data for collection " + collectionName, e);
			System.out.println("Failed to restore collection meta-data for collection " + collectionName
					+ " due to following error : " + e.getLocalizedMessage());
		}
	}

	@Override
	public void close()
			throws IOException {
		super.close();

		solrClient.close();
	}
}
