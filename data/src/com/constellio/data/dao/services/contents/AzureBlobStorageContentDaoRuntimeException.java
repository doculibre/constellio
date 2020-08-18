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

	public static class AzureBlobStorageContentDaoRuntimeException_FailedToAddFile extends AzureBlobStorageContentDaoRuntimeException {
		public AzureBlobStorageContentDaoRuntimeException_FailedToAddFile(String contentId) {
			super("Une erreur s'est produite lors de l'ajout du fichier '" + contentId);
		}
	}

	public static class AzureBlobStorageContentDaoRuntimeException_FailedToSaveInformationInVaultRecoveryFile extends FileSystemContentDaoRuntimeException {

		public AzureBlobStorageContentDaoRuntimeException_FailedToSaveInformationInVaultRecoveryFile(String id) {
			super("La sauvegarde dans le fichier de recuperation de la voûte à échoué. Pour l'id : " + id);
		}
	}

	public static class AzureBlobStorageContentDaoRuntimeException_FailedToMoveFileToVault extends FileSystemContentDaoRuntimeException {

		public AzureBlobStorageContentDaoRuntimeException_FailedToMoveFileToVault(String id) {
			super("La sauvegarde du fichier dans la voûte à échoué. Pour l'id : " + id);
		}
	}

	public static class AzureBlobStorageContentDaoRuntimeException_FailedToDeleteFileFromAzure extends FileSystemContentDaoRuntimeException {

		public AzureBlobStorageContentDaoRuntimeException_FailedToDeleteFileFromAzure(String id) {
			super("La suppression du fichier de azure blob storage à échoué. Pour l'id : " + id);
		}
	}

}
