package com.constellio.app.api.cmis;

import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;

@SuppressWarnings("serial")
public class CmisExceptions {

	public static class CmisExceptions_InvalidLogin extends CmisPermissionDeniedException {
		public CmisExceptions_InvalidLogin() {
			super("Invalid username or password.");
		}
	}

	public static class CmisExceptions_ObjectNotFound extends CmisObjectNotFoundException {
		public CmisExceptions_ObjectNotFound(String object, String id) {
			super("Unknown " + object + " '" + id + "'!");
		}

		public CmisExceptions_ObjectNotFound(String message, Exception e) {
			super(message, e);
		}

		public CmisExceptions_ObjectNotFound() {
			super("Object not found!");
		}

		public CmisExceptions_ObjectNotFound(String object) {
			super(object + " not found!");
		}
	}

	public static class CmisExceptions_InvalidArgumentNoObjectIdsProvided extends CmisInvalidArgumentException {
		public CmisExceptions_InvalidArgumentNoObjectIdsProvided() {
			super("No object ids provided!");
		}
	}

	public static class CmisExceptions_CannotCreateCollection extends CmisInvalidArgumentException {
		public CmisExceptions_CannotCreateCollection() {
			super("Cannot create collection via CMIS.");
		}
	}

	public static class CmisExceptions_CannotUpdateCollection extends CmisInvalidArgumentException {
		public CmisExceptions_CannotUpdateCollection() {
			super("Cannot update collection via CMIS.");
		}
	}

	public static class CmisExceptions_UpdateConflict extends CmisInvalidArgumentException {
		public CmisExceptions_UpdateConflict() {
			super("CMIS Update conflict, record cannot be updated.");
		}
	}

	public static class CmisExceptions_CannotCreateTaxonomy extends CmisInvalidArgumentException {
		public CmisExceptions_CannotCreateTaxonomy() {
			super("Cannot create taxonomy via CMIS.");
		}
	}

	public static class CmisExceptions_InvalidArgument extends CmisInvalidArgumentException {
		public CmisExceptions_InvalidArgument(String argument) {
			super(argument + " is not valid!");
		}
	}

	public static class CmisExceptions_InvalidArgumentObjectNotSetted extends CmisInvalidArgumentException {
		public CmisExceptions_InvalidArgumentObjectNotSetted() {
			super("Object Id must be set.");
		}

		public CmisExceptions_InvalidArgumentObjectNotSetted(String objectName) {
			super(objectName + " must be set.");
		}
	}

	public static class CmisExceptions_InvalidArgumentNotAFolder extends CmisInvalidArgumentException {
		public CmisExceptions_InvalidArgumentNotAFolder() {
			super("Not a folder!");
		}
	}

	public static class CmisExceptions_InvalidArgumentNotADocument extends CmisInvalidArgumentException {
		public CmisExceptions_InvalidArgumentNotADocument() {
			super("Not a document!");
		}
	}

	public static class CmisExceptions_InvalidArgumentDepthCantBeZero extends CmisInvalidArgumentException {
		public CmisExceptions_InvalidArgumentDepthCantBeZero() {
			super("Depth must not be 0!");
		}
	}

	public static class CmisExceptions_InvalidArgumentHasNoParent extends CmisInvalidArgumentException {
		public CmisExceptions_InvalidArgumentHasNoParent() {
			super("The root folder has no parent!");
		}
	}

	public static class CmisExceptions_ConstraintRequired extends CmisConstraintException {
		public CmisExceptions_ConstraintRequired(String id) {
			super("Property '" + id + "' is required!");
		}
	}

	public static class CmisExceptions_ConstraintNoContent extends CmisConstraintException {
		public CmisExceptions_ConstraintNoContent(String fileName) {
			super("Document '" + fileName + "' has no content!");
		}
	}

	public static class CmisExceptions_ConstraintVersioningNotSupported extends CmisConstraintException {
		public CmisExceptions_ConstraintVersioningNotSupported() {
			super("Versioning not supported!");
		}
	}

	public static class CmisExceptions_ConstraintUnknown extends CmisConstraintException {
		public CmisExceptions_ConstraintUnknown(String id) {
			super("Property '" + id + "' is unknown!");
		}
	}

	public static class CmisExceptions_ConstraintReadOnly extends CmisConstraintException {
		public CmisExceptions_ConstraintReadOnly(String id) {
			super("Property '" + id + "' is readonly!");
		}
	}

	public static class CmisExceptions_ConstraintCannotBeUpdated extends CmisConstraintException {
		public CmisExceptions_ConstraintCannotBeUpdated(String id) {
			super("Property '" + id + "' cannot be updated!");
		}
	}

	public static class CmisExceptions_ConstraintViolationName extends CmisNameConstraintViolationException {
		public CmisExceptions_ConstraintViolationName() {
			super("Name is not valid!");
		}
	}

	public static class CmisExceptions_ConstraintFolderNotEmpty extends CmisConstraintException {
		public CmisExceptions_ConstraintFolderNotEmpty(String folderName) {
			super("Folder '" + folderName + "' is not empty!");
		}
	}

	public static class CmisExceptions_SkippingStream extends CmisRuntimeException {
		public CmisExceptions_SkippingStream(Exception e) {
			super("Skipping the stream failed!", e);
		}
	}

	public static class CmisExceptions_ReadingStream extends CmisRuntimeException {
		public CmisExceptions_ReadingStream(Exception e) {
			super("Reading the stream failed!", e);
		}
	}

	public static class CmisExceptions_Runtime extends CmisRuntimeException {

		public CmisExceptions_Runtime(String message) {
			super(message);
		}

		public CmisExceptions_Runtime(String message, Exception e) {
			super(message, e);
		}
	}

	public static class CmisExceptions_TargetIsNotInPrincipalTaxonomy extends CmisExceptions_Runtime {
		public CmisExceptions_TargetIsNotInPrincipalTaxonomy(String targetRecordId) {
			super("Target " + targetRecordId + " record is not in a principal taxonomy");
		}
	}

	public static class CmisExceptions_StorageWriteRead extends CmisStorageException {
		public CmisExceptions_StorageWriteRead(Exception e) {
			super("Could not write or read content: " + e.getMessage(), e);
		}
	}

	public static class CmisExceptions_StorageWrite extends CmisStorageException {
		public CmisExceptions_StorageWrite(Exception e) {
			super("Could not write content: " + e.getMessage(), e);
		}
	}

	public static class CmisExceptions_StorageDeletationFailed extends CmisStorageException {
		public CmisExceptions_StorageDeletationFailed(String fileName) {
			super("Deletion '" + fileName + "' failed!");
		}
	}

	public static class CmisExceptions_StorageCreateFile extends CmisStorageException {
		public CmisExceptions_StorageCreateFile(Exception e) {
			super("Could not create file: " + e.getMessage(), e);
		}
	}

	public static class CmisExceptions_StorageObjectAlreadyExists extends CmisStorageException {
		public CmisExceptions_StorageObjectAlreadyExists(String objectName) {
			super("Object '" + objectName + "' already exists!");
		}
	}

	public static class CmisExceptions_StorageCreateFolder extends CmisStorageException {
		public CmisExceptions_StorageCreateFolder(String folderName) {
			super("Could not create folder: " + folderName);
		}
	}

	public static class CmisExceptions_StorageMoveFailed extends CmisStorageException {
		public CmisExceptions_StorageMoveFailed() {
			super("Move failed!");
		}
	}

	public static class CmisExceptions_ConstraintViolationDocumentAlreadyExists extends CmisNameConstraintViolationException {
		public CmisExceptions_ConstraintViolationDocumentAlreadyExists(String doc) {
			super("Document '" + doc + "' already exists!");
		}
	}

	public static class CmisExceptions_CmisStreamNotSupportedNotAFile extends CmisStreamNotSupportedException {
		public CmisExceptions_CmisStreamNotSupportedNotAFile(String objectName) {
			super(objectName + " is not a file!");
		}
	}

	public static class CmisExceptions_CmisUpdateConflictCannotRename extends CmisUpdateConflictException {
		public CmisExceptions_CmisUpdateConflictCannotRename() {
			super("Could not rename object!");
		}
	}

	public static class CmisExceptions_CmisContentAlreadyExists extends CmisContentAlreadyExistsException {
		public CmisExceptions_CmisContentAlreadyExists() {
			super("Content already exists!");
		}
	}

	public static class CmisExceptions_CmisRuntimeCannotUpdateRecord extends CmisRuntimeException {
		public CmisExceptions_CmisRuntimeCannotUpdateRecord(String recordId, Exception e) {
			super("Cannot update record " + recordId, e);
		}
	}
}
