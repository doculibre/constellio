package com.constellio.model.services.records.populators;

import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.extensions.ModelLayerCollectionExtensions;
import com.constellio.model.extensions.events.schemas.SearchFieldPopulatorParams;
import com.constellio.model.services.contents.ParsedContentProvider;
import com.constellio.model.services.extensions.ModelLayerExtensions;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.parser.LanguageDetectionManager;
import com.constellio.sdk.tests.ConstellioTest;
import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SearchFieldsPopulatorTest extends ConstellioTest {

	String oldElvishText = "oldElvishText1";
	String oldElvishText2 = "oldElvishText2";
	String klingonIntimidation = "klingonIntimidation";
	String klingonIntimidation2 = "klingonIntimidation2";
	String beauceronMessage = "Or'tchûle toé";
	String failureMessage = "boom";

	@Mock Metadata metadata;

	@Mock LanguageDetectionManager languageDectionServices;

	List<String> collectionLanguages = Arrays.asList("klingon", "elvish");

	SearchFieldsPopulator populator;

	String klingonIntimidationContent1Hash = "klingonIntimidationContent1Hash";
	String klingonIntimidationContent2Hash = "klingonIntimidationContent2Hash";
	String oldElvishTextContentHash = "oldElvishTextContentHash";
	String contentWithoutParsedContentHash = "contentWithoutParsedContentHash";

	@Mock Content oldElvishTextContent;
	@Mock Content klingonIntimidationContent1;
	@Mock Content klingonIntimidationContent2;
	@Mock Content contentWithoutParsedContent;
	@Mock ContentVersion klingonIntimidationContent1CurrentVersion;
	@Mock ContentVersion klingonIntimidationContent2CurrentVersion;
	@Mock ContentVersion oldElvishTextContentCurrentVersion;
	@Mock ContentVersion contentWithoutParsedContentCurrentVersion;

	String klingonIntimidationContent1CurrentVersionFilename = "klingonIntimidationContent1CurrentVersionFilename";
	String klingonIntimidationContent2CurrentVersionFilename = "klingonIntimidationContent2CurrentVersionFilename";
	String oldElvishTextContentCurrentVersionFilename = "oldElvishTextContentCurrentVersionFilename";
	String contentWithoutParsedContentCurrentVersionFilename = "contentWithoutParsedContentCurrentVersionFilename";

	@Mock ParsedContentProvider parsedContentProvider;
	@Mock MetadataSchemaTypes types;
	@Mock ConstellioEIMConfigs configs;
	@Mock ModelLayerExtensions extensions;

	CollectionInfo collectionInfo = new CollectionInfo((byte) 0, zeCollection, "klingon", collectionLanguages);

	@Before
	public void setUp()
			throws Exception {


		populator = new SearchFieldsPopulator(types, false, parsedContentProvider, collectionInfo, configs, extensions);

		when(languageDectionServices.tryDetectLanguage(oldElvishText)).thenReturn("elvish");
		when(languageDectionServices.tryDetectLanguage(oldElvishText2)).thenReturn("elvish");
		when(languageDectionServices.tryDetectLanguage(klingonIntimidation)).thenReturn("klingon");
		when(languageDectionServices.tryDetectLanguage(klingonIntimidation2)).thenReturn("klingon");
		when(languageDectionServices.tryDetectLanguage(beauceronMessage)).thenReturn(null);
		when(languageDectionServices.tryDetectLanguage(failureMessage)).thenReturn(null);

		when(oldElvishTextContent.getCurrentVersion()).thenReturn(oldElvishTextContentCurrentVersion);
		when(klingonIntimidationContent1.getCurrentVersion()).thenReturn(klingonIntimidationContent1CurrentVersion);
		when(klingonIntimidationContent2.getCurrentVersion()).thenReturn(klingonIntimidationContent2CurrentVersion);
		when(contentWithoutParsedContent.getCurrentVersion()).thenReturn(contentWithoutParsedContentCurrentVersion);

		when(klingonIntimidationContent1CurrentVersion.getHash()).thenReturn(klingonIntimidationContent1Hash);
		when(klingonIntimidationContent2CurrentVersion.getHash()).thenReturn(klingonIntimidationContent2Hash);
		when(oldElvishTextContentCurrentVersion.getHash()).thenReturn(oldElvishTextContentHash);
		when(contentWithoutParsedContentCurrentVersion.getHash()).thenReturn(contentWithoutParsedContentHash);

		when(klingonIntimidationContent1CurrentVersion.getFilename())
				.thenReturn(klingonIntimidationContent1CurrentVersionFilename);
		when(klingonIntimidationContent2CurrentVersion.getFilename())
				.thenReturn(klingonIntimidationContent2CurrentVersionFilename);
		when(oldElvishTextContentCurrentVersion.getFilename()).thenReturn(oldElvishTextContentCurrentVersionFilename);
		when(contentWithoutParsedContentCurrentVersion.getFilename())
				.thenReturn(contentWithoutParsedContentCurrentVersionFilename);

		when(parsedContentProvider.getParsedContentIfAlreadyParsed(klingonIntimidationContent1Hash))
				.thenReturn(klingonParsedContent(klingonIntimidation));
		when(parsedContentProvider.getParsedContentIfAlreadyParsed(klingonIntimidationContent2Hash))
				.thenReturn(klingonParsedContent(klingonIntimidation2));
		when(parsedContentProvider.getParsedContentIfAlreadyParsed(oldElvishTextContentHash)).thenReturn(
				elvishParsedContent(oldElvishText));
		when(parsedContentProvider.getParsedContentIfAlreadyParsed(contentWithoutParsedContentHash)).thenReturn(null);

		when(configs.getDateFormat()).thenReturn("yyyy-MM-dd");

		when(types.getCollectionInfo()).thenReturn(new CollectionInfo((byte) 0, zeCollection, "klingon", asList("klingon", "elvish")));
	}

	@Test
	public void whenPopulatingForNotSearchableTextMetadata()
			throws Exception {

		when(metadata.getType()).thenReturn(MetadataValueType.STRING);
		when(metadata.getDataStoreCode()).thenReturn("title_s");
		when(metadata.getLocalCode()).thenReturn("title");
		when(metadata.isSearchable()).thenReturn(false);
		when(metadata.isSortable()).thenReturn(false);

		assertThat(populate(oldElvishText)).isEmpty();

	}

	@Test
	public void whenPopulatingForNotSearchableContentMetadata()
			throws Exception {

		when(metadata.getType()).thenReturn(MetadataValueType.CONTENT);
		when(metadata.getDataStoreCode()).thenReturn("content_s");
		when(metadata.getLocalCode()).thenReturn("content");
		when(metadata.isSearchable()).thenReturn(false);
		when(metadata.isSortable()).thenReturn(false);

		assertThat(populate(oldElvishText)).isEmpty();

	}

	//Multilinguage broken@Test
	public void whenPopulatingForSearchableTextMetadataThenPopulateCopyFieldForCorrectLanguage()
			throws Exception {

		when(metadata.getType()).thenReturn(MetadataValueType.STRING);
		when(metadata.getDataStoreCode()).thenReturn("title_s");
		when(metadata.getLocalCode()).thenReturn("title");
		when(metadata.isSearchable()).thenReturn(true);
		when(metadata.isSortable()).thenReturn(false);

		assertThat(populate(oldElvishText))
				.containsOnly(MapEntry.entry("title_t_klingon", ""), MapEntry.entry("title_t_elvish", oldElvishText));
		assertThat(populate(klingonIntimidation))
				.containsOnly(MapEntry.entry("title_t_klingon", klingonIntimidation), MapEntry.entry("title_t_elvish", ""));
		assertThat(populate(beauceronMessage)).containsOnly(MapEntry.entry("title_t_elvish", ""),
				MapEntry.entry("title_t_klingon", ""));
		assertThat(populate(failureMessage)).containsOnly(MapEntry.entry("title_t_elvish", ""),
				MapEntry.entry("title_t_klingon", ""));

	}

	//Multilinguage broken@Test
	public void whenPopulatingForSearchableLargeTextMetadataThenPopulateCopyFieldForCorrectLanguage()
			throws Exception {

		when(metadata.getType()).thenReturn(MetadataValueType.TEXT);
		when(metadata.getDataStoreCode()).thenReturn("title_t");
		when(metadata.getLocalCode()).thenReturn("title");
		when(metadata.isSearchable()).thenReturn(true);
		when(metadata.isSortable()).thenReturn(false);

		assertThat(populate(oldElvishText))
				.containsOnly(MapEntry.entry("title_t_klingon", ""), MapEntry.entry("title_t_elvish", oldElvishText));
		assertThat(populate(klingonIntimidation))
				.containsOnly(MapEntry.entry("title_t_klingon", klingonIntimidation), MapEntry.entry("title_t_elvish", ""));
		assertThat(populate(beauceronMessage)).containsOnly(MapEntry.entry("title_t_elvish", ""),
				MapEntry.entry("title_t_klingon", ""));
		assertThat(populate(failureMessage)).containsOnly(MapEntry.entry("title_t_elvish", ""),
				MapEntry.entry("title_t_klingon", ""));

	}

	@Test
	public void whenPopulatingForSearchableContentMetadataThenPopulateCopyFieldForCorrectLanguage()
			throws Exception {

		when(metadata.getType()).thenReturn(MetadataValueType.CONTENT);
		when(metadata.getDataStoreCode()).thenReturn("content_s");
		when(metadata.getLocalCode()).thenReturn("content");
		when(metadata.isSearchable()).thenReturn(true);
		when(metadata.isSortable()).thenReturn(false);

		assertThat(populate(oldElvishTextContent))
				.containsOnly(MapEntry.entry("content_txt_klingon", Arrays.asList("")),
						MapEntry.entry("content_txt_elvish", Arrays.asList(oldElvishText)),
						MapEntry.entry("content_klingon_ss", Arrays.asList("")),
						MapEntry.entry("content_elvish_ss", Arrays.asList(oldElvishTextContentCurrentVersionFilename)));
		assertThat(populate(klingonIntimidationContent1))
				.containsOnly(MapEntry.entry("content_txt_klingon", Arrays.asList(klingonIntimidation)),
						MapEntry.entry("content_txt_elvish", Arrays.asList("")),
						MapEntry.entry("content_klingon_ss", Arrays.asList(klingonIntimidationContent1CurrentVersionFilename)),
						MapEntry.entry("content_elvish_ss", Arrays.asList("")));
		assertThat(populate(contentWithoutParsedContent)).containsOnly(MapEntry.entry("content_txt_elvish", Arrays.asList("")),
				MapEntry.entry("content_txt_klingon", Arrays.asList("")),
				MapEntry.entry("content_klingon_ss", Arrays.asList(contentWithoutParsedContentCurrentVersionFilename)),
				MapEntry.entry("content_elvish_ss", Arrays.asList(contentWithoutParsedContentCurrentVersionFilename)));

	}

	@Test
	public void whenPopulatingForNullSearchableTextMetadataThenPopulateNullCopyfieldsInAllLanguages()
			throws Exception {


		ModelLayerCollectionExtensions collectionExtensions = mock(ModelLayerCollectionExtensions.class);
		when(extensions.forCollection(anyString())).thenReturn(collectionExtensions);
		when(collectionExtensions.populateSearchField(any(SearchFieldPopulatorParams.class))).thenReturn(null);

		when(metadata.getType()).thenReturn(MetadataValueType.STRING);
		when(metadata.getDataStoreCode()).thenReturn("title_s");
		when(metadata.getLocalCode()).thenReturn("title");
		when(metadata.isSearchable()).thenReturn(true);
		when(metadata.isSortable()).thenReturn(false);

		assertThat(populateForKlingon(null)).containsOnly(MapEntry.entry("title_t_klingon", null));

		assertThat(populateForElvish(null)).containsOnly(MapEntry.entry("title_t_elvish", null));

	}

	@Test
	public void whenPopulatingForNullSearchableContentMetadataThenPopulateNullCopyfieldsInAllLanguages()
			throws Exception {

		when(metadata.getType()).thenReturn(MetadataValueType.CONTENT);
		when(metadata.getDataStoreCode()).thenReturn("content_s");
		when(metadata.getLocalCode()).thenReturn("content");
		when(metadata.isSearchable()).thenReturn(true);
		when(metadata.isSortable()).thenReturn(false);

		assertThat(populate(null)).containsOnly(MapEntry.entry("content_txt_elvish", Arrays.asList("")),
				MapEntry.entry("content_txt_klingon", Arrays.asList("")), MapEntry.entry("content_elvish_ss", Arrays.asList("")),
				MapEntry.entry("content_klingon_ss", Arrays.asList("")));

	}

	//Multilinguage broken@Test
	public void whenPopulatingForSearchableMultivalueTextMetadataThenPopulateCopyFieldForCorrectLanguage()
			throws Exception {

		when(metadata.getType()).thenReturn(MetadataValueType.STRING);
		when(metadata.getDataStoreCode()).thenReturn("title_ss");
		when(metadata.getLocalCode()).thenReturn("title");
		when(metadata.isMultivalue()).thenReturn(true);
		when(metadata.isSearchable()).thenReturn(true);
		when(metadata.isSortable()).thenReturn(false);
		Object value = Arrays.asList(klingonIntimidation, klingonIntimidation2, beauceronMessage, oldElvishText, failureMessage);
		assertThat(populator.populateCopyfields(metadata, value, Locale.FRENCH)).hasSize(2)
				.containsEntry("title_txt_klingon", Arrays.asList(klingonIntimidation, klingonIntimidation2))
				.containsEntry("title_txt_elvish", Arrays.asList(oldElvishText));

		value = Arrays.asList(klingonIntimidation, klingonIntimidation2, beauceronMessage);
		assertThat(populator.populateCopyfields(metadata, value, Locale.FRENCH)).hasSize(2)
				.containsEntry("title_txt_klingon", Arrays.asList(klingonIntimidation, klingonIntimidation2))
				.containsEntry("title_txt_elvish", Arrays.asList(""));

	}

	@Test
	public void whenPopulatingForSearchableMultivalueContentMetadataThenPopulateCopyFieldForCorrectLanguage()
			throws Exception {

		when(metadata.getType()).thenReturn(MetadataValueType.CONTENT);
		when(metadata.getDataStoreCode()).thenReturn("content_ss");
		when(metadata.getLocalCode()).thenReturn("content");
		when(metadata.isMultivalue()).thenReturn(true);
		when(metadata.isSearchable()).thenReturn(true);
		when(metadata.isSortable()).thenReturn(false);

		Object value = Arrays.asList(oldElvishTextContent, klingonIntimidationContent1, klingonIntimidationContent2,
				contentWithoutParsedContent);
		assertThat(populator.populateCopyfields(metadata, value, Locale.FRENCH)).hasSize(4)
				.containsEntry("content_txt_klingon", Arrays.asList(klingonIntimidation, klingonIntimidation2))
				.containsEntry("content_txt_elvish", Arrays.asList(oldElvishText))
				.containsEntry("content_klingon_ss",
						Arrays.asList(klingonIntimidationContent1CurrentVersionFilename,
								klingonIntimidationContent2CurrentVersionFilename,
								contentWithoutParsedContentCurrentVersionFilename))
				.containsEntry("content_elvish_ss",
						Arrays.asList(oldElvishTextContentCurrentVersionFilename,
								contentWithoutParsedContentCurrentVersionFilename));

		value = Arrays.asList(klingonIntimidationContent1, contentWithoutParsedContent);
		assertThat(populator.populateCopyfields(metadata, value, Locale.FRENCH)).hasSize(4)
				.containsEntry("content_txt_klingon", Arrays.asList(klingonIntimidation))
				.containsEntry("content_klingon_ss",
						Arrays.asList(klingonIntimidationContent1CurrentVersionFilename,
								contentWithoutParsedContentCurrentVersionFilename))
				.containsEntry("content_elvish_ss",
						Arrays.asList(contentWithoutParsedContentCurrentVersionFilename))
				.containsEntry("content_txt_elvish", Arrays.asList(""))
		;

	}

	@Test
	public void whenPopulatingForNullSearchableMultivalueTextMetadataThenPopulateNullCopyfieldsInAllLanguages()
			throws Exception {

		when(metadata.getType()).thenReturn(MetadataValueType.STRING);
		when(metadata.getDataStoreCode()).thenReturn("title_ss");
		when(metadata.getLocalCode()).thenReturn("title");
		when(metadata.isMultivalue()).thenReturn(true);
		when(metadata.isSearchable()).thenReturn(true);
		when(metadata.isSortable()).thenReturn(false);

		assertThat(populateForKlingon(null)).containsOnly(MapEntry.entry("title_txt_klingon", Arrays.asList("")));

		assertThat(populateForElvish(null)).containsOnly(MapEntry.entry("title_txt_elvish", Arrays.asList("")));

	}

	@Test
	public void whenPopulatingForNullSearchableMultivalueContentMetadataThenPopulateNullCopyfieldsInAllLanguages()
			throws Exception {

		when(metadata.getType()).thenReturn(MetadataValueType.CONTENT);
		when(metadata.getDataStoreCode()).thenReturn("content_ss");
		when(metadata.getLocalCode()).thenReturn("content");
		when(metadata.isMultivalue()).thenReturn(true);
		when(metadata.isSearchable()).thenReturn(true);
		when(metadata.isSortable()).thenReturn(false);

		assertThat(populate(null)).containsOnly(MapEntry.entry("content_txt_elvish", Arrays.asList("")),
				MapEntry.entry("content_txt_klingon", Arrays.asList("")), MapEntry.entry("content_elvish_ss", Arrays.asList("")),
				MapEntry.entry("content_klingon_ss", Arrays.asList("")));

	}

	private ParsedContent elvishParsedContent(String text) {
		return new ParsedContent(text, "elvish", "zeMime", 42, new HashMap<String, Object>(),
				new HashMap<String, List<String>>());
	}

	private ParsedContent klingonParsedContent(String text) {
		return new ParsedContent(text, "klingon", "zeMime", 666, new HashMap<String, Object>(),
				new HashMap<String, List<String>>());
	}

	private Map<String, Object> populate(Object value) {
		return populator.populateCopyfields(metadata, value, new Locale("klingon"));
	}


	private Map<String, Object> populateForKlingon(Object value) {
		return populator.populateCopyfields(metadata, value, new Locale("klingon"));
	}

	private Map<String, Object> populateForElvish(Object value) {
		return populator.populateCopyfields(metadata, value, new Locale("elvish"));
	}
}
