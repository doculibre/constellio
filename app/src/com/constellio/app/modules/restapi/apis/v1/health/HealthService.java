package com.constellio.app.modules.restapi.apis.v1.health;

import com.constellio.app.modules.restapi.apis.v1.core.BaseDao;
import com.constellio.app.modules.restapi.apis.v1.core.BaseService;
import com.constellio.app.modules.restapi.apis.v1.health.dao.HealthDao;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HealthService extends BaseService {

	@Inject
	private HealthDao healthDao;

	private Map<String, Boolean> cache;
	private ScheduledExecutorService executorService;

	private static final String CONSTELLIO_KEY = "Constellio";
	private static final long DELAY = 10000L;

	@PostConstruct
	public void init() {
		cache = new HashMap<>();
		cache.put(CONSTELLIO_KEY, true);

		Runnable runnableTask = () -> {
			try {
				boolean healthy = healthDao.isConstellioHealthy();
				cache.put(CONSTELLIO_KEY, healthy);
				log.trace("Health status updated in cache");
			} catch (Exception e) {
				e.printStackTrace();
			}
		};

		executorService = Executors.newSingleThreadScheduledExecutor();
		executorService.scheduleWithFixedDelay(runnableTask, 0, DELAY, TimeUnit.MILLISECONDS);
	}

	@PreDestroy
	public void cleanup() {
		try {
			executorService.shutdownNow();
		} catch (Exception ignored) {
		}
	}

	@Override
	protected BaseDao getDao() {
		return healthDao;
	}

	public boolean isConstellioHealthy() {
		return cache.get(CONSTELLIO_KEY);
	}
}
