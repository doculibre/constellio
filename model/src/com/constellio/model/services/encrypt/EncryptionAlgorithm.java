package com.constellio.model.services.encrypt;

public enum EncryptionAlgorithm {
	AES("AES/CBC/PKCS5Padding"), RSA("RSA/ECB/PKCS1Padding");

	private final String algorithm;

	EncryptionAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getAlgorithm() {
		return algorithm;
	}
}