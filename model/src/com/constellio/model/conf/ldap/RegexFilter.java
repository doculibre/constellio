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
package com.constellio.model.conf.ldap;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class RegexFilter implements Filter {
	private String acceptedRegex;
	private String rejectedRegex;
	private Pattern acceptedPattern;
	private Pattern rejectedPattern;

	public RegexFilter(String acceptedRegex, String rejectedRegex) {
		this.acceptedRegex = acceptedRegex;
		this.rejectedRegex = rejectedRegex;
		if(acceptedRegex != null && StringUtils.isNotBlank(acceptedRegex)){
			this.acceptedPattern = Pattern.compile(acceptedRegex);
		}
		if(rejectedRegex != null && StringUtils.isNotBlank(rejectedRegex)){
			this.rejectedPattern = Pattern.compile(rejectedRegex);
		}
	}

	@Override
	public Boolean isAccepted(String word) {
		if (accepted(word)){
			return !rejected(word);
		}else{
			return false;
		}
	}

	private boolean rejected(String word) {
		if (this.rejectedPattern == null){
			return false;
		}
		return this.rejectedPattern.matcher(word).matches();
	}

	private boolean accepted(String word) {
		if (this.acceptedPattern == null){
			return true;
		}
		return this.acceptedPattern.matcher(word).matches();
	}

	public String getAcceptedRegex() {
		return acceptedRegex;
	}

	public String getRejectedRegex() {
		return rejectedRegex;
	}
}