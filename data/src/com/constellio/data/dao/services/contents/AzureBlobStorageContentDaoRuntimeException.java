package com.constellio.data.dao.services.contents;


public class AzureBlobStorageContentDaoRuntimeException extends ContentDaoRuntimeException {

	public AzureBlobStorageContentDaoRuntimeException(String message) {
		super(message);
	}

	public AzureBlobStorageContentDaoRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public AzureBlobStorageContentDaoRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class AzureBlobStorageContentDaoRuntimeException_FailedToGetFile extends AzureBlobStorageContentDaoRuntimeException {
		public AzureBlobStorageContentDaoRuntimeException_FailedToGetFile(String contentId) {
			super("Le fichier '" + contentId + "' n'est pas enregistré en local");
		}
	}


}
