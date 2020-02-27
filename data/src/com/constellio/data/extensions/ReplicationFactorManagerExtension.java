package com.constellio.data.extensions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.Collection;

public class ReplicationFactorManagerExtension {

	public void onTransactionsReplayed(TransactionsReplayedParams params) {
	}

	@Getter
	@AllArgsConstructor
	public static class TransactionsReplayedParams {
		private Collection<TransactionReplayed> replayedTransactions;
	}

	@Data
	@AllArgsConstructor
	public static class TransactionReplayed {
		private String recordDtoId;
		private Long version;
	}

}
