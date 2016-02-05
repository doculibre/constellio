package com.constellio.model.services.security;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.values.XMLConfiguration;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.utils.OneXMLConfigPerCollectionManager;
import com.constellio.sdk.tests.ConstellioTest;

public class AuthorizationDetailsManagerTest extends ConstellioTest {

	static String AUTHORIZATIONS_CONFIG = "/authorizations.xml";
	@Mock ConfigManager configManager;
	@Mock CollectionsListManager collectionsListManager;
	@Mock AuthorizationDetailsWriter writer;
	@Mock DocumentAlteration addDocumentAlteration;
	@Mock DocumentAlteration removeAuthorizationDocumentAlteration;
	@Mock XMLConfiguration xmlConfiguration;
	@Mock Document document;
	@Mock OneXMLConfigPerCollectionManager<Map<String, AuthorizationDetails>> oneXMLConfigPerCollectionManager;

	AuthorizationDetails authorizationDetails;

	AuthorizationDetailsManager authorizationDetailsManager;
	ArrayList<String> metadataRelations;

	@Before
	public void setup()
			throws Exception {
		when(document.getRootElement()).thenReturn(new Element("authorizations"));
		when(configManager.getXML(AUTHORIZATIONS_CONFIG)).thenReturn(xmlConfiguration);
		when(xmlConfiguration.getDocument()).thenReturn(document);
		authorizationDetailsManager = spy(new AuthorizationDetailsManager(configManager, collectionsListManager));
		doReturn(oneXMLConfigPerCollectionManager).when(authorizationDetailsManager).newOneXMLConfigPerCollectionManager();
		authorizationDetailsManager.initialize();
		when(authorizationDetailsManager.newAuthorizationsWriter(any(Document.class))).thenReturn(writer);
		when(authorizationDetailsManager.getAuthorizationsDetails(zeCollection))
				.thenReturn(new HashMap<String, AuthorizationDetails>());

		doNothing().when(writer).createEmptyAuthorizations();
		authorizationDetails = newAthorization();
	}

	@Test
	public void whenAddAuthorizationThenItIsAddedToDocument()
			throws Exception {

		authorizationDetailsManager.add(authorizationDetails);

		verify(authorizationDetailsManager).newAddAuthorizationDocumentAlteration(authorizationDetails);
	}

	@Test
	public void whenRemoveThenCallRemoveFromWriter()
			throws Exception {

		doNothing().when(oneXMLConfigPerCollectionManager).updateXML(zeCollection, removeAuthorizationDocumentAlteration);
		when(authorizationDetailsManager.newRemoveAuthorizationDocumentAlteration(authorizationDetails))
				.thenReturn(removeAuthorizationDocumentAlteration);

		authorizationDetailsManager.remove(authorizationDetails);

		verify(authorizationDetailsManager).newRemoveAuthorizationDocumentAlteration(authorizationDetails);
		verify(oneXMLConfigPerCollectionManager).updateXML(zeCollection, removeAuthorizationDocumentAlteration);
	}

	@Test
	public void whenModifyEndDateOfAuthorizationThenCallModifyEndDateFromWriter()
			throws Exception {

		LocalDate endate = new LocalDate(2020, 1, 1);
		authorizationDetailsManager.modifyEndDate(authorizationDetails, endate);

		verify(authorizationDetailsManager).newModifyEndDateAuthorizationDocumentAlteration(any(AuthorizationDetails.class));
	}

	private AuthorizationDetails newAthorization() {
		List<String> roles = asList("role1", "role2");
		return AuthorizationDetails.create(aString(), roles, zeCollection);
	}

}
