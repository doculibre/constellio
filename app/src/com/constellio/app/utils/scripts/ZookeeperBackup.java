package com.constellio.app.utils.scripts;

import com.constellio.data.dao.managers.config.ZooKeeperConfigManager;
import com.constellio.data.io.services.facades.IOServices;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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

	public void importOption(File localDir, String zkAddress) throws Exception {
		System.out.println("Connecting to : " + zkAddress);
		ZooKeeperConfigManager configManager = new ZooKeeperConfigManager(zkAddress, "/", new IOServices(null));
		if (localDir.listFiles().length == 0) {
			throw new IOException("localDir: " + localDir + " is empty");
		}
		configManager.importFrom(localDir);
		System.out.println("Import to Zookeeper completed");
	}

	public void exportOption(File localDir, String zkAddress) throws Exception {
		File tempFolder = File.createTempFile("temp_", Long.toString(System.nanoTime()));
		System.out.println("Connecting to : " + zkAddress);
		ZooKeeperConfigManager configManager = new ZooKeeperConfigManager(zkAddress, "/", new IOServices(null));
		if (localDir.listFiles().length != 0) {
			throw new IOException("localDir: " + localDir + " is NOT empty");
		}
		configManager.exportTo(localDir);
		System.out.println("Export to " + localDir + " + completed");
	}
	
//	public void purge

	private static boolean validateArgs(String argv[]) throws Exception {
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
