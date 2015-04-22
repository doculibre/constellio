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
package com.constellio.model.services.records;

public class BulkRecordTransactionHandlerRuntimeException extends RuntimeException {

	public BulkRecordTransactionHandlerRuntimeException(String message) {
		super(message);
	}

	public BulkRecordTransactionHandlerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public BulkRecordTransactionHandlerRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class BulkRecordTransactionHandlerRuntimeException_Interrupted
			extends BulkRecordTransactionHandlerRuntimeException {
		public BulkRecordTransactionHandlerRuntimeException_Interrupted(InterruptedException e) {
			super("Interrupted", e);
		}
	}

	public static class BulkRecordTransactionHandlerRuntimeException_ExceptionExecutingTransaction
			extends BulkRecordTransactionHandlerRuntimeException {
		public BulkRecordTransactionHandlerRuntimeException_ExceptionExecutingTransaction(Exception e) {
			super("An exception occured while executing a transaction", e);
		}
	}
}
