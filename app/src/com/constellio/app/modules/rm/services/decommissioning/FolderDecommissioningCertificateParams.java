package com.constellio.app.modules.rm.services.decommissioning;

import com.constellio.app.modules.rm.reports.model.decommissioning.DocumentsCertificateReportModel;
import com.constellio.app.modules.rm.reports.model.decommissioning.FoldersCertificateReportModel;

public class FolderDecommissioningCertificateParams {

	FoldersCertificateReportModel reportModel;

	public FolderDecommissioningCertificateParams(
			FoldersCertificateReportModel reportModel) {
		this.reportModel = reportModel;
	}

	public FoldersCertificateReportModel getReportModel() {
		return reportModel;
	}
}
