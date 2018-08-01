package com.constellio.app.utils.scripts;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.common.util.NamedList;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SolrSnapshotTool {

	private static final String[] COLLECTIONS = new String[]{"records", "events", "notifications"};

	public static void main(String[] argv)
			throws Exception {

		if (!validateArgs(argv)) {
			usage();
			return;
		}

		Map<String, NamedList> replicasNamedList = new HashMap<>();
		for (String collection : COLLECTIONS) {
			File snapshotDir = new File(argv[0]);
			File snapshotForCollection = new File(snapshotDir, collection + ".xml");

			NamedList responseNamedList = new XMLResponseParser().processResponse(new StringReader(FileUtils.readFileToString(snapshotForCollection, StandardCharsets.UTF_8)));
			Object snapshots = responseNamedList.get("snapshots");
			if (snapshots != null) {
				NamedList snapshotsNamedList = (NamedList) snapshots;
				Object snapshot = snapshotsNamedList.get(argv[1]);
				if (snapshot != null) {
					NamedList snapshotNamedList = (NamedList) snapshot;
					Object replicas = snapshotNamedList.get("replicas");
					if (replicas != null) {
						replicasNamedList.put(collection, (NamedList) replicas);
					}
				}
			}

		}

		if (replicasNamedList == null) {
			throw new RuntimeException("No Solr replicas found");
		}

		StringBuilder builder = new StringBuilder();
		builder.append("sudo systemctl stop solr");

		SolrClient client = new HttpSolrClient.Builder().withBaseSolrUrl("http://localhost:8983/solr").build();

		CoreAdminResponse coreAdminResponse = CoreAdminRequest.getStatus(null, client);
		NamedList<NamedList<Object>> coreStatusNamedList = coreAdminResponse.getCoreStatus();
		for (Map.Entry<String, NamedList<Object>> coreStatus : coreStatusNamedList) {
			String coreName = coreStatus.getKey();
			String collectionName = StringUtils.substringBefore(coreName, "_shard");

			Object coreSnapshot = replicasNamedList.get(collectionName).get(coreName);
			if (coreSnapshot != null) {
				NamedList coreSnapshotNamedList = (NamedList) coreSnapshot;
				int indexDirPathIndex = coreSnapshotNamedList.indexOf("indexDirPath", 0);
				String indexDirPath = coreSnapshotNamedList.getVal(indexDirPathIndex).toString();

				int filesIndex = coreSnapshotNamedList.indexOf("files", 0);
				List<String> snapshotIndexFiles = (List) coreSnapshotNamedList.getVal(filesIndex);

				File indexDir = new File(indexDirPath);
				if (!indexDir.exists()) {
					throw new RuntimeException("indexDir missing : " + indexDir.getAbsolutePath());
				}

				for (String snapshotIndexFile : snapshotIndexFiles) {
					File file = new File(indexDir, snapshotIndexFile);
					if (!file.exists()) {
						throw new IOException("Index file missing : \"" + file.getAbsolutePath() + "\" Check if index folder was renamed in index.properties on Solr Core.");
					}
				}

				File tlog = new File(indexDir.getParent(), "tlog");

				if (!snapshotIndexFiles.isEmpty()) {
					builder.append("\nsudo rm -rf \"" + tlog.getAbsolutePath() + "\"");
					builder.append("\ncd \"" + indexDirPath + "\"");
					builder.append("\nsudo rm -r `ls | grep -v \"" + StringUtils.join(snapshotIndexFiles, "\\|") + "\"`");
				}
			}
		}
		FileUtils.write(new File(argv[2]), builder.toString(), "UTF-8");
	}

	private static boolean validateArgs(String argv[]) throws Exception {
		if (argv.length != 3) {
			return false;
		}
		File snapshotDir = new File(argv[0]);
		if (!snapshotDir.exists()) {
			System.err.println("<snapshotDir> : " + snapshotDir.getAbsolutePath() + " does not exists");
			return false;
		} else if (!snapshotDir.isDirectory()) {
			System.err.println("<snapshotDir> : " + snapshotDir.getAbsolutePath() + " is not a directory");
			return false;
		}
		for (String collection : COLLECTIONS) {
			File snapshotForCollection = new File(snapshotDir, collection + ".xml");
			if (!snapshotForCollection.exists()) {
				System.err.println("Missing " + collection + ".xml snapshot in <snapshotDir> : " + snapshotDir.getAbsolutePath());
				return false;
			}
		}
		return true;
	}

	private static void usage() {
		System.out.println("Usage: SolrSnapshotTool <snapshotDir> <commitName> <outputFile>");
		System.out.println("  Prepare a list of commands to restore a snapshot. Deletes other snapshots. ***Run this on all nodes before a full collection restore.");
	}
}
