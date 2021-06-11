package com.constellio.app.modules.restapi.apis.v1.health.dao;

import com.constellio.app.modules.restapi.apis.v1.core.BaseDao;
import com.constellio.app.services.ping.PingServices;

import javax.annotation.PostConstruct;

public class HealthDao extends BaseDao {

	private PingServices pingServices;

	@PostConstruct
	protected void init() {
		super.init();
		pingServices = new PingServices(appLayerFactory);
	}

	public boolean isConstellioHealthy() {
		try {
			return pingServices.testZookeeperAndSolr();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
