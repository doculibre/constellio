package com.constellio.app.modules.complementary.esRmRobots.services;

import static org.junit.Assert.*;

import com.constellio.sdk.tests.annotations.UiTest;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;

@InDevelopmentTest
@UiTest
public class SolrCollectionsBackupRestoreManagementToolAcceptanceTest extends ConstellioTest {

	private final String SOLR_CLOUD_HOST = "192.168.0.103:2181";

	// private RMTestRecords records = new RMTestRecords(zeCollection);
	private SolrCollectionsBackupRestoreManagementTool tool;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioESModule().withRobotsModule().withAllTestUsers());
		tool = new SolrCollectionsBackupRestoreManagementTool(SOLR_CLOUD_HOST);
	}

	@Test
	public void testCreateSnapshot() {
		tool.createSnapshot("records", zeCollection + System.currentTimeMillis());
	}

	@Test
	public void testRestoreSnapshot() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteSnapshot() {
		fail("Not yet implemented");
	}

	@Test
	public void testListSnapshots() {
		fail("Not yet implemented");
	}

	@Test
	public void testExportSnapshot() {
		fail("Not yet implemented");
	}

}
