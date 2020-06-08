package com.constellio.app.services.ping;

import com.constellio.app.client.ZookeeperClient;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.conf.PropertiesConfigurationRuntimeException.PropertiesConfigurationRuntimeException_ConfigNotDefined;
import com.constellio.data.conf.SolrServerType;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.jgoodies.common.base.Strings;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PingServices {

	private DataLayerFactory dataLayerFactory;

	public PingServices(AppLayerFactory appLayerFactory) {
		dataLayerFactory = appLayerFactory.getModelLayerFactory().getDataLayerFactory();
	}

	public boolean testZookeeperAndSolr() throws IOException {
		return testZookeeperAndSolr(null, true);
	}

	public boolean testZookeeperAndSolr(PrintWriter pw, boolean online)
			throws IOException {
		online = changeOnlineStatus(online, testZookeeper(pw, online));

		SolrServerType solrServerType = dataLayerFactory.getDataLayerConfiguration().getRecordsDaoSolrServerType();

		if (SolrServerType.HTTP == solrServerType) {
			online = changeOnlineStatus(online, testHttpSolr(pw));
		} else if (SolrServerType.CLOUD == solrServerType) {

			SolrClient solrClient = dataLayerFactory.newRecordDao().getBigVaultServer().getNestedSolrServer();

			online = changeOnlineStatus(online, testSolrCloudNodes(pw, solrClient));
		} else {
			throw new ImpossibleRuntimeException("Unsupported solr server type");
		}

		return online;
	}

	private boolean testZookeeper(PrintWriter pw, boolean online) {
		String recordsDaoCloudSolrServerZkHost = null;


		try {
			recordsDaoCloudSolrServerZkHost = dataLayerFactory.getDataLayerConfiguration()
					.getRecordsDaoCloudSolrServerZKHost();
		} catch (PropertiesConfigurationRuntimeException_ConfigNotDefined e) {

		}

		List<String> zooKeeperAddr = null;

		if (Strings.isNotBlank(recordsDaoCloudSolrServerZkHost)) {
			zooKeeperAddr = Arrays.asList(recordsDaoCloudSolrServerZkHost.split(","));
		}

		if (zooKeeperAddr != null) {
			for (String addr : zooKeeperAddr) {
				int port = getPort(addr);
				String domain = getDomain(addr);
				ZookeeperClient zookeeperClient = new ZookeeperClient(domain, port);
				try {
					String stat = zookeeperClient.stat();
					if (stat.contains("Mode: leader") || stat.contains("Mode: follower") ||
						(zooKeeperAddr.size() == 1 && stat.contains("Mode: standalone"))) {
						online = changeOnlineStatus(online, true);
						if (pw != null) {
							pw.append("ZooKeeper : " + domain + ":" + port + " is up and running");
							pw.append("\n");
						}
					} else {
						if (pw != null) {
							pw.append("ZooKeeper : " + domain + ":" + port + " is down");
							online = changeOnlineStatus(online, false);
							pw.append("\n");
						}
					}
				} catch (Exception e) {
					if (pw != null) {
						pw.append("ZooKeeper : " + domain + ":" + port + " is not responding");
						online = changeOnlineStatus(online, false);
						pw.append("\n");
					}
				}
			}
		}
		return online;
	}

	private boolean testSolrCloudNodes(PrintWriter pw, SolrClient solrClient)
			throws IOException {
		boolean online = true;
		try {
			//CollectionAdminRequest.ClusterStatus
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
							if (pw != null) {
								pw.append("core : " + replicasState.get("core") + " baseUrl : " + replicasState.get("base_url") + " Status : " + replicasState.get("state") + ".");
								pw.append("\n");
								online = false;
							}
						}
					}
				}
			}
		} catch (SolrServerException e) {
			online = false;
			if (pw != null) {
				pw.append("Error : the request to get solr server status failed. Exception message : " + e.getMessage());
				pw.append("\n");
			}
		}

		if (online) {
			if (pw != null) {
				pw.append("Solr cluster is up and runnng.");
				pw.append("\n");
			}
		}

		return online;
	}


	private boolean changeOnlineStatus(boolean currentVal, boolean isOnline) {
		if (!currentVal) {
			return false;
		} else {
			return isOnline;
		}
	}

	private boolean testHttpSolr(PrintWriter pw) {
		boolean online = true;
		try {
			dataLayerFactory.newRecordDao().documentsCount();

		} catch (Exception e) {
			if (pw != null) {
				pw.append("Status : offline (Solr error)");
				pw.append("\n");
			}
			online = false;
			e.printStackTrace();
		}
		return online;
	}

	private int getPort(String addrWithPort) {
		int beginPortIndex = addrWithPort.lastIndexOf(":");

		return Integer.parseInt(addrWithPort.substring(beginPortIndex + 1, addrWithPort.length()));
	}

	private String getDomain(String addrWithPort) {
		int beginPortIndex = addrWithPort.lastIndexOf(":");


		return addrWithPort.substring(0, beginPortIndex);
	}

}
