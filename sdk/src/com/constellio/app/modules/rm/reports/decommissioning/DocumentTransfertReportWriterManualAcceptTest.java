package com.constellio.app.modules.rm.reports.decommissioning;

import java.util.Arrays;

import org.junit.Test;

import com.constellio.app.modules.rm.reports.builders.decommissioning.DocumentTransfertReportWriter;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentReportModel;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentReportModel.DocumentTransfertModel_Calendar;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentReportModel.DocumentTransfertModel_Document;
import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentReportModel.DocumentTransfertModel_Identification;
import com.constellio.app.modules.rm.reports.model.decommissioning.ReportBooleanField;
import com.constellio.app.reports.builders.administration.plan.ReportBuilderTestFramework;
import com.constellio.data.utils.TimeProvider;

public class DocumentTransfertReportWriterManualAcceptTest extends ReportBuilderTestFramework {

	@Test
	public void verify_report_has_one_page_and_informations_in_all_fields() {

		DocumentReportModel model = new DocumentReportModel();

		createCalendar(model);
		createIdentification(model);
		createDocumentList(model, 80);

		build(new DocumentTransfertReportWriter(model, getIOLayerFactory().newIOServices(),
				getModelLayerFactory().getFoldersLocator()));
	}

	private void createCalendar(DocumentReportModel model) {
		DocumentTransfertModel_Calendar calendarModel = model.getCalendarModel();

		calendarModel.setCalendarNumber("004-005");
		calendarModel.setRuleNumber("006");
		calendarModel.setSemiActiveRange("30");
		calendarModel.setDispositionYear("2019");
		calendarModel.setConservationDisposition(
				Arrays.asList(new ReportBooleanField("Destruction -> D", true),
						new ReportBooleanField("Tri/T", false),
						new ReportBooleanField("Conservation -> C", false)));
		calendarModel.setSupports(
				Arrays.asList(
						new ReportBooleanField("DM", false),
						new ReportBooleanField("PA", true),
						new ReportBooleanField("AA", true),
						new ReportBooleanField("BB", true),
						new ReportBooleanField("CC", true)

				));

		calendarModel.setQuantity("1");
		calendarModel.setExtremeDate("2004-2005");
	}

	private void createIdentification(DocumentReportModel model) {
		DocumentTransfertModel_Identification identificationModel = model.getIdentificationModel();
		identificationModel.setSentDate(TimeProvider.getLocalDateTime().toString());
		identificationModel.setBoxNumber("Transf2014-899-01");
		identificationModel.setOrganisationName("MSE");
		identificationModel.setPublicOrganisationNumber("001");
		identificationModel.setAdministrationAddress("230 rue du Minist�re \n Local 20304 20eme etage \n Departement MS local 3");
		identificationModel.setResponsible("Dakota Lindien");
		identificationModel.setFunction("Charg� de projet");
		identificationModel.setEmail("dakota.lindien@mse.gc.ca");
		identificationModel.setPhoneNumber("111-222-3333");
	}

	private void createDocumentList(DocumentReportModel model, int numberDocument) {

		DocumentTransfertModel_Document docModel = new DocumentTransfertModel_Document();

		docModel.setCode("14 3100");
		docModel.setDelayNumber("");
		docModel.setReferenceId("80");
		docModel.setTitle("Dossier papier 24");
		docModel.setStartingYear("2004");
		docModel.setEndingYear("2005");

		for (int index = 0; index < numberDocument; ++index) {
			model.addDocument(docModel);
		}
	}
}



