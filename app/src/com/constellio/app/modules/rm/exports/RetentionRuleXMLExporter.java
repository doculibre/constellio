package com.constellio.app.modules.rm.exports;

import com.constellio.app.modules.rm.exports.RetentionRuleXMLExporterRuntimeException.RetentionRuleXMLExporterRuntimeException_InvalidFile;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.data.io.services.facades.FileService;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.constellio.model.entities.schemas.Schemas.CODE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class RetentionRuleXMLExporter {

	private static final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";

	List<RetentionRule> rules;

	File exportFile;

	String collection;

	ModelLayerFactory modelLayerFactory;

	RMSchemasRecordsServices rm;

	public RetentionRuleXMLExporter(List<RetentionRule> rules, File exportFile, String collection,
									ModelLayerFactory modelLayerFactory) {
		this.rules = rules;
		this.exportFile = exportFile;
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	public void run() {

		Document document = new Document();
		Element rowset = new Element("ROWSET");
		document.setRootElement(rowset);

		for (RetentionRule rule : rules) {

			Element row = new Element("ROW");
			rowset.addContent(row);

			row.addContent(new Element("ID_ORGANISME").setText(getCollectionOragnizationNumber()));
			row.addContent(new Element("NUMREGLE").setText(rule.getCode()));

			//can have A, M or S values (Ajout, Modifier, Supprimer)
			row.addContent(new Element("TYPE_TRANS").setText("A"));
			row.addContent(new Element("RECUEIL").setText(rule.getCorpus()));
			row.addContent(new Element("TITRESER").setText(rule.getTitle()));
			row.addContent(new Element("REFLEGALE").setText(rule.getJuridicReference()));
			row.addContent(new Element("NO_RECUEIL").setText(rule.getCorpusRuleNumber()));
			row.addContent(new Element("PROCACTIVI").setText(getCategoriesTitlesToString(rule)));
			row.addContent(new Element("CODECLASS").setText(getCategoriesCodeToString(rule)));
			row.addContent(new Element("NOMUNITE").setText(getAdministrativesUnitsNames(rule)));
			row.addContent(new Element("DESCSERIE").setText(rule.getDescription()));
			row.addContent(new Element("TYPESDOC").setText(getDocumentTypesToString(rule)));
			row.addContent(new Element("DOCUMESSEN").setText(isDocumentsEssentialToString(rule)));
			row.addContent(new Element("DOCUMCONFI").setText(isDocumentConfidentialToString(rule)));
			row.addContent(new Element("REMARQDELA").setText(getCopyRetentionRulesCommentsToString(rule)));
			row.addContent(new Element("REMARQGEN").setText(rule.getGeneralComment()));
			row.addContent(new Element("GRILLECHAN").setText("N"));

			Element delai = new Element("DELAI");
			row.addContent(delai);

			List<CopyRetentionRule> allCopyRetentionRules = rule.getCopyRetentionRules();

			for (CopyRetentionRule currentCopyRetentionRule : allCopyRetentionRules) {

				Element delaiRow = new Element("DELAI_ROW");
				delai.addContent(delaiRow);

				delaiRow.addContent(new Element("NUMDELAI"));
				delaiRow.addContent(new Element("NUMREGLE").setText(currentCopyRetentionRule.getCode()));
				delaiRow.addContent(new Element("TYPEDOSS").setText(getCopyTypeToString(currentCopyRetentionRule)));
				delaiRow.addContent(new Element("SUPPDOSS").setText(getMediumTypesCodesToString(currentCopyRetentionRule)));

				if (currentCopyRetentionRule.getContentTypesComment() != null) {
					delaiRow.addContent(new Element("REM_SUPPDOSS").setText(currentCopyRetentionRule.getContentTypesComment()));
				}

				delaiRow.addContent(
						new Element("PERIOACTIF").setText(getActiveRetentionPeriodToString(currentCopyRetentionRule)));

				if (currentCopyRetentionRule.getActiveRetentionComment() != null) {
					delaiRow.addContent(
							new Element("REM_PERIOACTIF").setText(currentCopyRetentionRule.getActiveRetentionComment()));
				}

				delaiRow.addContent(
						new Element("PERIOSMACT").setText(getSemiActiveRetentionPeriodToString(currentCopyRetentionRule)));

				if (currentCopyRetentionRule.getSemiActiveRetentionComment() != null) {
					delaiRow.addContent(
							new Element("REM_PERIOSMACT").setText(currentCopyRetentionRule.getSemiActiveRetentionComment()));
				}

				delaiRow.addContent(
						new Element("ID_REF_DISPOSITION").setText(getInactiveDisposalTypeCode(currentCopyRetentionRule)));

				if (currentCopyRetentionRule.getInactiveDisposalComment() != null) {
					delaiRow.addContent(
							new Element("REM_DISPOINACT").setText(currentCopyRetentionRule.getInactiveDisposalComment()));
				}
			}

		}

		XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
		try {
			FileService fileService = modelLayerFactory.getIOServicesFactory().newFileService();
			fileService.replaceFileContent(exportFile, xmlOutput.outputString(document));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String getCollectionOragnizationNumber() {
		Collection collection = rm.getCollection(this.collection);
		return collection != null ? collection.getOrganizationNumber() : null;
	}

	private String getAdministrativesUnitsNames(RetentionRule rule) {

		if (rule.isResponsibleAdministrativeUnits()) {
			return "unités administratives responsables";
		}

		List<AdministrativeUnit> allAdministrativesUnits = rm.getAdministrativeUnits(rule.getAdministrativeUnits());

		Collections.sort(allAdministrativesUnits, new Comparator<AdministrativeUnit>() {
			@Override
			public int compare(AdministrativeUnit o1, AdministrativeUnit o2) {
				return o1.getCode().compareTo(o2.getCode());
			}
		});

		String administrativesUnitsNamesString = "";

		for (AdministrativeUnit anAdministrativeUnit : allAdministrativesUnits) {
			if (administrativesUnitsNamesString == "") {
				administrativesUnitsNamesString = anAdministrativeUnit.getTitle();
			} else {
				administrativesUnitsNamesString = administrativesUnitsNamesString + "; " + anAdministrativeUnit.getTitle();
			}
		}
		return administrativesUnitsNamesString;
	}

	private String getInactiveDisposalTypeCode(CopyRetentionRule copyRetentionRule) {
		String inactiveDisposalTypeCode = copyRetentionRule.getInactiveDisposalType().getCode();
		if (inactiveDisposalTypeCode.isEmpty()) {
			return "--";
		}
		return inactiveDisposalTypeCode;
	}

	private String getSemiActiveRetentionPeriodToString(CopyRetentionRule copyRetentionRule) {
		String semiActiveRetentionPeriodToString = copyRetentionRule.getSemiActiveRetentionPeriod().toString();
		if (semiActiveRetentionPeriodToString.isEmpty()) {
			return "--";
		}
		return semiActiveRetentionPeriodToString;
	}

	private String getActiveRetentionPeriodToString(CopyRetentionRule copyRetentionRule) {
		String activeRetentionPeriodToString = copyRetentionRule.getActiveRetentionPeriod().toString();
		if (activeRetentionPeriodToString.isEmpty()) {
			return "--";
		}
		return activeRetentionPeriodToString;
	}

	private String getCopyTypeToString(CopyRetentionRule copyRetentionRule) {
		String copyTypeToString = copyRetentionRule.getCopyType().getCode();
		if (copyTypeToString.isEmpty()) {
			return "--";
		}
		return copyTypeToString;
	}

	private String isDocumentsEssentialToString(RetentionRule rule) {
		if (rule.isEssentialDocuments()) {
			return "O";
		}
		return "N";
	}

	private String isDocumentConfidentialToString(RetentionRule rule) {
		if (rule.isConfidentialDocuments()) {
			return "O";
		}
		return "N";
	}

	private String getMediumTypesCodesToString(CopyRetentionRule currentCopyRetentionRule) {
		List<String> mediumTypesIds = currentCopyRetentionRule.getMediumTypeIds();
		List<MediumType> mediumTypesCodes = rm.getMediumTypes(mediumTypesIds);
		String mediumTypeCode = "";

		Collections.sort(mediumTypesCodes, new Comparator<MediumType>() {
			@Override
			public int compare(MediumType o1, MediumType o2) {
				return o1.getCode().compareTo(o2.getCode());
			}
		});

		for (MediumType aMediumType : mediumTypesCodes) {
			if (mediumTypeCode == "") {
				mediumTypeCode = aMediumType.getCode().toLowerCase();
			} else {
				mediumTypeCode = mediumTypeCode + ", " + aMediumType.getCode().toLowerCase();
			}
		}
		return mediumTypeCode;
	}

	private String getCopyRetentionRulesCommentsToString(RetentionRule rule) {
		List<String> copyRetentionRules = rule.getCopyRulesComment();
		String copyRetentionRulesComments = "";

		for (String aCopyRetentionRule : copyRetentionRules) {
			if (copyRetentionRulesComments == "") {
				copyRetentionRulesComments = aCopyRetentionRule;
			} else {
				copyRetentionRulesComments = copyRetentionRulesComments + "; " + aCopyRetentionRule;
			}
		}
		return copyRetentionRulesComments;
	}

	private String getDocumentTypesToString(RetentionRule rule) {
		List<String> docTypesId = rule.getDocumentTypes();
		String docTypes = "";

		for (String aDocType : docTypesId) {
			if (docTypes == "") {
				docTypes = rm.getDocumentType(aDocType).getTitle();
			} else {
				docTypes = docTypes + "; " + rm.getDocumentType(aDocType).getTitle();
			}
		}
		return docTypes;
	}

	private String getCategoriesTitlesToString(RetentionRule rule) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		List<Record> records = searchServices.search(new LogicalSearchQuery()
				.setCondition(from(rm.category.schemaType()).where(rm.category.retentionRules()).isEqualTo(rule)).sortAsc(CODE));

		List<Category> categories = rm.wrapCategorys(records);
		String categoriesTitles = "";

		for (Category zeCategory : categories) {

			if (categoriesTitles == "") {
				categoriesTitles = zeCategory.getTitle();
			} else {
				categoriesTitles = categoriesTitles + "; " + zeCategory.getTitle();
			}
		}
		return categoriesTitles;
	}

	private String getCategoriesCodeToString(RetentionRule rule) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		List<Record> records = searchServices.search(new LogicalSearchQuery()
				.setCondition(from(rm.category.schemaType()).where(rm.category.retentionRules()).isEqualTo(rule)).sortAsc(CODE));

		List<Category> categories = rm.wrapCategorys(records);
		String categoriesCodes = "";

		for (Category zeCategory : categories) {

			if (categoriesCodes == "") {
				categoriesCodes = zeCategory.getCode();
			} else {
				categoriesCodes = categoriesCodes + "; " + zeCategory.getCode();
			}
		}
		return categoriesCodes;
	}

	public static RetentionRuleXMLExporter forAllApprovedRulesInCollection(String collection,
																		   File exportFile,
																		   ModelLayerFactory modelLayerFactory) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		List<Record> records = searchServices.search(new LogicalSearchQuery(
				from(rm.retentionRule.schemaType())
						.where(rm.retentionRule.approved()).isTrue()
						.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull()).sortAsc(CODE));

		return new RetentionRuleXMLExporter(rm.wrapRetentionRules(records), exportFile, collection, modelLayerFactory);
	}

	public static RetentionRuleXMLExporter forAllRulesInCollection(String collection,
																   File exportFile,
																   ModelLayerFactory modelLayerFactory) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		List<Record> records = searchServices.search(new LogicalSearchQuery(
				from(rm.retentionRule.schemaType())
						.where(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull()).sortAsc(CODE));

		return new RetentionRuleXMLExporter(rm.wrapRetentionRules(records), exportFile, collection, modelLayerFactory);
	}

	public static void validate(File xmlFile) {

		try {

			File schemaFile = new File(new FoldersLocator().getModuleResourcesFolder("rm"), "RetentionRuleExport.xsd");
			Source xmlFileSource = new StreamSource(xmlFile);
			SchemaFactory schemaFactory = SchemaFactory
					.newInstance(W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(schemaFile);
			Validator validator = schema.newValidator();
			validator.validate(xmlFileSource);

		} catch (SAXException | IOException e) {
			String content = "";

			try {
				content = FileUtils.readFileToString(xmlFile);
			} catch (IOException e2) {
				e.printStackTrace();
			}

			throw new RetentionRuleXMLExporterRuntimeException_InvalidFile(content, e);
		}
	}
}
