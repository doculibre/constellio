package com.constellio.app.modules.es.connectors.http.fetcher;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.sdk.tests.ConstellioTest;

public class ConnectorUrlAcceptorTest extends ConstellioTest {

	String includePatterns = "";
	String excludePatterns = "";
	@Mock ConnectorHttpInstance connector;
	UrlAcceptor urlAcceptor;

	@Before
	public void setUp()
			throws Exception {
		when(connector.getSeedsList()).thenReturn(asList(
				"http://www.monsite1.com",
				"http://www.monsite2.com/index.html"
		));

		when(connector.getOnDemandsList()).thenReturn(new ArrayList<String>());
		when(connector.getIncludePatterns()).thenReturn(includePatterns);
		when(connector.getExcludePatterns()).thenReturn(excludePatterns);
		urlAcceptor = new ConnectorUrlAcceptor(connector);
	}

	@Test
	public void givenNoIncludeListsWhenAcceptingAnUrlFromSameSeedThenTrue()
			throws Exception {
		assertThat(urlAcceptor.isAccepted("http://www.monsite1.com")).isTrue();
		assertThat(urlAcceptor.isAccepted("http://www.monsite1.com/zePage")).isTrue();
		assertThat(urlAcceptor.isAccepted("https://www.monsite1.com/zePage")).isFalse();
		assertThat(urlAcceptor.isAccepted("http://monsite1.com/zePage")).isFalse();
		
		assertThat(urlAcceptor.isAccepted("http://www.monsite2.com/index.html")).isTrue();
		assertThat(urlAcceptor.isAccepted("http://www.monsite2.com/index.html?queryParam=value")).isTrue();
		assertThat(urlAcceptor.isAccepted("https://www.monsite2.com/index.html")).isFalse();
		assertThat(urlAcceptor.isAccepted("http://www.monsite2.com/zePage")).isFalse();
		assertThat(urlAcceptor.isAccepted("https://www.monsite2.com/zePage")).isFalse();
		assertThat(urlAcceptor.isAccepted("http://monsite2.com/zePage")).isFalse();
	}

	@Test
	public void givenNoIncludeListsWhenAcceptingAnUrlOnAnotherSiteThenFalse()
			throws Exception {

		assertThatSeedsAreTrue();

		assertThat(urlAcceptor.isAccepted("http://www.monsite3.com/zePage")).isFalse();
		assertThat(urlAcceptor.isAccepted("http://monsite3.com/zePage")).isFalse();
		assertThat(urlAcceptor.isAccepted("https://www.monsite3.com/zePage")).isFalse();
		assertThat(urlAcceptor.isAccepted("https://monsite3.com/zePage")).isFalse();
	}

	@Test
	public void givenIncludeListWhenAcceptingUrlMatchingThenTrue()
			throws Exception {

		includePatterns = "http://www.monsite3.com/"
				+ "\nhttp://www.monsite4.com/";
		when(connector.getIncludePatterns()).thenReturn(includePatterns);

		assertThatSeedsAreTrue();

		assertThat(urlAcceptor.isAccepted("http://www.monsite3.com/zePage")).isTrue();
		assertThat(urlAcceptor.isAccepted("http://monsite3.com/zePage")).isFalse();

		assertThat(urlAcceptor.isAccepted("http://www.monsite4.com/zePage")).isTrue();
		assertThat(urlAcceptor.isAccepted("http://monsite4.com/zePage")).isFalse();
	}
	
	@Test
	public void givenNullsWhenAcceptingUrlMatchingThenTrue()
			throws Exception {

		when(connector.getIncludePatterns()).thenReturn(null);
		when(connector.getExcludePatterns()).thenReturn(null);

		assertThatSeedsAreTrue();
	}

	@Test
	public void givenIncludeListWhenAcceptingUrlNotMatchingThenFalse()
			throws Exception {

		includePatterns = "http://www.monsite3.com/"
				+ "\nhttp://www.monsite4.com/";
		when(connector.getIncludePatterns()).thenReturn(includePatterns);

		assertThatSeedsAreTrue();

		assertThat(urlAcceptor.isAccepted("http://www.monsite5.com/zePage")).isFalse();
		assertThat(urlAcceptor.isAccepted("http://monsite5.com/zePage")).isFalse();
		assertThat(urlAcceptor.isAccepted("https://www.monsite5.com/zePage")).isFalse();
		assertThat(urlAcceptor.isAccepted("https://monsite5.com/zePage")).isFalse();
		assertThat(urlAcceptor.isAccepted("https://monsite1.com/zePage")).isFalse();
		assertThat(urlAcceptor.isAccepted("https://monsite2.com/zePage")).isFalse();

	}

	private void assertThatSeedsAreTrue() {
		assertThat(urlAcceptor.isAccepted("http://www.monsite1.com")).isTrue();
		assertThat(urlAcceptor.isAccepted("http://www.monsite2.com/index.html")).isTrue();
	}

	@Test
	public void givenExcludeListWhenAcceptingUrlMatchingThenFalse()
			throws Exception {

		excludePatterns = "http://www.monsite3.com/"
				+ "\nhttp://www.monsite4.com/";
		when(connector.getExcludePatterns()).thenReturn(excludePatterns);

		assertThatSeedsAreTrue();

		assertThat(urlAcceptor.isAccepted("http://www.monsite3.com/zePage")).isFalse();
		assertThat(urlAcceptor.isAccepted("http://www.monsite4.com/zePage")).isFalse();
	}

	@Test
	public void givenExcludeAndIncludeListWhenAcceptingUrlMatchingIncludeThenTrue()
			throws Exception {

		includePatterns = "http://www.monsite3.com/"
				+ "\nhttp://www.monsite4.com/";
		excludePatterns = "http://www.monsite5.com/"
				+ "\nhttp://www.monsite6.com/";
		when(connector.getIncludePatterns()).thenReturn(includePatterns);
		when(connector.getExcludePatterns()).thenReturn(excludePatterns);

		assertThatSeedsAreTrue();

		assertThat(urlAcceptor.isAccepted("http://www.monsite3.com/zePage")).isTrue();
		assertThat(urlAcceptor.isAccepted("http://www.monsite4.com/zePage")).isTrue();
		
		assertThat(urlAcceptor.isAccepted("http://www.monsite5.com/zePage")).isFalse();
		assertThat(urlAcceptor.isAccepted("http://www.monsite6.com/zePage")).isFalse();
	}

	@Test
	public void givenExcludeAndIncludeListWhenAcceptingUrlMatchingNothingThenFalse()
			throws Exception {
		includePatterns = "http://www.monsite3.com/"
				+ "\nhttp://www.monsite4.com/";
		excludePatterns = "http://www.monsite6.com/"
				+ "\nhttp://www.monsite4.com/";
		when(connector.getIncludePatterns()).thenReturn(includePatterns);
		when(connector.getExcludePatterns()).thenReturn(excludePatterns);

		assertThatSeedsAreTrue();

		assertThat(urlAcceptor.isAccepted("http://monsite5.com/zePage")).isFalse();
		assertThat(urlAcceptor.isAccepted("https://monsite1.com/zePage")).isFalse();
		assertThat(urlAcceptor.isAccepted("https://monsite2.com/zePage")).isFalse();
	}

	@Test
	public void givenExcludeAndIncludeListWhenAcceptingUrlMatchingBothThenFalse()
			throws Exception {
		includePatterns = "http://www.monsite3.com/"
				+ "\nhttp://www.monsite4.com/" + "\nhttp://www.monsite5.com/";
		excludePatterns = "http://www.monsite3.com/"
				+ "\nhttp://www.monsite4.com/";
		when(connector.getIncludePatterns()).thenReturn(includePatterns);
		when(connector.getExcludePatterns()).thenReturn(excludePatterns);

		assertThatSeedsAreTrue();

		assertThat(urlAcceptor.isAccepted("http://www.monsite3.com/zePage")).isFalse();
		assertThat(urlAcceptor.isAccepted("http://www.monsite4.com/zePage")).isFalse();
		assertThat(urlAcceptor.isAccepted("http://www.monsite5.com/zePage")).isTrue();
		assertThat(urlAcceptor.isAccepted("http://www.monsite6.com/zePage")).isFalse();
	}
	
	@Test
	public void givenNoSeedWhenAcceptingUrlThenEvaluateWhite()
			throws Exception {
		when(connector.getSeedsList()).thenReturn(asList(
				"http://www.monsite1.com",
				"http://www.monsite2.com/index.html"
		));
		
		includePatterns = "http://www.monsite1.com/";
		when(connector.getIncludePatterns()).thenReturn(includePatterns);

		assertThat(urlAcceptor.isAccepted("http://www.monsite1.com/zePage")).isTrue();
		assertThat(urlAcceptor.isAccepted("http://www.monsite2.com/zePage")).isFalse();
	}

	@Test
	public void givenMalFormedUrlInIncludeListWhenAcceptingUrlThenFalse()
			throws Exception {
		includePatterns = "email@email.com" + "\n "
				+ "\nhttp://www.monsite4.com/";
		when(connector.getIncludePatterns()).thenReturn(includePatterns);
		when(connector.getExcludePatterns()).thenReturn(excludePatterns);

		assertThatSeedsAreTrue();

		assertThat(urlAcceptor.isAccepted("")).isFalse();
		assertThat(urlAcceptor.isAccepted(" ")).isFalse();
		assertThat(urlAcceptor.isAccepted("email@email.com")).isFalse();
		assertThat(urlAcceptor.isAccepted("http://www.monsite4.com/zePage")).isTrue();
	}
}
