package com.constellio.app.ui.framework.components.viewers.audio;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.components.content.ContentVersionVOResource;
import com.vaadin.ui.Audio;
import com.vaadin.ui.CustomComponent;

public class AudioViewer extends CustomComponent {
	
	public static final String[] SUPPORTED_EXTENSIONS = { /* "mp3", "wav" */ };

	public AudioViewer(ContentVersionVO contentVersionVO) {
		Audio audio = new Audio();
		audio.setHtmlContentAllowed(true);
		audio.setSource(new ContentVersionVOResource(contentVersionVO));
		setCompositionRoot(audio);
	}

}
