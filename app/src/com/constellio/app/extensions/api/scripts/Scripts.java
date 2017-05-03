package com.constellio.app.extensions.api.scripts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Scripts {

	private static List<Script> scripts = new ArrayList<>();

	public static void registerScript(Script script) {
		scripts.add(script);
	}

	public static List<Script> getScripts() {
		return Collections.unmodifiableList(scripts);
	}

}
