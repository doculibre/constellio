package com.constellio.app.ui.pages.management.thesaurus;

import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ThesaurusConfig;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.thesaurus.SkosConcept;
import com.constellio.model.services.thesaurus.ThesaurusManager;
import com.constellio.model.services.thesaurus.ThesaurusService;
import com.constellio.model.services.thesaurus.ThesaurusServiceBuilder;
import com.constellio.model.services.thesaurus.exception.ThesaurusInvalidFileFormat;
import com.vaadin.server.Page;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.data.dao.services.contents.ContentDao.MoveToVaultOption.ONLY_IF_INEXISTING;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ThesaurusConfigurationPresenter extends BasePresenter<ThesaurusConfigurationView> {

	public static final String THESAURUS_CONFIGURATION_PRESENTER_STREAM_NAME = "thesaurusConfigurationPresenterStreamName";
	public static final String THESAURUS_TEMPORARY_FILE = "thesaurusTemporaryFile";
	public static final String THESAURUS_FILE = "thesaurus.xml";

	ThesaurusConfig thesaurusConfig = null;
	SchemasRecordsServices schemaRecordService;
	RecordServices recordServices;
	ThesaurusManager thesaurusManager;
	ThesaurusService tempThesarusService = null;

	boolean isInStateToBeSaved;

	public ThesaurusConfigurationPresenter(ThesaurusConfigurationView view) {
		super(view);

		SearchServices searchService = modelLayerFactory.newSearchServices();
		recordServices = modelLayerFactory.newRecordServices();
		schemaRecordService = new SchemasRecordsServices(collection, modelLayerFactory);
		List<Record> thesaurusConfigRecordFound = searchService.cachedSearch(new LogicalSearchQuery(LogicalSearchQueryOperators
				.from(schemaRecordService.thesaurusConfig.schemaType()).returnAll()));

		if (thesaurusConfigRecordFound != null && thesaurusConfigRecordFound.size() == 1) {
			thesaurusConfig = schemaRecordService.wrapThesaurusConfig(thesaurusConfigRecordFound.get(0));
		}

		thesaurusManager = modelLayerFactory.getThesaurusManager();
	}

	public boolean isThesaurusConfiguration() {
		if (thesaurusConfig == null) {
			return false;
		} else {
			if (thesaurusConfig.getContent() == null) {
				return false;
			}
		}
		return true;
	}

	public void thesaurusFileUploaded(TempFileUpload tempFileUpload) {
		boolean isNew = false;
		IOServices ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();

		InputStream inputStreamFromFile = null;
		InputStream inputStream2FromFile = null;
		try {
			inputStreamFromFile = ioServices
					.newFileInputStream(tempFileUpload.getTempFile(), THESAURUS_CONFIGURATION_PRESENTER_STREAM_NAME);
			thesaurusManager.set(inputStreamFromFile, view.getCollection());

			if (thesaurusConfig == null) {
				thesaurusConfig = schemaRecordService.newThesaurusConfig();
				isNew = true;
			}
			inputStream2FromFile = ioServices
					.newFileInputStream(tempFileUpload.getTempFile(), THESAURUS_CONFIGURATION_PRESENTER_STREAM_NAME);

			ContentManager contentManager = modelLayerFactory.getContentManager();
			ContentVersionDataSummary contentVersionDataSummary = upload(inputStream2FromFile, THESAURUS_FILE, contentManager);
			ioServices.closeQuietly(inputStream2FromFile);
			Content content = contentManager.createMajor(getCurrentUser(), THESAURUS_FILE, contentVersionDataSummary);

			thesaurusManager.get(collection).setDeniedTerms(thesaurusConfig.getDenidedWords());
			thesaurusConfig.setContent(content);
			if (isNew) {
				recordServices.add(thesaurusConfig);
			} else {
				recordServices.update(thesaurusConfig);
			}

			modelLayerFactory.getContentManager().getContentDao()
					.moveFileToVault(contentVersionDataSummary.getHash(), tempFileUpload.getTempFile(), ONLY_IF_INEXISTING);
			view.setSKOSSaveButtonEnabled(false);
		} catch (IOException e) {
			e.printStackTrace();
			view.showErrorMessage($("ThesaurusConfigurationView.errorWhileSavingThesaurusFile"));
		} catch (RecordServicesException e) {
			e.printStackTrace();
			view.showErrorMessage($("ThesaurusConfigurationView.errorWhileSavingThesaurusFile"));
		} catch (ThesaurusInvalidFileFormat thesaurusInvalidFileFormat) {
			// Not suppose to happen because the file have been validated when uploaded.
			thesaurusInvalidFileFormat.printStackTrace();
			view.showErrorMessage($("ThesaurusConfigurationView.errorWhileSavingThesaurusFile"));
		} finally {
			ioServices.closeQuietly(inputStreamFromFile);
			ioServices.closeQuietly(inputStream2FromFile);
		}
		tempFileUpload.delete();
		Page.getCurrent().reload();
	}

	private ContentVersionDataSummary upload(InputStream resource, String fileName, ContentManager contentManager) {
		return contentManager.upload(resource, new ContentManager.UploadOptions(fileName)).getContentVersionDataSummary();
	}

	public void deleteSkosFileButtonClicked() {
		thesaurusConfig.setContent(null);
		try {
			recordServices.update(thesaurusConfig);
			Page.getCurrent().reload();
		} catch (RecordServicesException e) {
			e.printStackTrace();
			view.showErrorMessage($("ThesaurusConfigurationView.errorWhileSavingThesaurusFile"));
		}
	}

	public void valueChangeInFileSelector(TempFileUpload tempFileUpload) {
		if (tempFileUpload == null) {
			isInStateToBeSaved = false;
			view.setSKOSSaveButtonEnabled(false);
			return;
		}

		try {
			tempThesarusService = ThesaurusServiceBuilder.getThesaurus(new FileInputStream(tempFileUpload.getTempFile()));
			isInStateToBeSaved = true;
			view.setSKOSSaveButtonEnabled(true);
			view.loadDescriptionFieldsWithFileValue();
		} catch (FileNotFoundException e) {
			isInStateToBeSaved = false;
			view.setSKOSSaveButtonEnabled(false);
			e.printStackTrace();
			throw new RuntimeException("ThesaurusConfigurationPresenter - Internal Error", e);
		} catch (ThesaurusInvalidFileFormat thesaurusInvalidFileFormat) {
			isInStateToBeSaved = false;
			view.setSKOSSaveButtonEnabled(false);
			view.toNoThesaurusAvailable();
			view.showErrorMessage($("ThesaurusConfigurationView.errorInvalidFileFormat"));
			view.removeAllTheSelectedFile();
		}
	}

	public ContentVersionVO getContentVersionForDownloadLink() {
		if (isThesaurusConfiguration()) {
			return new RecordToVOBuilder()
					.build(thesaurusConfig.getWrappedRecord(), RecordVO.VIEW_MODE.FORM, view.getSessionContext())
					.get(ThesaurusConfig.CONTENT);
		}
		throw new IllegalStateException("A thesaurusFile need to be avalible when calling this method");
	}

	public void saveRejectedTermsButtonClicked(String rejectedTerms)
			throws RecordServicesException {
		String[] termsPerLine = rejectedTerms.split("[\\r\\n]+");

		List<String> termsPerLineAsList = Arrays.asList(termsPerLine);
		boolean isNew = false;
		if (thesaurusConfig == null) {
			thesaurusConfig = schemaRecordService.newThesaurusConfig();
			isNew = true;
		}

		thesaurusConfig.setDenidedWords(termsPerLineAsList);

		if (isNew) {
			recordServices.add(thesaurusConfig);
		} else {
			recordServices.update(thesaurusConfig);
		}

		ThesaurusService thesarusService = thesaurusManager.get(collection);
		if (thesarusService != null) {
			thesarusService.setDeniedTerms(termsPerLineAsList);
		}
		view.showMessage($("ThesaurusConfigurationView.rejectedTermsSaved"));
	}

	public String getRejectedTerms() {
		String currentRejectedTerms = "";

		if (thesaurusConfig != null) {
			StringBuilder stringBuilder = new StringBuilder();

			for (String denidedWord : thesaurusConfig.getDenidedWords()) {
				stringBuilder.append(denidedWord).append("\n");
			}

			currentRejectedTerms = stringBuilder.toString();
		}

		return currentRejectedTerms;
	}

	public boolean isInStateToBeSaved() {
		return isInStateToBeSaved;
	}

	public String getAbout() {
		ThesaurusService thesaurusService;
		if (isInStateToBeSaved) {
			return tempThesarusService.getRdfAbout();
		} else if ((thesaurusService = thesaurusManager.get(collection)) != null) {
			return thesaurusService.getRdfAbout();
		}
		return "";
	}

	public String getTitle() {
		if (isInStateToBeSaved) {
			return tempThesarusService.getDcTitle();
		}

		ThesaurusService thesaurusService;
		if ((thesaurusService = thesaurusManager.get(collection)) != null) {
			return thesaurusService.getDcTitle();
		}
		return "";
	}

	public String getDescription() {
		if (isInStateToBeSaved) {
			return tempThesarusService.getDcDescription();
		}

		ThesaurusService thesaurusService;
		if ((thesaurusService = thesaurusManager.get(collection)) != null) {
			return thesaurusService.getDcDescription();
		}
		return "";
	}

	public String getDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		if (isInStateToBeSaved && tempThesarusService.getDcDate() != null) {
			return sdf.format(tempThesarusService.getDcDate());
		}

		ThesaurusService thesaurusService;
		if ((thesaurusService = thesaurusManager.get(collection)) != null && thesaurusService.getDcDate() != null) {
			return sdf.format(thesaurusService.getDcDate());
		}
		return "";
	}

	public String getCreator() {
		if (isInStateToBeSaved) {
			return tempThesarusService.getDcDescription();
		}

		ThesaurusService thesaurusService;
		if ((thesaurusService = thesaurusManager.get(collection)) != null) {
			return thesaurusService.getDcCreator();
		}
		return "";
	}

	int getDocumentsWithAConcept() {
		try {
			ESSchemasRecordsServices es = new ESSchemasRecordsServices(collection, appLayerFactory);
			SearchServices searchService = modelLayerFactory.newSearchServices();
			schemaRecordService = new SchemasRecordsServices(collection, modelLayerFactory);
			LogicalSearchCondition condition = from(es.connectorHttpDocument.schemaType())
					.where(es.connectorHttpDocument.thesaurusMatch()).isNotNull();
			LogicalSearchQuery query = new LogicalSearchQuery(condition);
			query.setNumberOfRows(0);
			SPEQueryResponse response = searchService.query(query);

			return (int) response.getNumFound();
		} catch (com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.NoSuchSchemaType e) {
			return 0;
		}
	}

	int getDocumentsWithoutAConcept() {
		try {
			ESSchemasRecordsServices es = new ESSchemasRecordsServices(collection, appLayerFactory);
			SearchServices searchService = modelLayerFactory.newSearchServices();
			schemaRecordService = new SchemasRecordsServices(collection, modelLayerFactory);
			LogicalSearchCondition condition = from(es.connectorHttpDocument.schemaType())
					.where(es.connectorHttpDocument.thesaurusMatch()).isNull();
			LogicalSearchQuery query = new LogicalSearchQuery(condition);
			query.setNumberOfRows(0);
			SPEQueryResponse response = searchService.query(query);

			return (int) response.getNumFound();
		} catch (com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.NoSuchSchemaType e) {
			return 0;
		}
	}

	int getUsedConcepts() {
		try {
			ESSchemasRecordsServices es = new ESSchemasRecordsServices(collection, appLayerFactory);
			SearchServices searchService = modelLayerFactory.newSearchServices();
			schemaRecordService = new SchemasRecordsServices(collection, modelLayerFactory);
			LogicalSearchCondition condition = from(es.connectorHttpDocument.schemaType()).returnAll();
			LogicalSearchQuery query = new LogicalSearchQuery(condition);
			query.setNumberOfRows(0);
			query.setFieldFacetLimit(-1);
			query.addFieldFacet("thesaurusMatch_ss");

			SPEQueryResponse response = searchService.query(query);
			List<FacetValue> usedConcept = response.getFieldFacetValues().get("thesaurusMatch_ss");

			return usedConcept.size();
		} catch (com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.NoSuchSchemaType e) {
			return 0;
		}
	}

	List<FacetValue> getMostUsedConcepts() {
		try {
			//  résultat de la facette : facet.field=thesaurusMatch_ss facet.limit=1000
			ESSchemasRecordsServices es = new ESSchemasRecordsServices(collection, appLayerFactory);
			SearchServices searchService = modelLayerFactory.newSearchServices();
			schemaRecordService = new SchemasRecordsServices(collection, modelLayerFactory);
			LogicalSearchCondition condition = from(es.connectorHttpDocument.schemaType()).returnAll();
			LogicalSearchQuery query = new LogicalSearchQuery(condition);
			query.setNumberOfRows(0);
			query.setFieldFacetLimit(1000);
			query.addFieldFacet("thesaurusMatch_ss");

			SPEQueryResponse response = searchService.query(query);
			return response.getFieldFacetValues().get("thesaurusMatch_ss");
		} catch (com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.NoSuchSchemaType e) {
			return new ArrayList<>();
		}
	}

	InputStream getMostUsedConceptsInputStream(List<FacetValue> mostUsedConcepts) {
		StringBuilder csv = new StringBuilder();
		csv.append("\"À propos\"");
		csv.append(",");
		csv.append("\"Français (skos:prefLabel)\"");
		csv.append(",");
		csv.append("\"Anglais (skos:prefLabel)\"");
		csv.append(",");
		csv.append("\"Fréquence\"");
		csv.append("\n");

		for (FacetValue concept : mostUsedConcepts) {
			SkosConcept skosConcept = thesaurusManager.get(collection).getSkosConcept(concept.getValue());
			if (skosConcept != null) {
				csv.append("\"" + skosConcept.getRdfAbout() + "\"");
				csv.append(",");
				csv.append("\"" + skosConcept.getPrefLabel(Locale.FRENCH) + "\"");
				csv.append(",");
				csv.append("\"" + skosConcept.getPrefLabel(Locale.ENGLISH) + "\"");
				csv.append(",");
				csv.append("\"" + concept.getQuantity() + "\"");
				csv.append("\n");
			}
		}
		return new ByteArrayInputStream(csv.toString().getBytes(StandardCharsets.ISO_8859_1));
	}

	List<SkosConcept> getUnusedConcepts() {
		try {
			List<SkosConcept> result = new ArrayList<>();
			// Tous les concepts du thesaurusManager MOINS résultat de la facette : facet.field=thesaurusMatch_ss facet.limit=100000
			ESSchemasRecordsServices es = new ESSchemasRecordsServices(collection, appLayerFactory);
			SearchServices searchService = modelLayerFactory.newSearchServices();
			schemaRecordService = new SchemasRecordsServices(collection, modelLayerFactory);
			LogicalSearchCondition condition = from(es.connectorHttpDocument.schemaType()).returnAll();
			LogicalSearchQuery query = new LogicalSearchQuery(condition);
			query.setNumberOfRows(0);
			query.setFieldFacetLimit(-1);
			query.addFieldFacet("thesaurusMatch_ss");

			SPEQueryResponse response = searchService.query(query);
			List<FacetValue> usedConcept = response.getFieldFacetValues().get("thesaurusMatch_ss");


			ThesaurusService thesaurusService = thesaurusManager.get(collection);
			if (thesaurusService != null) {
				Map<String, SkosConcept> allConcept = thesaurusService.getAllConcepts();

				for (FacetValue value : usedConcept) {
					allConcept.remove(value.getValue());
				}

				for (String conceptKey : allConcept.keySet()) {
					SkosConcept concept = allConcept.get(conceptKey);
					result.add(concept);
				}
			}
			return result;
		} catch (com.constellio.model.entities.schemas.MetadataSchemasRuntimeException.NoSuchSchemaType e) {
			return new ArrayList<>();
		}
	}

	public InputStream getUnusedConceptsInputStream(final List<SkosConcept> unusedConcepts) {
		StringBuilder csv = new StringBuilder();
		csv.append("\"À propos\"");
		csv.append(",");
		csv.append("\"Français (skos:prefLabel)\"");
		csv.append(",");
		csv.append("\"Anglais (skos:prefLabel)\"");
		csv.append("\n");

		for (SkosConcept unusedConcept : unusedConcepts) {
			csv.append("\"" + unusedConcept.getRdfAbout() + "\"");
			csv.append(",");
			csv.append("\"" + unusedConcept.getPrefLabel(Locale.FRENCH) + "\"");
			csv.append(",");
			csv.append("\"" + unusedConcept.getPrefLabel(Locale.ENGLISH) + "\"");
			csv.append("\n");
		}
		return new ByteArrayInputStream(csv.toString().getBytes(StandardCharsets.ISO_8859_1));
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return true;
	}

}
