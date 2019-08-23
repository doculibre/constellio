package com.constellio.app.servlet;

import com.constellio.app.client.ZookeeperClient;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.conf.PropertiesConfigurationRuntimeException.PropertiesConfigurationRuntimeException_ConfigNotDefined;
import com.constellio.data.conf.SolrServerType;
import com.constellio.data.dao.services.solr.SolrServerFactory;
import com.constellio.data.dao.services.solr.serverFactories.CloudSolrServerFactory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.services.records.reindexing.ReindexingServices;
import com.constellio.model.services.records.reindexing.SystemReindexingInfos;
import com.jgoodies.common.base.Strings;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ConstellioPingServlet extends HttpServlet {

	public static boolean systemRestarting;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		ConstellioFactories constellioFactories = ConstellioFactories.getInstanceIfAlreadyStarted();

		boolean testSolr = !"false".equals(req.getParameter("pingSolr"));


		PrintWriter pw = resp.getWriter();

		boolean online = true;
		if (systemRestarting) {
			pw.append("Constellio status : online (restarting)");
			pw.append("\n");
			online = changeOnlineStatus(online, true);

		} else {
			if (constellioFactories != null) {
				getConstellioStatus(pw);

				if (testSolr) {
					online = changeOnlineStatus(online, testZookeeper(constellioFactories, pw, online));

					SolrServerType solrServerType = constellioFactories.getDataLayerConfiguration().getRecordsDaoSolrServerType();

					if (SolrServerType.HTTP == solrServerType) {
						online = changeOnlineStatus(online, testHttpSolr(constellioFactories, testSolr, pw));
					} else if (SolrServerType.CLOUD == solrServerType) {

						SolrClient solrClient = constellioFactories.getDataLayerFactory().newRecordDao().getBigVaultServer().getNestedSolrServer();

						online = changeOnlineStatus(online, testSolrCloudNodes(pw, solrClient));
					} else {
						throw new ImpossibleRuntimeException("Unsupported solr server type");
					}
				}
			} else {
				pw.append("Constellio status : online (starting)");
				pw.append("\n");
				online = changeOnlineStatus(online, true);
			}
		}

		if (online) {
			pw.append("success");
		} else {
			pw.append("This ping contain error");
		}

		pw.close();
	}

	private boolean testZookeeper(ConstellioFactories constellioFactories, PrintWriter pw, boolean online) {
		String recordsDaoCloudSolrServerZkHost = null;


		try {
			recordsDaoCloudSolrServerZkHost = constellioFactories.getDataLayerConfiguration()
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
					if (stat.contains("Mode: leader") || stat.contains("Mode: follower")) {
						online = changeOnlineStatus(online, true);
						pw.append("ZooKeeper : " + domain + ":" + port + " is up and running");
						pw.append("\n");
					} else {
						pw.append("ZooKeeper : " + domain + ":" + port + " is down");
						online = changeOnlineStatus(online, false);
						pw.append("\n");
					}
				} catch (Exception e) {
					pw.append("ZooKeeper : " + domain + ":" + port + " is not responding");
					online = changeOnlineStatus(online, false);
					pw.append("\n");
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
							pw.append("core : " + replicasState.get("core") + " baseUrl : " + replicasState.get("base_url") + " Status : " + replicasState.get("state") + ".");
							pw.append("\n");
							online = false;
						}
					}
				}
			}
		} catch (SolrServerException e) {
			online = false;
			pw.append("Error : the request to get solr server status failed. Exception message : " + e.getMessage());
			pw.append("\n");
		}

		if (online) {
			pw.append("Solr cluster is up and runnng.");
			pw.append("\n");
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

	private boolean testHttpSolr(ConstellioFactories constellioFactories, boolean testSolr, PrintWriter pw) {
		boolean online = true;
		try {
			if (testSolr) {
				constellioFactories.getDataLayerFactory().newRecordDao().documentsCount();
			}

		} catch (Exception e) {
			pw.append("Status : offline (Solr error)");
			pw.append("\n");
			online = false;
			e.printStackTrace();
		}
		return online;
	}

	private void getConstellioStatus(PrintWriter pw) {
		SystemReindexingInfos reindexingInfos = ReindexingServices.getReindexingInfos();

		if (reindexingInfos != null) {
			pw.append("Constellio status : online (reindexing)");
			pw.append("\n");

		} else {
			pw.append("Constellio status : online (running)");
			pw.append("\n");
		}
	}

	private SolrServerFactory newSolrCloudServerFactory(ConstellioFactories constellioFactories) {
		String zkHost = constellioFactories.getDataLayerConfiguration().getRecordsDaoCloudSolrServerZKHost();
		return new CloudSolrServerFactory(zkHost);
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
