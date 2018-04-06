package com.constellio.app.utils.scripts;

import java.io.File;
import java.io.IOException;

import com.constellio.data.dao.managers.config.ZooKeeperConfigManager;
import com.constellio.data.events.EventBus;
import com.constellio.data.events.EventBusListener;
import com.constellio.data.io.services.facades.IOServices;

public class ZookeeperBackup {

	public static void main(String argv[])
			throws Exception {
		if (!validateArgs(argv)) {
			usage();
			return;
		}
		File importDir = new File(argv[1]);
		if (!importDir.exists()) {
			System.err.println("importDir: " + importDir + " does not exists");
		}

		ZookeeperBackup zb = new ZookeeperBackup();
		if ("--import".equals(argv[0])) {
			zb.importOption(importDir, argv[2]);
		} else if ("--export".equals(argv[0])) {
			zb.exportOption(importDir, argv[2]);
		}
	}

	private EventBus fakeEventBus() {
		return new EventBus(null, null, null) {
			@Override
			public void send(String type, Object data) {
				//Do nothing
			}

			@Override
			public void register(EventBusListener listener) {
				//Do nothing
			}
		};
	}

	public void importOption(File localDir, String zkAddress)
			throws Exception {
		System.out.println("Connecting to : " + zkAddress);
		ZooKeeperConfigManager configManager = new ZooKeeperConfigManager(zkAddress, "/", new IOServices(null), fakeEventBus());
		if (localDir.listFiles().length == 0) {
			throw new IOException("localDir: " + localDir + " is empty");
		}
		configManager.importFrom(localDir);
		System.out.println("Import to Zookeeper completed");
	}

	public void exportOption(File localDir, String zkAddress)
			throws Exception {
		File tempFolder = File.createTempFile("temp_", Long.toString(System.nanoTime()));
		System.out.println("Connecting to : " + zkAddress);
		ZooKeeperConfigManager configManager = new ZooKeeperConfigManager(zkAddress, "/", new IOServices(null), fakeEventBus());
		if (localDir.listFiles().length != 0) {
			throw new IOException("localDir: " + localDir + " is NOT empty");
		}
		configManager.exportTo(localDir);
		System.out.println("Export to " + localDir + " + completed");
	}

	//	public void purge

	private static boolean validateArgs(String argv[])
			throws Exception {
		if (argv.length != 3) {
			return false;
		}
		if (!argv[0].equals("--import") && !argv[0].equals("--export")) {
			return false;
		}
		return true;
	}

	private static void usage() {
		System.out.println("Usage: ZookeeperBackup OPTIONS <zkAddress>");
		System.out.println("OPTIONS");
		System.out.println(" --import <localDir>");
		System.out.println("         Imports <localDir> in ZK node /constellio/conf, if empty");
		System.out.println(" --export <localDir>");
		System.out.println("         Exports ZK node /constellio/conf to <localDir>, if empty");
	}
}
