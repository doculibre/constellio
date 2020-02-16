package com.constellio.data.dao.services.transactionLog;

import org.apache.commons.lang.StringUtils;

import java.util.List;

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

	public static class SecondTransactionLogRuntimeException_CannotParseLogCommand
			extends SecondTransactionLogRuntimeException {

		public SecondTransactionLogRuntimeException_CannotParseLogCommand(List<String> lines, String fileName,
																		  Throwable t) {
			super("Cannot parse log command in file '" + fileName + "' : " + toCommand(lines), t);
		}

		private static String toCommand(List<String> lines) {
			StringBuilder stringBuilder = new StringBuilder();
			for (String line : lines) {
				stringBuilder.append("\n\t" + line);
			}
			return stringBuilder.toString();
		}
	}

	public static class SecondTransactionLogRuntimeException_CannotParseJsonLogCommand
			extends SecondTransactionLogRuntimeException {

		public SecondTransactionLogRuntimeException_CannotParseJsonLogCommand(String json,
																		  Throwable t) {
			super("Cannot parse json log command in String : " + json, t);
		}
	}

	public static class SecondTransactionLogRuntimeException_NotAllLogsWereDeletedCorrectlyException extends SecondTransactionLogRuntimeException {
		final List<String> notDeletedFiles;

		public SecondTransactionLogRuntimeException_NotAllLogsWereDeletedCorrectlyException(
				List<String> notDeletedFiles) {
			super("Not all tLog files were deleted, remaining :" + StringUtils.join(notDeletedFiles, ", "));
			this.notDeletedFiles = notDeletedFiles;
		}

		public List<String> getNotDeletedFiles() {
			return notDeletedFiles;
		}
	}
}
