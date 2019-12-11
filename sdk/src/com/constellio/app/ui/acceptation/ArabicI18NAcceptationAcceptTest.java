package com.constellio.app.ui.acceptation;

import com.constellio.data.utils.LangUtils.ListComparisonResults;
import com.constellio.model.entities.Language;
import com.constellio.sdk.dev.tools.CompareI18nKeys;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

public class ArabicI18NAcceptationAcceptTest extends ConstellioTest {

	private static List<String> keysWithSameFrenchEnglishValue = new ArrayList<>();

	static {
		keysWithSameFrenchEnglishValue.add("SystemConfigurationGroup.agent");
	}

	@Test
	public void ensureArabicAndFrenchLanguageFilesHaveSameKeys()
			throws Exception {
		//		assumeArabicLabelsValidated();
		ListComparisonResults<String> results = CompareI18nKeys.compare(Language.Arabic);

		if (!results.getNewItems().isEmpty() || !results.getRemovedItems().isEmpty()) {
			String comparisonMessage = CompareI18nKeys.getComparisonMessage(Language.Arabic, results);
			fail("Missing i18n keys\n" + comparisonMessage);
		}
	}


	@Test
	public void ensureArabicAndFrenchLanguageCoreFilesHaveSameKeys()
			throws Exception {
		//		assumeArabicLabelsValidated();

		StringBuilder stringBuilder = new StringBuilder();
		String core = "core";
		addComparisonMessage(core, "baseView", stringBuilder);
		addComparisonMessage(core, "imports", stringBuilder);
		addComparisonMessage(core, "managementViews", stringBuilder);
		addComparisonMessage(core, "model", stringBuilder);
		addComparisonMessage(core, "schemasManagementViews", stringBuilder);
		addComparisonMessage(core, "search", stringBuilder);
		addComparisonMessage(core, "security", stringBuilder);
		addComparisonMessage(core, "usersAndGroupsManagementViews", stringBuilder);
		addComparisonMessage(core, "userViews", stringBuilder);
		addComparisonMessage(core, "webServices", stringBuilder);

		String finalComparisonMessage = stringBuilder.toString();
		if (!finalComparisonMessage.isEmpty()) {
			System.out.println(finalComparisonMessage);
			fail("Missing i18n keys");
		}
	}

	@Test
	public void ensureArabicAndFrenchLanguageEsFilesHaveSameKeys()
			throws Exception {
		//		assumeArabicLabelsValidated();

		StringBuilder stringBuilder = new StringBuilder();
		String es = "es";
		addComparisonMessage(es, "i18n", stringBuilder);
		String finalComparisonMessage = stringBuilder.toString();
		if (!finalComparisonMessage.isEmpty()) {
			System.out.println(finalComparisonMessage);
			fail("Missing i18n keys");
		}
	}

	@Test
	public void ensureArabicAndFrenchLanguageRmFilesHaveSameKeys()
			throws Exception {
		//		assumeArabicLabelsValidated();

		StringBuilder stringBuilder = new StringBuilder();
		String rm = "rm";
		addComparisonMessage(rm, "audits", stringBuilder);
		addComparisonMessage(rm, "decommissioningViews", stringBuilder);
		addComparisonMessage(rm, "demo", stringBuilder);
		addComparisonMessage(rm, "foldersAndDocuments", stringBuilder);
		addComparisonMessage(rm, "managementViews", stringBuilder);
		addComparisonMessage(rm, "model", stringBuilder);
		addComparisonMessage(rm, "reports", stringBuilder);
		addComparisonMessage(rm, "storageAndContainers", stringBuilder);
		addComparisonMessage(rm, "userViews", stringBuilder);

		String finalComparisonMessage = stringBuilder.toString();
		if (!finalComparisonMessage.isEmpty()) {
			System.out.println(finalComparisonMessage);
			fail("Missing i18n keys");
		}
	}

	@Test
	public void ensureArabicAndFrenchLanguageRobotsFilesHaveSameKeys()
			throws Exception {
		//		assumeArabicLabelsValidated();

		StringBuilder stringBuilder = new StringBuilder();
		String robots = "robots";
		addComparisonMessage(robots, "i18n", stringBuilder);

		String finalComparisonMessage = stringBuilder.toString();
		if (!finalComparisonMessage.isEmpty()) {
			System.out.println(finalComparisonMessage);
			fail("Missing i18n keys");
		}
	}

	@Test
	public void ensureArabicAndFrenchLanguageTasksFilesHaveSameKeys()
			throws Exception {
		//		assumeArabicLabels#Validated();

		StringBuilder stringBuilder = new StringBuilder();
		String tasks = "tasks";
		addComparisonMessage(tasks, "model", stringBuilder);
		addComparisonMessage(tasks, "views", stringBuilder);
		addComparisonMessage(tasks, "workflowBeta", stringBuilder);

		String finalComparisonMessage = stringBuilder.toString();
		if (!finalComparisonMessage.isEmpty()) {
			System.out.println(finalComparisonMessage);
			fail("Missing i18n keys");
		}
	}


	private void addComparisonMessage(String folderName, String keysFileName, StringBuilder stringBuilder)
			throws Exception {
		ListComparisonResults<String> results = CompareI18nKeys.compareKeys(Language.Arabic, folderName, keysFileName);
		if (!results.getNewItems().isEmpty() || !results.getRemovedItems().isEmpty()) {
			stringBuilder.append(CompareI18nKeys.getComparisonMessage(Language.Arabic, results, keysFileName));
			stringBuilder.append("\n\n");
		}
	}

	protected void assumeArabicLabelsValidated() {
		assumeTrue("Arabic i18n validations are disabled, set 'skip.arabicI18nTests=false' in sdk.properties to enable them", areArabicI18nValidationsEnabled());
	}

	protected boolean areArabicI18nValidationsEnabled() {
		return "false".equalsIgnoreCase(getCurrentTestSession().getProperty("skip.arabicI18nTests"));
	}

}
