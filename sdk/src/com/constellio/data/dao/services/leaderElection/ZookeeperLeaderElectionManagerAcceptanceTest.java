package com.constellio.data.dao.services.leaderElection;

import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Confirm @SlowTest
public class ZookeeperLeaderElectionManagerAcceptanceTest extends ConstellioTest {

	TestingServer zkTestServer;
	CuratorFramework clientA;
	CuratorFramework clientB;

	ZookeeperLeaderElectionManager electionManagerA;

	ZookeeperLeaderElectionManager electionManagerB;

	@Before
	public void setUp()
			throws Exception {
		assumeZookeeperConfigs();
		zkTestServer = new TestingServer(2189);
		zkTestServer.start();
		clientA = CuratorFrameworkFactory.newClient(zkTestServer.getConnectString(), new RetryOneTime(2000));
		clientA.start();
		clientA.blockUntilConnected();
		clientB = CuratorFrameworkFactory.newClient(zkTestServer.getConnectString(), new RetryOneTime(2000));
		clientB.start();
		clientB.blockUntilConnected();

		DataLayerFactory dataLayerFactory = mock(DataLayerFactory.class);
		when(dataLayerFactory.getCuratorFramework()).thenReturn(clientA);
		electionManagerA = new ZookeeperLeaderElectionManager(getDataLayerFactory());
		when(dataLayerFactory.getCuratorFramework()).thenReturn(clientB);
		electionManagerB = new ZookeeperLeaderElectionManager(getDataLayerFactory());
	}

	@Test
	public void givenLeaderWhenGoesDownThenReplaced()
			throws InterruptedException {
		electionManagerA.initialize("/unitTest/constellio/leader");
		Thread.sleep(1000);
		assertThat(electionManagerA.isCurrentNodeLeader()).isTrue();
		electionManagerB.initialize("/unitTest/constellio/leader");
		Thread.sleep(5000);
		assertThat(electionManagerA.isCurrentNodeLeader()).isTrue();
		assertThat(electionManagerB.isCurrentNodeLeader()).isFalse();
		electionManagerA.close();
		;
		Thread.sleep(5000);
		assertThat(electionManagerA.isCurrentNodeLeader()).isFalse();
		assertThat(electionManagerB.isCurrentNodeLeader()).isTrue();
	}

	@After
	public void tearDown()
			throws Exception {
		CloseableUtils.closeQuietly(clientA);
		CloseableUtils.closeQuietly(clientB);
		CloseableUtils.closeQuietly(zkTestServer);
	}
}
