package com.constellio.data.dao.services.solr.serverFactories;

import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Arrays;

import org.apache.solr.client.solrj.SolrClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;

@RunWith(value = Parameterized.class)
public class SolrServerFactoryTest {
	public static final int TIMES = 3;
	public static String CORE_1 = "core1";
	public static String CORE_2 = "core2";
	public static String CORE_ADMIN = "";

	@Parameters(name = "{index}: Test {0}")
	public static Iterable<Object[]> setUpParameters() throws Exception {
		IOServicesFactory ioServicesFactory = Mockito.mock(IOServicesFactory.class);
		Object[][] params = new Object[][]{new Object[]{new HttpSolrServerFactory("http://localhost/", ioServicesFactory)}, new Object[]{new CloudSolrServerFactory("http://zkhost.com")}};
		return Arrays.asList(params); 
	}

	private AbstractSolrServerFactory solrServerFactoryUnderTest;
	private @Mock SolrClient solrClient1;
	private @Mock SolrClient solrClient2;
	private @Mock SolrClient solrClientAdmin;

	private @Mock AtomicFileSystem atomicFileSystem1;
	private @Mock AtomicFileSystem atomicFileSystem2;
	private @Mock AtomicFileSystem atomicFileSystemAdmin;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		String[] cores = new String[]{CORE_1, CORE_2, CORE_ADMIN};
		SolrClient[] solrClients = new SolrClient[]{solrClient1, solrClient2, solrClientAdmin};
		AtomicFileSystem[] atomicFileSystems = new AtomicFileSystem[]{atomicFileSystem1, atomicFileSystem2, atomicFileSystemAdmin};

		for (int i = 0; i < cores.length; i++){
			willReturn(solrClients[i]).given(solrServerFactoryUnderTest).getSolrClient(cores[i]);
			willReturn(atomicFileSystems[i]).given(solrServerFactoryUnderTest).getAtomicFileSystem(cores[i]);
		}

		for (int i = 0; i < TIMES; i++){
			solrServerFactoryUnderTest.getAdminServer();
			solrServerFactoryUnderTest.getConfigFileSystem();
			for (String core: cores){
				if (core.isEmpty()) //it is admin not an ordinary core.
					continue;
				solrServerFactoryUnderTest.newSolrServer(core);
				solrServerFactoryUnderTest.getConfigFileSystem(core);
			}
		}
	}

	public SolrServerFactoryTest(AbstractSolrServerFactory solrServerFactory) {
		solrServerFactoryUnderTest = spy(solrServerFactory);
	}


	@Test
	public void whenClearingSolrServerFactoryThenAllSolrClientAndAtomicFileSystemAreClosed() throws IOException {
		solrServerFactoryUnderTest.clear();

		verify(atomicFileSystem1, times(TIMES)).close();
		verify(atomicFileSystem2, times(TIMES)).close();
		verify(atomicFileSystemAdmin, times(TIMES)).close();

		verify(solrClient1, times(TIMES)).close();
		verify(solrClient2, times(TIMES)).close();
		verify(solrClientAdmin, times(TIMES)).close();
	}

}
