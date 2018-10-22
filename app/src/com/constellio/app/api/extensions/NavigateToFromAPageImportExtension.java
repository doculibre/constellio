package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.params.NavigateToFromAPageParams;

public class NavigateToFromAPageImportExtension {

	/**
	 * @return whether or not a navigation has occured
	 */
	public boolean navigateToDisplayDocumentWhileKeepingTraceOfPreviousView(
			NavigateToFromAPageParams navigateToFromAPageParams) {
		return false;
	}

	/**
	 * @return whether or not a navigation has occured
	 */
	public boolean navigateToDisplayFolderWhileKeepingTraceOfPreviousView(
			NavigateToFromAPageParams navigateToFromAPageParams) {
		return false;
	}

	/**
	 * @return whether or not a navigation has occured
	 */
	public boolean navigateToEditFolderWhileKeepingTraceOfPreviousView(
			NavigateToFromAPageParams navigateToFromAPageParams) {
		return false;
	}

	/**
	 * @return whether or not a navigation has occured
	 */
	public boolean navigateToEditDocumentWhileKeepingTraceOfPreviousView(
			NavigateToFromAPageParams navigateToFromAPageParams) {
		return false;
	}

	/**
	 * @return whether or not a navigation has occured
	 */
	public boolean navigateToDuplicateFolderWhileKeepingTraceOfPreviousView(
			NavigateToFromAPageParams navigateToFromAPageParams) {
		return false;
	}

	/**
	 * @return whether or not a navigation has occured
	 */
	public boolean navigateToAddDocumentWhileKeepingTraceOfPreviousView(
			NavigateToFromAPageParams navigateToFromAPageParams) {
		return false;
	}
}
