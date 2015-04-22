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
package com.constellio.model.services.batch.xml.list;

@SuppressWarnings("serial")
public class BatchProcessListWriterRuntimeException extends RuntimeException {

	public BatchProcessListWriterRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public BatchProcessListWriterRuntimeException(String message) {
		super(message);
	}

	public BatchProcessListWriterRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class CannotHaveTwoBatchProcessInCurrentBatchProcessList extends BatchProcessListWriterRuntimeException {

		public CannotHaveTwoBatchProcessInCurrentBatchProcessList() {
			super("Cannot have two batch processes in batch process current list");
		}
	}

	public static class NoPendingBatchProcessesInList extends BatchProcessListWriterRuntimeException {

		public NoPendingBatchProcessesInList() {
			super("No pending batch processes in list");
		}
	}

	public static class BatchProcessAlreadyFinished extends BatchProcessListWriterRuntimeException {

		public BatchProcessAlreadyFinished() {
			super("Batch process already finished");
		}
	}

	public static class BatchProcessNotFound extends BatchProcessListWriterRuntimeException {

		public BatchProcessNotFound(String id) {
			super("Batch process " + id + " not found");
		}
	}
}
