package com.constellio.model.services.emails;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class EmailTemplatesManagerAcceptanceTest extends ConstellioTest {
	private EmailTemplatesManager manager;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection()
		);

		manager = getModelLayerFactory().getEmailTemplatesManager();
		manager.initialize();
	}

	@Test
	public void whenAddingNewTemplateThenAddedCorrectly()
			throws Exception {
		String templateText = "lol";
		String templateId = "folderReturnReminder";
		InputStream inputStream = new ByteArrayInputStream(templateText.getBytes());
		manager.addCollectionTemplate(templateId, zeCollection, inputStream);
		//purge cache
		manager.initialize();
		String text = manager.getCollectionTemplate(templateId, zeCollection);
		assertThat(text).isEqualTo(templateText);
	}

	@Test
	public void whenReplacingExistingTemplateThenReplacedCorrectly()
			throws Exception {
		//add
		String templateText = "lol";
		String templateId = "folderReturnReminder";
		InputStream inputStream = new ByteArrayInputStream(templateText.getBytes());
		manager.addCollectionTemplate(templateId, zeCollection, inputStream);
		assertThat(manager.getCollectionTemplate(templateId, zeCollection)).isEqualTo(templateText);
		//replace
		String replacementText = "replaceLol";
		inputStream = new ByteArrayInputStream(replacementText.getBytes());
		manager.replaceCollectionTemplate(templateId, zeCollection, inputStream);
		//purge cache
		manager.initialize();
		assertThat(manager.getCollectionTemplate(templateId, zeCollection)).isEqualTo(replacementText);

	}

	@Test
	public void whenAddingNewTemplateThenAddedCorrectly1()
			throws Exception {
		String templateText = "lol";
		String templateId = "folderReturnReminder";
		InputStream inputStream = new ByteArrayInputStream(templateText.getBytes());
		manager.addCollectionTemplate(templateId, zeCollection, inputStream);
		//purge cache
		manager.initialize();
		String text = manager.getCollectionTemplate(templateId, zeCollection);
		assertThat(text).isEqualTo(templateText);
	}
}
