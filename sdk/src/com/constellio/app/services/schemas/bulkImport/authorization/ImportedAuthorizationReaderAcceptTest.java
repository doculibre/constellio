package com.constellio.app.services.schemas.bulkImport.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.junit.Test;

import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorization.ImportedAuthorizationPrincipal;
import com.constellio.app.services.schemas.bulkImport.authorization.ImportedAuthorization.ImportedAuthorizationTarget;
import com.constellio.sdk.tests.ConstellioTest;

public class ImportedAuthorizationReaderAcceptTest extends ConstellioTest {
	@Test
	public void whenReadingAuthorizationXMLFileThenReadCorrectly()
			throws Exception {
		List<ImportedAuthorization> allAuthorizations = readTestAuthorizations();
		assertThat(allAuthorizations.size()).isEqualTo(2);
		ImportedAuthorization emptyAuthorization = allAuthorizations.get(0);
		assertThat(emptyAuthorization.getId()).isNull();
		assertThat(emptyAuthorization.getAccess()).isNull();
		assertThat(emptyAuthorization.getPrincipals()).isEmpty();
		assertThat(emptyAuthorization.getRoles()).isEmpty();
		assertThat(emptyAuthorization.getTargets()).isEmpty();
		ImportedAuthorization nonEmptyAuthorization = allAuthorizations.get(1);
		assertThat(nonEmptyAuthorization.getId()).isEqualTo("authorizationId");
		assertThat(nonEmptyAuthorization.getAccess()).isEqualTo("rwd");
		assertThat(nonEmptyAuthorization.getRoles()).containsExactly("u", "g", "rgd");
		assertThat(nonEmptyAuthorization.getTargets()).containsExactly(
				new ImportedAuthorizationTarget("folder", "folderLegacyId"),
				new ImportedAuthorizationTarget("document", "documentLegacyId"),
				new ImportedAuthorizationTarget("administrativeUnit", "administrativeUnitLegacyId"),
				new ImportedAuthorizationTarget("userTask", "userTaskLegacyId"));
		assertThat(nonEmptyAuthorization.getPrincipals())
				.containsExactly(new ImportedAuthorizationPrincipal("user", "alice"),
						new ImportedAuthorizationPrincipal("group", "heroes"));
	}

	private List<ImportedAuthorization> readTestAuthorizations()
			throws Exception {
		File authorizationFile = getTestResourceFile("authorizations.xml");
		Document document = new SAXBuilder().build(authorizationFile);
		ImportedAuthorizationReader reader = new ImportedAuthorizationReader(document);
		return reader.readAll();
	}
}
