package com.constellio.app.services.guide;

import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class GuideManagerAcceptanceTest extends ConstellioTest {
	private static final String CUSTOM_PROPERTY_KEY_THAT_EXISTS = "pageName";
	private static final String DEFAULT_LANGUAGE = "fr";
	private static final String OTHER_LANGUAGE_1 = "en";
	private static final String OTHER_LANGUAGE_2 = "ar";

	private static final String URL_DEFAULT_LANGUAGE = "http://www.default-language.com";
	private static final String URL_SECOND_LANGUAGE = "http://www.second-language.com";
	private static final String URL_THIRD_LANGUAGE = "http://www.third-language.com";


	private String CUSTOM_URL = "url personalisé (dans guide.properties)";
	private Map<String, String> customUrlsFile = new HashMap<>();

	private PropertiesConfiguration USER_DEFINED_URLS;

	GuideManager guideManager;

	@Before
	public void setUp()
			throws Exception {
		USER_DEFINED_URLS = new PropertiesConfiguration("", customUrlsFile);
		guideManager = new GuideManager(getDataLayerFactory());
		guideManager.alterProperty(DEFAULT_LANGUAGE, CUSTOM_PROPERTY_KEY_THAT_EXISTS, URL_DEFAULT_LANGUAGE);
	}


	@Test
	public void givenCustomUrlDefinedWhenFetchingGuideUrlThenReturnCustomUrl() {
		String returnedUrl = guideManager.getPropertyValue(DEFAULT_LANGUAGE, CUSTOM_PROPERTY_KEY_THAT_EXISTS);
		assertThat(returnedUrl.equals(CUSTOM_URL));
	}

	@Test
	public void givenPageWithNoCustomUrlDefinedWhenFetchingGuideUrlThenReturnDefaultUrl() {
		String CUSTOM_PROPERTY_KEY_THAT_DOESNT_EXIST = "clé qui n'existe pas";
		String returnedUrl = guideManager.getPropertyValue(DEFAULT_LANGUAGE, CUSTOM_PROPERTY_KEY_THAT_DOESNT_EXIST);
		assertThat(returnedUrl).isNull();
	}

	@Test
	public void givenCustomUrlDefinedWhenFetchingGuideUrlForDifferentLanguageThenReturnUrlOfCorrectLangugage() {
		guideManager.alterProperty(DEFAULT_LANGUAGE, CUSTOM_PROPERTY_KEY_THAT_EXISTS, URL_DEFAULT_LANGUAGE);
		guideManager.alterProperty(OTHER_LANGUAGE_1, CUSTOM_PROPERTY_KEY_THAT_EXISTS, URL_SECOND_LANGUAGE);
		guideManager.alterProperty(OTHER_LANGUAGE_2, CUSTOM_PROPERTY_KEY_THAT_EXISTS, URL_THIRD_LANGUAGE);


		String returnedUrl_fr = guideManager.getPropertyValue(DEFAULT_LANGUAGE, CUSTOM_PROPERTY_KEY_THAT_EXISTS);
		assertThat(returnedUrl_fr.equals(URL_DEFAULT_LANGUAGE));

		String returnedUrl_en = guideManager.getPropertyValue(OTHER_LANGUAGE_1, CUSTOM_PROPERTY_KEY_THAT_EXISTS);
		assertThat(returnedUrl_en.equals(URL_SECOND_LANGUAGE));

		String returnedUrl_ar = guideManager.getPropertyValue(OTHER_LANGUAGE_2, CUSTOM_PROPERTY_KEY_THAT_EXISTS);
		assertThat(returnedUrl_ar.equals(URL_THIRD_LANGUAGE));
	}

	@Test
	public void givenCustomUrlDefinedWhenFetchingGuideUrlForUnsupportedLanguageThenNull() {
		String UNSUPPORTED_LANGUAGE = "de";
		String returnedUrl = guideManager.getPropertyValue(UNSUPPORTED_LANGUAGE, CUSTOM_PROPERTY_KEY_THAT_EXISTS);
		assertThat(returnedUrl).isNull();
	}


	@Test
	public void givenNoCustomUrlDefinedWhenAddingNewCustomUrlThenIsAdded() {
		String newProperty = "new_custom_property";
		String newPropertyValue = "new_custom_url";
		guideManager.alterProperty(DEFAULT_LANGUAGE, newProperty, newPropertyValue);
		assertThat(guideManager.getPropertyValue(DEFAULT_LANGUAGE, newProperty).equals(newPropertyValue));
	}

	@Test
	public void givenCustomUrlDefinedWhenAddingNewCustomUrlThenIsReplaced() {
		assertThat(guideManager.getPropertyValue(DEFAULT_LANGUAGE, CUSTOM_PROPERTY_KEY_THAT_EXISTS).equals(URL_DEFAULT_LANGUAGE));
		String A_DIFFERENT_VALUE = "je suis une url";
		guideManager.alterProperty(DEFAULT_LANGUAGE, CUSTOM_PROPERTY_KEY_THAT_EXISTS, A_DIFFERENT_VALUE);
		assertThat(guideManager.getPropertyValue(DEFAULT_LANGUAGE, CUSTOM_PROPERTY_KEY_THAT_EXISTS).equals(A_DIFFERENT_VALUE));
	}


	@Test
	public void givenCustomUrlDefinedWhenNewUrlIsEmptyThenIsDeleted() {
		assertThat(guideManager.getPropertyValue(DEFAULT_LANGUAGE, CUSTOM_PROPERTY_KEY_THAT_EXISTS).equals(URL_DEFAULT_LANGUAGE));

		guideManager.alterProperty(DEFAULT_LANGUAGE, CUSTOM_PROPERTY_KEY_THAT_EXISTS, "");
		assertThat(guideManager.getPropertyValue(DEFAULT_LANGUAGE, CUSTOM_PROPERTY_KEY_THAT_EXISTS)).isNull();
	}

	@Test
	public void givenCustomUrlDefinedWhenNewUrlIsSameAsDefaultUrlThenIsDeleted() {
		//todo: impossible de mocker i18n... comment tester?
	}
}