package com.constellio.app.modules.rm.reports.model.decommissioning;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.constellio.data.io.streamFactories.StreamFactory;

public class DocumentReportModel {

	private StreamFactory<InputStream> headerLogo;

	private List<DocumentTransfertModel_Document> documentList = new ArrayList<>();

	private DocumentTransfertModel_Calendar calendarModel = new DocumentTransfertModel_Calendar();

	private DocumentTransfertModel_Identification identificationModel = new DocumentTransfertModel_Identification();

	private String printDate = "";

	public StreamFactory<InputStream> getHeaderLogo() {
		return headerLogo;
	}

	public DocumentReportModel setHeaderLogo(StreamFactory<InputStream> headerLogo) {
		this.headerLogo = headerLogo;
		return this;
	}

	public List<DocumentTransfertModel_Document> getDocumentList() {
		return Collections.unmodifiableList(documentList);
	}

	public void addDocument(DocumentTransfertModel_Document document) {
		documentList.add(document);
	}

	public DocumentTransfertModel_Calendar getCalendarModel() {
		return calendarModel;
	}

	public DocumentTransfertModel_Identification getIdentificationModel() {
		return identificationModel;
	}

	public String getPrintDate() {
		return printDate;
	}

	public void setPrintDate(String printDate) {
		this.printDate = printDate;
	}

	public static class DocumentTransfertModel_Calendar {
		private String calendarNumber;

		private String ruleNumber;

		private String semiActiveRange;

		private String dispositionYear;

		private List<ReportBooleanField> conservationDisposition;

		private List<ReportBooleanField> supports;

		private String quantity;

		private String extremeDate;

		public String getCalendarNumber() {
			return calendarNumber;
		}

		public String getRuleNumber() {
			return ruleNumber;
		}

		public String getSemiActiveRange() {
			return semiActiveRange;
		}

		public String getDispositionYear() {
			return dispositionYear;
		}

		public List<ReportBooleanField> getConservationDisposition() {
			return Collections.unmodifiableList(conservationDisposition);
		}

		public List<ReportBooleanField> getSupports() {
			return Collections.unmodifiableList(supports);
		}

		public String getQuantity() {
			return quantity;
		}

		public String getExtremeDate() {
			return extremeDate;
		}

		public DocumentTransfertModel_Calendar setCalendarNumber(String calendarNumber) {
			this.calendarNumber = calendarNumber;
			return this;
		}

		public DocumentTransfertModel_Calendar setRuleNumber(String ruleNumber) {
			this.ruleNumber = ruleNumber;
			return this;
		}

		public DocumentTransfertModel_Calendar setSemiActiveRange(String semiActiveRange) {
			this.semiActiveRange = semiActiveRange;
			return this;
		}

		public DocumentTransfertModel_Calendar setDispositionYear(String dispositionYear) {
			this.dispositionYear = dispositionYear;
			return this;
		}

		public DocumentTransfertModel_Calendar setConservationDisposition(
				List<ReportBooleanField> conservationDisposition) {
			this.conservationDisposition = conservationDisposition;
			return this;
		}

		public DocumentTransfertModel_Calendar setSupports(List<ReportBooleanField> supports) {
			this.supports = supports;
			return this;
		}

		public DocumentTransfertModel_Calendar setQuantity(String quantity) {
			this.quantity = quantity;
			return this;
		}

		public DocumentTransfertModel_Calendar setExtremeDate(String extremeDate) {
			this.extremeDate = extremeDate;
			return this;
		}
	}

	public static class DocumentTransfertModel_Identification {
		private String sentDate;

		private String boxNumber;

		private String organisationName;

		private String publicOrganisationNumber;

		private String administrationAddress;

		private String responsible;

		private String function;

		private String phoneNumber;

		private String email;

		public String getSentDate() {
			return sentDate;
		}

		public String getBoxNumber() {
			return boxNumber;
		}

		public String getOrganisationName() {
			return organisationName;
		}

		public String getPublicOrganisationNumber() {
			return publicOrganisationNumber;
		}

		public String getAdministrationAddress() {
			return administrationAddress;
		}

		public String getResponsible() {
			return responsible;
		}

		public String getFunction() {
			return function;
		}

		public String getPhoneNumber() {
			return phoneNumber;
		}

		public String getEmail() {
			return email;
		}

		public DocumentTransfertModel_Identification setSentDate(String sentDate) {
			this.sentDate = sentDate;
			return this;
		}

		public DocumentTransfertModel_Identification setBoxNumber(String boxNumber) {
			this.boxNumber = boxNumber;
			return this;
		}

		public DocumentTransfertModel_Identification setOrganisationName(String organisationName) {
			this.organisationName = organisationName;
			return this;
		}

		public DocumentTransfertModel_Identification setPublicOrganisationNumber(String publicOrganisationNumber) {
			this.publicOrganisationNumber = publicOrganisationNumber;
			return this;
		}

		public DocumentTransfertModel_Identification setAdministrationAddress(String administrationAddress) {
			this.administrationAddress = administrationAddress;
			return this;
		}

		public DocumentTransfertModel_Identification setResponsible(String responsible) {
			this.responsible = responsible;
			return this;
		}

		public DocumentTransfertModel_Identification setFunction(String function) {
			this.function = function;
			return this;
		}

		public DocumentTransfertModel_Identification setPhoneNumber(String phoneNumber) {
			this.phoneNumber = phoneNumber;
			return this;
		}

		public DocumentTransfertModel_Identification setEmail(String email) {
			this.email = email;
			return this;
		}
	}

	public static class DocumentTransfertModel_Document {

		private String code;

		private String delayNumber;

		private String referenceId;

		private String title;

		private String startingYear;

		private String endingYear;

		private String restrictionYear;

		public String getCode() {
			return code;
		}

		public String getDelayNumber() {
			return this.delayNumber;
		}

		public String getTitle() {
			return this.title;
		}

		public String getStartingYear() {
			return this.startingYear;
		}

		public String getEndingYear() {
			return this.endingYear;
		}

		public String getRestrictionYear() {
			return this.restrictionYear;
		}

		public String getReferenceId() {
			return this.referenceId;
		}

		public DocumentTransfertModel_Document setTitle(String title) {
			this.title = title;
			return this;
		}

		public DocumentTransfertModel_Document setCode(String code) {
			this.code = code;
			return this;
		}

		public DocumentTransfertModel_Document setDelayNumber(String delayNumber) {
			this.delayNumber = delayNumber;
			return this;
		}

		public DocumentTransfertModel_Document setReferenceId(String referenceId) {
			this.referenceId = referenceId;
			return this;
		}

		public DocumentTransfertModel_Document setStartingYear(String startingYear) {
			this.startingYear = startingYear;
			return this;
		}

		public DocumentTransfertModel_Document setEndingYear(String endingYear) {
			this.endingYear = endingYear;
			return this;
		}

		public DocumentTransfertModel_Document setRestrictionYear(String restrictionYear) {
			this.restrictionYear = restrictionYear;
			return this;
		}
	}

	public void setDocumentList(List<DocumentTransfertModel_Document> documents) {
		this.documentList = documents;
	}

	public void setCalendarModel(DocumentTransfertModel_Calendar calendarModel) {
		this.calendarModel = calendarModel;
	}

	public void setIdentificationModel(DocumentTransfertModel_Identification identificationModel) {
		this.identificationModel = identificationModel;
	}

}
