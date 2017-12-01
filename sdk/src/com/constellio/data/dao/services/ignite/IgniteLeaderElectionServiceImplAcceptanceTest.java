package com.constellio.data.dao.services.ignite;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.IgniteTest;

@IgniteTest
public class IgniteLeaderElectionServiceImplAcceptanceTest extends ConstellioTest {
	private DefaultLeaderElectionServiceImpl service;
	private Ignite server;

	@Before
	public void setUp()
			throws Exception {

		service = new DefaultLeaderElectionServiceImpl(getDataLayerFactory());
		server = Ignition.start(createConfiguration("server", false));
	}

	protected IgniteConfiguration createConfiguration(String instanceName, boolean clientMode) {
		IgniteConfiguration ic = new IgniteConfiguration();
		ic.setIgniteInstanceName(instanceName);
		ic.setClientMode(clientMode);
		return ic;
	}

	@After
	public void tearDown()
			throws Exception {
		server.close();
	}

	@Test
	public void givenUniqueIgniteClientWhenValidatingThenTrue() {
		try (Ignite client = Ignition.start(createConfiguration("client", true))) {
			boolean isLeader = service.isCurrentNodeLeader(client);

			assertThat(isLeader).isTrue();
		}
	}

	@Test
	public void givenMultipleIgniteClientsWhenValidatingThenFirstIsTrueAndOthersAreFalse() {
		Ignite client1 = null, client2 = null, client3 = null, client4 = null;

		try {
			client1 = Ignition.start(createConfiguration("client1", true));
			client2 = Ignition.start(createConfiguration("client2", true));
			client3 = Ignition.start(createConfiguration("client3", true));
			client4 = Ignition.start(createConfiguration("client4", true));

			assertThat(service.isCurrentNodeLeader(client1)).isTrue();
			assertThat(service.isCurrentNodeLeader(client2)).isFalse();
			assertThat(service.isCurrentNodeLeader(client3)).isFalse();
			assertThat(service.isCurrentNodeLeader(client4)).isFalse();

			closeQuitely(client1);

			assertThat(service.isCurrentNodeLeader(client2)).isTrue();
			assertThat(service.isCurrentNodeLeader(client3)).isFalse();
			assertThat(service.isCurrentNodeLeader(client4)).isFalse();

			closeQuitely(client2);

			assertThat(service.isCurrentNodeLeader(client3)).isTrue();
			assertThat(service.isCurrentNodeLeader(client4)).isFalse();

			closeQuitely(client3);

			assertThat(service.isCurrentNodeLeader(client4)).isTrue();

			closeQuitely(client4);
		} catch (IgniteException e) {
			closeQuitely(client1);
			closeQuitely(client2);
			closeQuitely(client3);
			closeQuitely(client4);

			throw e;
		}
	}

	protected void closeQuitely(AutoCloseable closeable) {
		try {
			closeable.close();
		} catch (Exception e) {
		}
	}
}
