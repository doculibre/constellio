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

	public static class FileSystemContentDaoRuntimeException_FileNotFoundWhileRestauringVaultFiles extends FileSystemContentDaoRuntimeException {
		public FileSystemContentDaoRuntimeException_FileNotFoundWhileRestauringVaultFiles(Exception exception) {
			super("Un fichier n'existe pas pendant la restauration de ce fichier dans la voûte", exception);
		}
	}

	public static class FileSystemContentDaoRuntimeException_IOErrorWhileRestauringVaultFiles extends FileSystemContentDaoRuntimeException {
		public FileSystemContentDaoRuntimeException_IOErrorWhileRestauringVaultFiles(Exception exception) {
			super("Une erreur d'IO sais produite pendant la restauration des fichiers de la voûte.", exception);
		}
	}

	public static class FileSystemContentDaoRuntimeException_ErrorWhileDeletingReplicationRecoveryFile extends FileSystemContentDaoRuntimeException {
		public FileSystemContentDaoRuntimeException_ErrorWhileDeletingReplicationRecoveryFile() {
			super("Une erreur sait produite pendant la suppression du fichier de restauration de la réplication.");
		}
	}

	public static class FileSystemContentDaoRuntimeException_IOErrorWhileCreatingReplicationRecoveryFile extends FileSystemContentDaoRuntimeException {
		public FileSystemContentDaoRuntimeException_IOErrorWhileCreatingReplicationRecoveryFile() {
			this(null);
		}

		public FileSystemContentDaoRuntimeException_IOErrorWhileCreatingReplicationRecoveryFile(Exception exception) {
			super("Une erreur sait produite pendant la création du fichier de restauration de la réplication.", exception);
		}
	}


	public static class FileSystemContentDaoRuntimeException_FileNotFoundWhileRestauringReplicationVault extends FileSystemContentDaoRuntimeException {
		public FileSystemContentDaoRuntimeException_FileNotFoundWhileRestauringReplicationVault(Exception exception) {
			super("Un fichier n'existe pas pendant la restauration de ce fichier dans la voûte de réplication", exception);
		}
	}

	public static class FileSystemContentDaoRuntimeException_IOErrorWhileRestauringReplicationVault extends FileSystemContentDaoRuntimeException {
		public FileSystemContentDaoRuntimeException_IOErrorWhileRestauringReplicationVault(Exception exception) {
			super("Une erreur d'IO sais produite pendant la restauration des fichiers de la voûte de réplication.", exception);
		}
	}

	public static class FileSystemContentDaoRuntimeException_ErrorWhileDeletingVaultRecoveryFile extends FileSystemContentDaoRuntimeException {
		public FileSystemContentDaoRuntimeException_ErrorWhileDeletingVaultRecoveryFile() {
			super("Une erreur sait produite pendant la suppression du fichier de restauration de la voûte.");
		}
	}

	public static class FileSystemContentDaoRuntimeException_IOErrorWhileCreatingVaultRecoveryFile extends FileSystemContentDaoRuntimeException {
		public FileSystemContentDaoRuntimeException_IOErrorWhileCreatingVaultRecoveryFile() {
			this(null);
		}

		public FileSystemContentDaoRuntimeException_IOErrorWhileCreatingVaultRecoveryFile(Exception exception) {
			super("Une erreur sait produite pendant la création du fichier de restauration de la voûte.", exception);
		}
	}

	public static class FileSystemContentDaoRuntimeException_ErrorWhileCopingFileToTheVault extends FileSystemContentDaoRuntimeException {
		public FileSystemContentDaoRuntimeException_ErrorWhileCopingFileToTheVault(File file) {
			super("Une erreur sait produite pendant la copie du fichier : " + file.getAbsolutePath() + " vers la voûte." );
		}
	}

	public static class FileSystemContentDaoRuntimeException_ErrorWhileCopingFileToTheReplicationVault extends FileSystemContentDaoRuntimeException {
		public FileSystemContentDaoRuntimeException_ErrorWhileCopingFileToTheReplicationVault(File file) {
			super("Une erreur sait produite pendant la copie du fichier : " + file.getAbsolutePath() + " vers la voûte de réplication.");
		}
	}
}
