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
package com.constellio.model.services.batch.xml.detail;

@SuppressWarnings("serial")
public class BatchProcessWriterRuntimeException extends RuntimeException {

	public BatchProcessWriterRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public BatchProcessWriterRuntimeException(String message) {
		super(message);
	}

	public BatchProcessWriterRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class AlreadyProcessingABatchProcessPart extends BatchProcessWriterRuntimeException {

		public AlreadyProcessingABatchProcessPart(String computerName) {
			super("Computer " + computerName + "is already processing a batch process part");
		}
	}

	public static class ComputerNotFound extends BatchProcessWriterRuntimeException {

		public ComputerNotFound(String computerName) {
			super("Computer " + computerName + "not found");
		}
	}
}
