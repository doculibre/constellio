package com.constellio.app.services.guide;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.PropertiesAlteration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

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
	@Mock DataLayerFactory dataLayerFactory;
	@Mock ConfigManager configManager;

	@Before
	public void setUp()
			throws Exception {
		buildDummyGuideUrlFile();
		USER_DEFINED_URLS = new PropertiesConfiguration("", customUrlsFile);
		when(configManager.getProperties(anyString())).thenReturn(USER_DEFINED_URLS);

		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				PropertiesAlteration propertiesAlteration = (PropertiesAlteration) arguments[1];
				String newPropertyValue = propertiesAlteration.toString();
				customUrlsFile.put((String) arguments[0], newPropertyValue);
				return null;
			}
		}).when(configManager).updateProperties(anyString(), any(PropertiesAlteration.class));


		when(dataLayerFactory.getConfigManager()).thenReturn(configManager);
		guideManager = new GuideManager(dataLayerFactory);

	}

	private void buildDummyGuideUrlFile() {
		customUrlsFile.put("pageName_fr", URL_DEFAULT_LANGUAGE);
		customUrlsFile.put("pageName_en", URL_SECOND_LANGUAGE);
		customUrlsFile.put("pageName_ar", URL_THIRD_LANGUAGE);
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

	/*
	@Test
	public void givenNoCustomUrlDefinedWhenAddingNewCustomUrlThenIsAdded(){
		String newProperty = "new_custom_property";
		String newPropertyValue = "new_custom_url";
		guideManager.alterProperty(DEFAULT_LANGUAGE,newProperty,newPropertyValue);
		assertThat(guideManager.getPropertyValue(DEFAULT_LANGUAGE,newProperty).equals(newPropertyValue));
	}

	@Test
	public void givenCustomUrlDefinedWhenAddingNewCustomUrlThenIsReplaced(){
		//guideManager.alterProperty();
	}
*/

}
