package com.constellio.app.modules.es.connectors.http.robotstxt;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Group {
	private final List<String> userAgents = new ArrayList<>();
	private final AccessList accessList = new AccessList();
	private boolean anyAgent;
	private Integer crawlDelay;

	/**
	 * Checks if is any agent.
	 *
	 * @return <code>true</code> if any agent
	 */
	public boolean isAnyAgent() {
		return anyAgent;
	}

	/**
	 * Checks if group is exact in terms of user agents.
	 *
	 * @param group group to compare
	 * @return {@code true} if sections are exact.
	 */
	public boolean isExact(Group group) {
		if (isAnyAgent() && group.isAnyAgent()) {
			return true;
		}
		if ((isAnyAgent() && !group.isAnyAgent() || (!isAnyAgent() && group.isAnyAgent()))) {
			return false;
		}

		return IterableUtils.matchesAny(group.userAgents, new Predicate<String>() {
			@Override
			public boolean evaluate(final String sectionUserAgent) {
				return IterableUtils.matchesAny(userAgents, new Predicate<String>() {
					@Override
					public boolean evaluate(String userAgent) {
						return userAgent.equalsIgnoreCase(sectionUserAgent);
					}
				});
			}
		});
	}

	/**
	 * Adds user agent.
	 *
	 * @param userAgent host name
	 */
	public void addUserAgent(String userAgent) {
		if (userAgent.equals("*")) {
			anyAgent = true;
		} else {
			this.userAgents.add(userAgent);
		}
	}

	/**
	 * Gets access list.
	 *
	 * @return access list
	 */
	public AccessList getAccessList() {
		return accessList;
	}

	/**
	 * Adds access.
	 *
	 * @param access access
	 */
	public void addAccess(Access access) {
		this.accessList.addAccess(access);
	}

	/**
	 * Select any access matching input path.
	 *
	 * @param userAgent        user agent
	 * @param relativePath     path to test
	 * @param matchingStrategy matcher
	 * @return list of matching elements
	 */
	public List<Access> select(String userAgent, String relativePath, MatchingStrategy matchingStrategy) {
		if ((userAgent == null && !isAnyAgent()) || relativePath == null || !matchUserAgent(userAgent)) {
			return Collections.EMPTY_LIST;
		}
		return accessList.select(relativePath, matchingStrategy);
	}

	/**
	 * Checks if the section is applicable for a given user agent.
	 *
	 * @param userAgent requested user agent
	 * @return <code>true</code> if the section is applicable for the requested user agent
	 */
	public boolean matchUserAgent(String userAgent) {
		if (anyAgent) {
			return true;
		}
		if (!anyAgent && userAgent == null) {
			return false;
		}

		for (String agent : userAgents) {
			if (agent.equalsIgnoreCase(userAgent)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Sets crawl delay.
	 *
	 * @param crawlDelay crawl delay.
	 */
	public void setCrawlDelay(Integer crawlDelay) {
		this.crawlDelay = crawlDelay;
	}

	/**
	 * Gets crawl delay.
	 *
	 * @return crawl delay
	 */
	public Integer getCrawlDelay() {
		return crawlDelay;
	}

	@Override
	public String toString() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);

		if (anyAgent) {
			pw.format("User-agent: %s", "*").println();
		}

		for (String agent : userAgents) {
			pw.format("User-agent: %s", agent).println();
		}

		pw.println(accessList);

		if (crawlDelay != null) {
			pw.format("Crawl-delay: %d", crawlDelay).println();
		}

		pw.flush();

		return sw.toString();
	}
}
