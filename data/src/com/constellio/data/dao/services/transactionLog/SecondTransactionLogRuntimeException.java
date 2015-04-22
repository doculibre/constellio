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
package com.constellio.data.dao.services.transactionLog;

public class SecondTransactionLogRuntimeException extends RuntimeException {

	public SecondTransactionLogRuntimeException(String message) {
		super(message);
	}

	public SecondTransactionLogRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SecondTransactionLogRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class SecondTransactionLogRuntimeException_CouldNotPrepareTransactionLog
			extends SecondTransactionLogRuntimeException {

		public SecondTransactionLogRuntimeException_CouldNotPrepareTransactionLog(Throwable cause) {
			super("Could not prepare transaction log", cause);
		}
	}

	public static class SecondTransactionLogRuntimeException_CouldNotFlushTransaction
			extends SecondTransactionLogRuntimeException {

		public SecondTransactionLogRuntimeException_CouldNotFlushTransaction(Throwable cause) {
			super("Could not flush transaction", cause);
		}
	}

	public static class SecondTransactionLogRuntimeException_LogIsInInvalidStateCausedByPreviousException
			extends SecondTransactionLogRuntimeException {

		public SecondTransactionLogRuntimeException_LogIsInInvalidStateCausedByPreviousException() {
			super("Log in in a invalid state caused by a previous exception");
		}
	}

	public static class SecondTransactionLogRuntimeException_TransactionLogIsNotInitialized
			extends SecondTransactionLogRuntimeException {

		public SecondTransactionLogRuntimeException_TransactionLogIsNotInitialized() {
			super("Transaction log was not started");
		}
	}

	public static class SecondTransactionLogRuntimeException_TransactionLogHasAlreadyBeenInitialized
			extends SecondTransactionLogRuntimeException {

		public SecondTransactionLogRuntimeException_TransactionLogHasAlreadyBeenInitialized() {
			super("Transaction log has already been started");
		}
	}

	public static class SecondTransactionLogRuntimeException_CouldNotRegroupAndMoveInVault
			extends SecondTransactionLogRuntimeException {

		public SecondTransactionLogRuntimeException_CouldNotRegroupAndMoveInVault(Throwable t) {
			super("Could not regroup and move in vault", t);
		}
	}
}
