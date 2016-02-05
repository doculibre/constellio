package com.constellio.app.ui.aspects;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class UIInitAspect {
	
	@After ("call(void com.constellio.app.ui.application.ConstellioUI.init(com.vaadin.server.VaadinRequest))")
	public void afterUIInit() {
		System.out.println("UI init!");
	}

}
