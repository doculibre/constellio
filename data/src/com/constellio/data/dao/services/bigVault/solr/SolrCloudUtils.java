package com.constellio.data.dao.services.bigVault.solr;

import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;

import java.util.Map;

@Slf4j
public class SolrCloudUtils {

	public static boolean isOnline(SolrClient solrClient) {
		boolean online = true;
		try {
			final NamedList<Object> response = solrClient.request(new CollectionAdminRequest.ClusterStatus());
			final NamedList<Object> cluster = (NamedList<Object>) response.get("cluster");

			final SimpleOrderedMap collectionFromCluster = (SimpleOrderedMap) cluster.get("collections");

			Map<String, Map> settingByCollection = collectionFromCluster.asShallowMap();
			for (String collection : settingByCollection.keySet()) {
				Map<String, Map> collectionSettingsMapByName = settingByCollection.get(collection);

				Map<String, Map> shardsByName = collectionSettingsMapByName.get("shards");

				for (String shardName : shardsByName.keySet()) {
					Map<String, Map> shardSettingByName = shardsByName.get(shardName);

					Map<String, Map> replicasByName = ((Map<String, Map>) shardSettingByName.get("replicas"));
					for (String replicas : replicasByName.keySet()) {
						Map<String, String> replicasState = replicasByName.get(replicas);
						if (!replicasState.get("state").equals("active")) {
							online = false;
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Failed to check solr cloud status", e);
			online = false;
		}

		return online;
	}

}
