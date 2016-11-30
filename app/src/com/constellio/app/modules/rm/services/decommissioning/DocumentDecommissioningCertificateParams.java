package com.constellio.app.modules.rm.services.decommissioning;

import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentsCertificateReportModel;

public class DocumentDecommissioningCertificateParams {

	DocumentsCertificateReportModel reportModel;

	public DocumentDecommissioningCertificateParams(
			DocumentsCertificateReportModel reportModel) {
		this.reportModel = reportModel;
	}

	public DocumentsCertificateReportModel getReportModel() {
		return reportModel;
	}
}
