import org.junit.Test;

import com.constellio.app.modules.es.connectors.http.fetcher.HttpURLFetchingService;
import com.constellio.sdk.tests.ConstellioTest;

public class HttpURLFetchingServiceAcceptTest extends ConstellioTest{

	@Test
	public void unknownProblem()
			throws Exception {


		HttpURLFetchingService service = new HttpURLFetchingService(10000);
		service.fetch("http://www.csst.qc.ca/Pages/index.aspx");
	}
}
