package com.constellio.data.threads;

import com.constellio.data.services.tenant.TenantService;

import java.util.concurrent.Semaphore;

public class BackgroundActionExecutionManager {

	private static TenantService tenantService = TenantService.getInstance();
	private static int PERMITS = 14; // TODO config
	private static Semaphore executionSemaphore = new Semaphore(PERMITS);

	public static void execute(Runnable task) {
		execute(task, 1);
	}

	public static void execute(Runnable task, int permitsRequired) {
		if (tenantService.isSupportingTenants()) {
			executeWithPermission(task, permitsRequired);
		} else {
			task.run();
		}
	}

	private static void executeWithPermission(Runnable task, int permitsRequired) {
		try {
			executionSemaphore.acquire(permitsRequired);
			task.run();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			executionSemaphore.release(permitsRequired);
		}
	}

}
