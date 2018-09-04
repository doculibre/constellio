package com.constellio.app.modules.es.connectors.http.robotstxt;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public interface WinningStrategy {
	/**
	 * Selects a single winner amongst the candidates.
	 *
	 * @param candidates list of candidates
	 * @return a winner or {@code null} if no winner (for example: because empty list of candidates)
	 */
	Access selectWinner(List<Access> candidates);

	public static WinningStrategy DEFAULT = new WinningStrategy() {
		@Override
		public Access selectWinner(List<Access> candidates) {
			Access winningDisallow = getWinning(candidates, false);
			Access winningAllow = getWinning(candidates, true);

			if (winningAllow != null && winningAllow.getPath().length() >= (winningDisallow != null ? winningDisallow.getPath().length() : 0)) {
				return winningAllow;
			}

			if (winningDisallow != null) {
				return winningDisallow;
			}

			return null;
		}

		@NotNull
		public Comparator<Access> getAccessComparator() {
			return new Comparator<Access>() {
				@Override
				public int compare(Access l, Access r) {
					return r.getPath().length() - l.getPath().length();
				}
			};
		}

		@Nullable
		public Access getWinning(List<Access> candidates, final boolean allow) {

			List<Access> select = ListUtils.select(candidates, new Predicate<Access>() {
				@Override
				public boolean evaluate(Access acc) {
					return acc.hasAccess() == allow;
				}
			});

			if (!CollectionUtils.isEmpty(select)) {
				Collections.sort(select, getAccessComparator());

				return select.get(0);
			}

			return null;
		}
	};
}
