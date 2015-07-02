/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.services.borrowingServices;

import java.util.Date;

import org.joda.time.LocalDate;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_CannotBorrowActiveFolder;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_FolderIsAlreadyBorrowed;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_FolderIsNotBorrowed;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_UserNotAllowedToReturnFolder;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_UserWithoutReadAccessToFolder;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;

public class BorrowingServices {
	public static final String RGD = "RGD";
	public static final String ACTIVE = "ACTIVE";
	private final String collection;
	private final ModelLayerFactory modelLayerFactory;
	private final RecordServices recordServices;
	private final LoggingServices loggingServices;
	private final RMSchemasRecordsServices rm;

	public BorrowingServices(String collection, ModelLayerFactory modelLayerFactory) {
		this.collection = collection;
		this.modelLayerFactory = modelLayerFactory;
		this.recordServices = modelLayerFactory.newRecordServices();
		this.loggingServices = modelLayerFactory.newLoggingServices();
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	public void borrowFolder(String folderId, Date previewReturnDate, User currentUser, User borrowerEntered)
			throws RecordServicesException {

		Record folder = recordServices.getDocumentById(folderId);
		validateCanBorrow(currentUser, folder);
		setBorrowedMetadatasToFolder(folder, previewReturnDate, currentUser.getId(), borrowerEntered.getId());
		recordServices.update(folder);
		loggingServices.borrowRecord(folder, borrowerEntered);
	}

	public void returnFolder(String folderId, User currentUser)
			throws RecordServicesException {

		Record folder = recordServices.getDocumentById(folderId);
		validateCanReturnFolder(currentUser, folder);
		folder = setReturnedMetadatasToFolder(folder);
		recordServices.update(folder);
		loggingServices.returnRecord(folder, currentUser);
	}

	public void validateCanReturnFolder(User currentUser, Record folder) {
		if (currentUser.hasReadAccess().on(folder)) {
			if (folder.get(rm.folderBorrowed()) == null) {
				throw new BorrowingServicesRunTimeException_FolderIsNotBorrowed(folder.getId());
			} else if (!currentUser.getUserRoles().contains(RGD) && !currentUser.getId()
					.equals(folder.get(rm.folderBorrowedUserEntered()))) {
				throw new BorrowingServicesRunTimeException_UserNotAllowedToReturnFolder(currentUser.getUsername());
			}
		} else {
			throw new BorrowingServicesRunTimeException_UserWithoutReadAccessToFolder(currentUser.getUsername(), folder.getId());
		}

	}

	public void validateCanBorrow(User currentUser, Record folder) {

		if (currentUser.hasReadAccess().on(folder)) {
			if (ACTIVE.equals(folder.get(rm.folderArchivisticStatus()).toString())) {
				throw new BorrowingServicesRunTimeException_CannotBorrowActiveFolder(folder.getId());
			} else if (folder.get(rm.folderBorrowed()) != null) {
				throw new BorrowingServicesRunTimeException_FolderIsAlreadyBorrowed(folder.getId());
			}
		} else {
			throw new BorrowingServicesRunTimeException_UserWithoutReadAccessToFolder(currentUser.getUsername(), folder.getId());
		}
	}

	//

	private void setBorrowedMetadatasToFolder(Record folder, Date previewReturnDate, String userId, String borrowerEnteredId) {
		LocalDate newPreviewReturnDate = LocalDate.fromDateFields(previewReturnDate);
		folder.set(rm.folderBorrowed(), true);
		folder.set(rm.folderBorrowDate(), TimeProvider.getLocalDateTime());
		folder.set(rm.folderBorrowPreviewReturnDate(), newPreviewReturnDate);
		folder.set(rm.folderBorrowedUser(), userId);
		folder.set(rm.folderBorrowedUserEntered(), borrowerEnteredId);
	}

	private Record setReturnedMetadatasToFolder(Record folder) {

		folder.set(rm.folderBorrowed(), null);
		folder.set(rm.folderBorrowDate(), null);
		folder.set(rm.folderBorrowPreviewReturnDate(), null);
		folder.set(rm.folderBorrowedUser(), null);
		folder.set(rm.folderBorrowedUserEntered(), null);
		return folder;
	}
}