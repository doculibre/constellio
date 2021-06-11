package com.constellio.app.modules.rm.services.reports.printable;

import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.jgoodies.common.base.Strings;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRXmlUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.tika.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.services.records.GetRecordOptions.RETURNING_SUMMARY;
import static java.util.Arrays.asList;

class ReportPrintableGenerator extends PrintableGenerator {

	public ReportPrintableGenerator(String collection, AppLayerFactory appLayerFactory) {
		super(collection, appLayerFactory);
	}

	@Override
	public InputStream generate(PrintableGeneratorParams params) throws Exception {
		InputStream jasperReportInputStream = null;
		try {
			PrintableReport printableReport = rm.getPrintableReport(params.getPrintableId());

			if (CollectionUtils.isEmpty(printableReport.getJasperSubreportFiles())) {
				params.setIgnoreReferences(true);
			}
			if (printableReport.getDepth() != null) {
				params.setDepth(printableReport.getDepth());
			}
			if (printableReport.getAddChildren() || printableReport.getAddParents()) {
				params.setRecordIds(addHierarchyRecords(params.getRecordIds(), printableReport, params.getSchemaType()));
			}
			if (printableReport.isOptimized()) {
				JasperReport jasperReport = getJasperReport(printableReport.get(Printable.JASPERFILE));
				Set<String> requiredFields = getRequiredFields(jasperReport);
				if (printableReport.getJasperSubreportFiles() != null) {
					for (Content subreportContent : printableReport.getJasperSubreportFiles()) {
						JasperReport jasperSubreport = getJasperReport(subreportContent);
						requiredFields.addAll(getRequiredFields(jasperSubreport));
					}
				}
				params.setRequiredMetadataCodes(requiredFields);
			}

			org.w3c.dom.Document document = JRXmlUtils.parse(new ByteArrayInputStream(generateXml(params).getBytes(StandardCharsets.UTF_8)));
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, document);

			if (printableReport.getJasperSubreportFiles() != null) {
				for (int i = 0; i < printableReport.getJasperSubreportFiles().size(); i++) {
					Content jasperSubreportFileContent = printableReport.getJasperSubreportFiles().get(i);
					try (InputStream jasperSubreportInputStream = contentManager.getContentInputStream(jasperSubreportFileContent.getCurrentVersion().getHash(),
							getClass().getSimpleName())) {
						JasperReport subreportJasper = (JasperReport) JRLoader.loadObject(jasperSubreportInputStream);
						parameters.put("subreportParameter" + (i + 1), subreportJasper);
					}
				}
			}
			Content jasperFileContent = printableReport.get(Printable.JASPERFILE);
			jasperReportInputStream = contentManager.getContentInputStream(jasperFileContent.getCurrentVersion().getHash(), getClass().getSimpleName());
			return generate(params.getPrintableExtension(), jasperReportInputStream, parameters);
		} finally {
			IOUtils.closeQuietly(jasperReportInputStream);
		}
	}

	private Set<String> getRequiredFields(JasperReport jasperReport) {
		try {
			Set<String> fields = new HashSet<>();
			List<JRDataset> datasets = new ArrayList<>();
			datasets.add(jasperReport.getMainDataset());
			if (jasperReport.getDatasets() != null) {
				datasets.addAll(asList(jasperReport.getDatasets()));
			}
			datasets.forEach(dataset -> {
				if (dataset.getFields() != null) {
					Arrays.stream(dataset.getFields()).forEach(field -> {
						String description = field.getDescription();
						if (Strings.isNotBlank(description)) {
							Arrays.stream(description.split("/"))
									.filter(s -> s.length() > 0 && Character.isLowerCase(s.charAt(0)))
									.forEach(fields::add);
						}
						String[] names = field.getPropertiesMap().getPropertyNames();
						for (String name : names) {
							fields.add(field.getPropertiesMap().getProperty(name));
						}
					});
				}
				if (dataset.getQuery() != null) {
					String queryText = dataset.getQuery().getText();
					if (Strings.isNotBlank(queryText)) {
						Arrays.stream(queryText.split("/"))
								.filter(s -> s.length() > 0 && Character.isLowerCase(s.charAt(0)))
								.forEach(fields::add);
					}
				}
			});
			return fields;
		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptySet();
		}
	}

	List<String> addHierarchyRecords(List<String> recordIds, PrintableReport printableReport, String schemaType) {
		Set<String> allRecordIds = new HashSet<>(recordIds);
		if (printableReport.getAddChildren()) {
			recordIds.forEach(recordId -> {
				Record record = recordServices.get(recordId, RETURNING_SUMMARY);
				recordHierarchyServices.getAllRecordsInHierarchy(record, true).forEach(childRecord -> {
					if (childRecord.isOfSchemaType(schemaType)) {
						allRecordIds.add(childRecord.getId());
					}
				});
			});
		}
		if (printableReport.getAddParents()) {
			recordIds.forEach(recordId -> {
				Record record = recordServices.get(recordId, RETURNING_SUMMARY);
				List<String> paths = record.get(Schemas.PATH);
				if (CollectionUtils.isNotEmpty(paths)) {
					paths.forEach(path -> {
						List<String> ancestorIds = new ArrayList<>(Arrays.asList(path.substring(1).split("/")));
						ancestorIds.forEach(ancestorId -> {
							Record ancestorRecord = null;
							try {
								ancestorRecord = recordServices.get(ancestorId, RETURNING_SUMMARY);
							} catch (NoSuchRecordWithId ignored) {
							}
							if (ancestorRecord != null && ancestorRecord.isOfSchemaType(schemaType)) {
								allRecordIds.add(ancestorId);
							}
						});
					});
				}
			});
		}
		return new ArrayList<>(allRecordIds);
	}

	public JasperReport getJasperReport(Content jasperReportContent) throws Exception {
		try (InputStream jasperReportInputStream = contentManager.getContentInputStream(
				jasperReportContent.getCurrentVersion().getHash(), getClass().getSimpleName())) {
			return (JasperReport) JRLoader.loadObject(jasperReportInputStream);
		}
	}

}
