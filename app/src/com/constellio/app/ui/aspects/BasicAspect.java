/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.aspects;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
 
@Aspect
public class BasicAspect {
 
	/* Read as -- do this *before* any *call* to the function
         * *java.io.PrintStream.println* that takes a *String*
         * and returns *void* and the function is not called
	 * within any class under the package net.andrewewhite.aspects
	 */
    @Before("   call(void java.io.PrintStream.println(String)) " +
            "&& !within(com.constellio.app.ui.aspects..*)")
    public void beforePrintlnCall() {
        System.out.println("About to make call to print Hello World");
    }
 
    @After("    call(void java.io.PrintStream.println(String)) " +
           "&&  !within(com.constellio.app.ui.aspects..*)")
    public void afterPrintlnCall() {
        System.out.println("Just made call to print Hello World"); 
//        org.atmosphere.util.Version version;
    }
}
