package com.constellio.app.ui.aspects;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class ViewEnterAspect {
	
	@After ("call(void com.vaadin.navigator.View(com.vaadin.navigator.ViewChangeListener.ViewChangeEvent))")
	public void afterViewEnter() {
		System.out.println("View entered!");
	}

}
