package com.constellio.app.api.pdf.signature.config;

import com.constellio.app.modules.rm.RMConfigs.ConversionPdfFormat;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationGroup;
import com.constellio.model.services.configs.SystemConfigurationsManager;

import java.util.ArrayList;
import java.util.List;

public class ESignatureConfigs {

	public static SystemConfiguration SIGNING_KEYSTORE;
	public static SystemConfiguration SIGNING_KEYSTORE_PASSWORD;
	public static SystemConfiguration SIGNED_DOCUMENT_CONSULTATION_DURATION_IN_DAYS;
	public static SystemConfiguration CONVERT_TO_PDFA_WHEN_SIGNING;
	public static SystemConfiguration DISABLE_EXTERNAL_SIGNATURES;

	static {
		SystemConfigurationGroup signature = new SystemConfigurationGroup(null, "signature");
		SIGNING_KEYSTORE = signature.createBinary("signingKeystore");
		SIGNING_KEYSTORE_PASSWORD = signature.createString("signingPassword").whichHasHiddenValue();
		SIGNED_DOCUMENT_CONSULTATION_DURATION_IN_DAYS = signature.createInteger("signedDocumentConsultationDurationDays").withDefaultValue(7);
		CONVERT_TO_PDFA_WHEN_SIGNING = signature.createEnum("convertToPdfaWhenSigning", ConversionPdfFormat.class)
				.withDefaultValue(ConversionPdfFormat.PDF_A);
		DISABLE_EXTERNAL_SIGNATURES = signature.createBooleanFalseByDefault("disableExternalSignatures");
	}

	public static List<SystemConfiguration> getConfigurations(String moduleID) {
		List<SystemConfiguration> configurations = new ArrayList<>();
		configurations.add(SIGNING_KEYSTORE);
		configurations.add(SIGNING_KEYSTORE_PASSWORD);
		configurations.add(SIGNED_DOCUMENT_CONSULTATION_DURATION_IN_DAYS);
		configurations.add(CONVERT_TO_PDFA_WHEN_SIGNING);
		configurations.add(DISABLE_EXTERNAL_SIGNATURES);
		return configurations;
	}

	SystemConfigurationsManager manager;

	public ESignatureConfigs(SystemConfigurationsManager manager) {
		this.manager = manager;
	}
	
	public StreamFactory getKeystore() {
		return SIGNING_KEYSTORE != null ? manager.getValue(SIGNING_KEYSTORE) : null;
	}
	
	public String getKeystorePass() {
		return SIGNING_KEYSTORE_PASSWORD != null ? manager.getValue(SIGNING_KEYSTORE_PASSWORD) : null;
	}

	public int getSignedDocumentConsultationDurationInDays() {
		return SIGNED_DOCUMENT_CONSULTATION_DURATION_IN_DAYS != null ? manager.getValue(SIGNED_DOCUMENT_CONSULTATION_DURATION_IN_DAYS) : 7;
	}

	public boolean isConvertToPdfAWhenSigning() {
		return CONVERT_TO_PDFA_WHEN_SIGNING != null ? manager.getValue(CONVERT_TO_PDFA_WHEN_SIGNING).equals(ConversionPdfFormat.PDF_A) : true;
	}

	public boolean isDisableExternalSignatures() {
		return manager.getValue(DISABLE_EXTERNAL_SIGNATURES);
	}
}
