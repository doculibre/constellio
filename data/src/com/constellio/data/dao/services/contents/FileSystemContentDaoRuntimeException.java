package com.constellio.data.dao.services.contents;

import java.io.File;

public class FileSystemContentDaoRuntimeException extends ContentDaoRuntimeException {

	public FileSystemContentDaoRuntimeException(String message) {
		super(message);
	}

	public FileSystemContentDaoRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileSystemContentDaoRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class FileSystemContentDaoRuntimeException_DatastoreFailure extends FileSystemContentDaoRuntimeException {

		public FileSystemContentDaoRuntimeException_DatastoreFailure(Throwable cause) {
			super("Filesystem failure", cause);
		}
	}

	public static class FileSystemContentDaoRuntimeException_FailedToWriteVaultAndReplication extends  FileSystemContentDaoRuntimeException {

		public FileSystemContentDaoRuntimeException_FailedToWriteVaultAndReplication(File file) {
			this(file.getAbsolutePath());
		}

		public FileSystemContentDaoRuntimeException_FailedToWriteVaultAndReplication(String id) {
			super("La sauvegarde dans la voûte et de la réplication n'a pas fonctionné pour ce fichier : " + id);
		}
	}

	public static class FileSystemContentDaoRuntimeException_FailedToWriteVault extends FileSystemContentDaoRuntimeException {

		public FileSystemContentDaoRuntimeException_FailedToWriteVault(File file) {
			this(file.getAbsolutePath());
		}

		public FileSystemContentDaoRuntimeException_FailedToWriteVault(String id) {
			super("La réplication n'est pas activé et la sauvegarde dans la voûte n'a pas fonctionné pour ce fichier : " + id);
		}
	}

	public static class FileSystemContentDaoRuntimeException_FailedToCreateVaultRecoveryFile extends FileSystemContentDaoRuntimeException {

		public FileSystemContentDaoRuntimeException_FailedToCreateVaultRecoveryFile(File file) {
			super("Le fichier de recuperation n'a pas pu être créer pour la voûte. Le fichier en cause est  :" + file.getAbsolutePath());
		}
	}

	public static class FileSystemContentDaoRuntimeException_FailedToCreateReplicationRecoveryFile extends FileSystemContentDaoRuntimeException {

		public FileSystemContentDaoRuntimeException_FailedToCreateReplicationRecoveryFile(File file) {
			super("Le fichier de recuperation pour n'a pas pu être créer pour la replication. Le fichier en cause est  : " + file.getAbsolutePath());
		}
	}

	public static class FileSystemContentDaoRuntimeException_FailedToSaveInformationInVaultRecoveryFile extends FileSystemContentDaoRuntimeException {

		public FileSystemContentDaoRuntimeException_FailedToSaveInformationInVaultRecoveryFile(String id) {
			super("La sauvegarde dans le fichier de recuperation de la voûte à échoué. Pour l'id : " + id);
		}
	}

	public static class FileSystemContentDaoRuntimeException_FailedToSaveInformationInReplicationRecoveryFile extends FileSystemContentDaoRuntimeException {

		public FileSystemContentDaoRuntimeException_FailedToSaveInformationInReplicationRecoveryFile(String id) {
			super("La sauvegarde dans le fichier de recuperation de la réplication à échoué. Pour l'id : " + id);
		}
	}

}
