package com.constellio.app.ui.aspects;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class ButtonClickListenerAspect {
	
	@After ("call(void com.vaadin.ui.Button.ClickListener.buttonClick(com.vaadin.ui.Button.ClickEvent))")
	public void afterButtonClick() {
		System.out.println("Button clicked!");
	}

}
