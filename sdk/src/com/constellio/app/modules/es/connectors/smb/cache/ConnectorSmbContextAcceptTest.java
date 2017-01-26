package com.constellio.app.modules.es.connectors.smb.cache;

import com.constellio.app.modules.es.connectors.http.ConnectorHttpContext;
import com.constellio.app.modules.es.connectors.http.ConnectorHttpContextServices;
import com.constellio.app.modules.es.connectors.smb.cache.SmbConnectorContext;
import com.constellio.app.modules.es.connectors.smb.cache.SmbConnectorContextServices;
import com.constellio.app.modules.es.connectors.smb.service.SmbModificationIndicator;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConnectorSmbContextAcceptTest extends ConstellioTest {

	SmbConnectorContextServices services;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioESModule());

		ESSchemasRecordsServices es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());
		services = new SmbConnectorContextServices(es);

	}

	@Test
	public void whenSaveAndLoadUrlListThenValuesConserved()
			throws Exception {

		ESSchemasRecordsServices es = new ESSchemasRecordsServices(zeCollection, getAppLayerFactory());

		SmbConnectorContext context = services.createContext("zeConnector");
		context.traverseModified("smb://myShare/folder/", new SmbModificationIndicator("myHash", 0D, 5), "smb://myShare/", null);
		context.traverseModified("smb://myShare/folder/test.html", new SmbModificationIndicator("myHash", 5D, 5), "smb://myShare/folder/", null);
		context.traverseModified("smb://myShare/folder/test.html", new SmbModificationIndicator("newHash", 5D, 5), "smb://myShare/folder/", null);
		context.traverseModified("smb://myShare/folder/subFolder/", new SmbModificationIndicator("myHash", 0D, 5), "smb://myShare/folder/", null);
		context.traverseModified("smb://myShare/folder/subFolder/test.html", new SmbModificationIndicator("myHash", 5D, 5), "smb://myShare/folder/subFolder/", null);

		services.save(context);

		SmbConnectorContext context2 = services.loadContext("zeConnector");
		assertThat(context2.recordUrls).containsKeys(
				"smb://myShare/folder/",
				"smb://myShare/folder/test.html",
				"smb://myShare/folder/subFolder/",
				"smb://myShare/folder/subFolder/test.html"
		);

		context.traverseModified("smb://myShare/folder/test.html", new SmbModificationIndicator("newHash", 5D, 10), "folderId", null);
		context.traverseUnchanged("smb://myShare/folder/test.html", "myTraversal");
		context.traverseUnchanged("smb://myShare/folder/", "myTraversal");
		context.traverseUnchanged("smb://myShare/folder/subFolder/", "anotherTraversal");
		context.delete("smb://myShare/folder/subFolder/test.html");

		services.save(context);

		SmbConnectorContext context3 = services.loadContext("zeConnector");
		assertThat(context3.recordUrls).containsKeys(
				"smb://myShare/folder/",
				"smb://myShare/folder/test.html",
				"smb://myShare/folder/subFolder/"
		);

		assertThat(context3.staleUrls("anotherTraversal")).containsExactly("smb://myShare/folder/test.html", "smb://myShare/folder/");

		SmbModificationIndicator indicator = context3.getModificationIndicator("smb://myShare/folder/test.html");
		assertThat(indicator.getParentId()).isEqualTo("folderId");
		assertThat(indicator.getSize()).isEqualTo(5D);
		assertThat(indicator.getLastModified()).isEqualTo(10);
		assertThat(indicator.getPermissionsHash()).isEqualTo("newHash");
		assertThat(indicator.getTraversalCode()).isEqualTo("myTraversal");

		context.getParentId("smb://myShare/folder/test.html");
	}
}
