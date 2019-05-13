/**
 * IntelliGID
 * Copyright (C) 2010 DocuLibre inc.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.constellio.data.utils;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;

public class AccentApostropheCleaner {

	private static List<String> aposs = Arrays.asList(new String[]{"l'",
																   "d'", "m'", "t'", "qu'", "n'", "s'", "j'", "l’", "l’", "m’", "t’",
																   "qu’", "n’", "s’", "j’", "c'", "c’"});

	public final static String removeApostrophe(String text) {
		for (String apos : aposs) {
			if (text.startsWith(apos)) {
				text = text.substring(2, text.length());
			}
		}
		return text;
	}

	public final static String removePluriel(String text) {
		if (text == null) {
			return "";
		}
		if (text.toLowerCase().endsWith("s")) {
			text = text.substring(0, text.length() - 1);
		}
		return text;
	}

	public final static String removeAccents(String input) {
		if (input == null) {
			return "";
		}
		final StringBuffer output = new StringBuffer();
		for (int i = 0; i < input.length(); i++) {
			switch (input.charAt(i)) {
				case '\u0400':
					output.append('\u0065');
					break;
				case '\u0401':
					output.append('\u0065');
					break;
				case '\u0407':
					output.append('\u0069');
					break;
				case '\u0450':
					output.append('\u0065');
					break;
				case '\u0451':
					output.append('\u0065');
					break;
				case '\u0457':
					output.append('\u0069');
					break;
				case '\u04D0':
					output.append('\u0061');
					break;
				case '\u04D1':
					output.append('\u0061');
					break;
				case '\u04D2':
					output.append('\u0061');
					break;
				case '\u04D3':
					output.append('\u0061');
					break;
				case '\u04D6':
					output.append('\u0065');
					break;
				case '\u04D7':
					output.append('\u0065');
					break;
				case '\u04E6':
					output.append('\u006F');
					break;
				case '\u04E7':
					output.append('\u006F');
					break;
				case '\u04EE':
					output.append('\u0079');
					break;
				case '\u04EF':
					output.append('\u0079');
					break;
				case '\u04F0':
					output.append('\u0079');
					break;
				case '\u04F1':
					output.append('\u0079');
					break;
				case '\u04F2':
					output.append('\u0079');
					break;
				case '\u04F3':
					output.append('\u0079');
					break;
				case '\u0386':
					output.append('\u0061');
					break;
				case '\u0388':
					output.append('\u0065');
					break;
				case '\u0389':
					output.append('\u0068');
					break;
				case '\u038A':
					output.append('\u0069');
					break;
				case '\u038C':
					output.append('\u006F');
					break;
				case '\u038E':
					output.append('\u0079');
					break;
				case '\u0390':
					output.append('\u0069');
					break;
				case '\u03AA':
					output.append('\u0069');
					break;
				case '\u03AB':
					output.append('\u0079');
					break;
				case '\u03AF':
					output.append('\u0069');
					break;
				case '\u03CA':
					output.append('\u0069');
					break;
				case '\u03CC':
					output.append('\u006F');
					break;
				case '\u1F08':
					output.append('\u0061');
					break;
				case '\u1F09':
					output.append('\u0061');
					break;
				case '\u1F0A':
					output.append('\u0061');
					break;
				case '\u1F0B':
					output.append('\u0061');
					break;
				case '\u1F0C':
					output.append('\u0061');
					break;
				case '\u1F0D':
					output.append('\u0061');
					break;
				case '\u1F0E':
					output.append('\u0061');
					break;
				case '\u1F0F':
					output.append('\u0061');
					break;
				case '\u1F18':
					output.append('\u0065');
					break;
				case '\u1F19':
					output.append('\u0065');
					break;
				case '\u1F1A':
					output.append('\u0065');
					break;
				case '\u1F1B':
					output.append('\u0065');
					break;
				case '\u1F1C':
					output.append('\u0065');
					break;
				case '\u1F1D':
					output.append('\u0065');
					break;
				case '\u1F28':
					output.append('\u0068');
					break;
				case '\u1F29':
					output.append('\u0068');
					break;
				case '\u1F2A':
					output.append('\u0068');
					break;
				case '\u1F2B':
					output.append('\u0068');
					break;
				case '\u1F2C':
					output.append('\u0068');
					break;
				case '\u1F2D':
					output.append('\u0068');
					break;
				case '\u1F2E':
					output.append('\u0068');
					break;
				case '\u1F2F':
					output.append('\u0068');
					break;
				case '\u1F30':
					output.append('\u0069');
					break;
				case '\u1F31':
					output.append('\u0069');
					break;
				case '\u1F32':
					output.append('\u0069');
					break;
				case '\u1F33':
					output.append('\u0069');
					break;
				case '\u1F34':
					output.append('\u0069');
					break;
				case '\u1F35':
					output.append('\u0069');
					break;
				case '\u1F36':
					output.append('\u0069');
					break;
				case '\u1F37':
					output.append('\u0069');
					break;
				case '\u1F38':
					output.append('\u0069');
					break;
				case '\u1F39':
					output.append('\u0069');
					break;
				case '\u1F3A':
					output.append('\u0069');
					break;
				case '\u1F3B':
					output.append('\u0069');
					break;
				case '\u1F3C':
					output.append('\u0069');
					break;
				case '\u1F3D':
					output.append('\u0069');
					break;
				case '\u1F3E':
					output.append('\u0069');
					break;
				case '\u1F3F':
					output.append('\u0069');
					break;
				case '\u1F40':
					output.append('\u006F');
					break;
				case '\u1F41':
					output.append('\u006F');
					break;
				case '\u1F42':
					output.append('\u006F');
					break;
				case '\u1F43':
					output.append('\u006F');
					break;
				case '\u1F44':
					output.append('\u006F');
					break;
				case '\u1F45':
					output.append('\u006F');
					break;
				case '\u1F48':
					output.append('\u006F');
					break;
				case '\u1F49':
					output.append('\u006F');
					break;
				case '\u1F4A':
					output.append('\u006F');
					break;
				case '\u1F4B':
					output.append('\u006F');
					break;
				case '\u1F4C':
					output.append('\u006F');
					break;
				case '\u1F4D':
					output.append('\u006F');
					break;
				case '\u1F59':
					output.append('\u0079');
					break;
				case '\u1F5B':
					output.append('\u0079');
					break;
				case '\u1F5D':
					output.append('\u0079');
					break;
				case '\u1F5F':
					output.append('\u0079');
					break;
				case '\u1F76':
					output.append('\u0069');
					break;
				case '\u1F77':
					output.append('\u0069');
					break;
				case '\u1F78':
					output.append('\u006F');
					break;
				case '\u1F79':
					output.append('\u006F');
					break;
				case '\u1F88':
					output.append('\u0061');
					break;
				case '\u1F89':
					output.append('\u0061');
					break;
				case '\u1F8A':
					output.append('\u0061');
					break;
				case '\u1F8B':
					output.append('\u0061');
					break;
				case '\u1F8C':
					output.append('\u0061');
					break;
				case '\u1F8D':
					output.append('\u0061');
					break;
				case '\u1F8E':
					output.append('\u0061');
					break;
				case '\u1F8F':
					output.append('\u0061');
					break;
				case '\u1F98':
					output.append('\u0068');
					break;
				case '\u1F99':
					output.append('\u0068');
					break;
				case '\u1F9A':
					output.append('\u0068');
					break;
				case '\u1F9B':
					output.append('\u0068');
					break;
				case '\u1F9C':
					output.append('\u0068');
					break;
				case '\u1F9D':
					output.append('\u0068');
					break;
				case '\u1F9E':
					output.append('\u0068');
					break;
				case '\u1F9F':
					output.append('\u0068');
					break;
				case '\u1FB8':
					output.append('\u0061');
					break;
				case '\u1FB9':
					output.append('\u0061');
					break;
				case '\u1FBA':
					output.append('\u0061');
					break;
				case '\u1FBB':
					output.append('\u0061');
					break;
				case '\u1FBC':
					output.append('\u0061');
					break;
				case '\u1FC8':
					output.append('\u0065');
					break;
				case '\u1FC9':
					output.append('\u0065');
					break;
				case '\u1FCA':
					output.append('\u0068');
					break;
				case '\u1FCB':
					output.append('\u0068');
					break;
				case '\u1FCC':
					output.append('\u0068');
					break;
				case '\u1FD0':
					output.append('\u0069');
					break;
				case '\u1FD1':
					output.append('\u0069');
					break;
				case '\u1FD2':
					output.append('\u0069');
					break;
				case '\u1FD3':
					output.append('\u0069');
					break;
				case '\u1FD6':
					output.append('\u0069');
					break;
				case '\u1FD7':
					output.append('\u0069');
					break;
				case '\u1FD8':
					output.append('\u0069');
					break;
				case '\u1FD9':
					output.append('\u0069');
					break;
				case '\u1FDA':
					output.append('\u0069');
					break;
				case '\u1FDB':
					output.append('\u0069');
					break;
				case '\u1FE8':
					output.append('\u0079');
					break;
				case '\u1FE9':
					output.append('\u0079');
					break;
				case '\u1FEA':
					output.append('\u0079');
					break;
				case '\u1FEB':
					output.append('\u0079');
					break;
				case '\u1FEC':
					output.append('\u0070');
					break;
				case '\u1FF8':
					output.append('\u006F');
					break;
				case '\u1FF9':
					output.append('\u006F');
					break;
				case '\u00C0':
					output.append('\u0061');
					break;
				case '\u00C1':
					output.append('\u0061');
					break;
				case '\u00C2':
					output.append('\u0061');
					break;
				case '\u00C3':
					output.append('\u0061');
					break;
				case '\u00C4':
					output.append('\u0061');
					break;
				case '\u00C5':
					output.append('\u0061');
					break;
				case '\u00C7':
					output.append('\u0063');
					break;
				case '\u00C8':
					output.append('\u0045');
					break;
				case '\u00C9':
					output.append('\u0045');
					break;
				case '\u00CA':
					output.append('\u0045');
					break;
				case '\u00CB':
					output.append('\u0045');
					break;
				case '\u00CC':
					output.append('\u0069');
					break;
				case '\u00CD':
					output.append('\u0069');
					break;
				case '\u00CE':
					output.append('\u0069');
					break;
				case '\u00CF':
					output.append('\u0069');
					break;
				case '\u00D0':
					output.append('\u0064');
					break;
				case '\u00D1':
					output.append('\u006E');
					break;
				case '\u00D2':
					output.append('\u006F');
					break;
				case '\u00D3':
					output.append('\u006F');
					break;
				case '\u00D4':
					output.append('\u006F');
					break;
				case '\u00D5':
					output.append('\u006F');
					break;
				case '\u00D6':
					output.append('\u006F');
					break;
				case '\u00D8':
					output.append('\u006F');
					break;
				case '\u00D9':
					output.append('\u0075');
					break;
				case '\u00DA':
					output.append('\u0075');
					break;
				case '\u00DB':
					output.append('\u0075');
					break;
				case '\u00DC':
					output.append('\u0075');
					break;
				case '\u00DD':
					output.append('\u0079');
					break;
				case '\u00E0':
					output.append('\u0061');
					break;
				case '\u00E1':
					output.append('\u0061');
					break;
				case '\u00E2':
					output.append('\u0061');
					break;
				case '\u00E3':
					output.append('\u0061');
					break;
				case '\u00E4':
					output.append('\u0061');
					break;
				case '\u00E5':
					output.append('\u0061');
					break;
				case '\u00E7':
					output.append('\u0063');
					break;
				case '\u00E8':
					output.append('\u0065');
					break;
				case '\u00E9':
					output.append('\u0065');
					break;
				case '\u00EA':
					output.append('\u0065');
					break;
				case '\u00EB':
					output.append('\u0065');
					break;
				case '\u00EC':
					output.append('\u0069');
					break;
				case '\u00ED':
					output.append('\u0069');
					break;
				case '\u00EE':
					output.append('\u0069');
					break;
				case '\u00EF':
					output.append('\u0069');
					break;
				case '\u00F1':
					output.append('\u006E');
					break;
				case '\u00F2':
					output.append('\u006F');
					break;
				case '\u00F3':
					output.append('\u006F');
					break;
				case '\u00F4':
					output.append('\u006F');
					break;
				case '\u00F5':
					output.append('\u006F');
					break;
				case '\u00F6':
					output.append('\u006F');
					break;
				case '\u00F8':
					output.append('\u006F');
					break;
				case '\u00F9':
					output.append('\u0075');
					break;
				case '\u00FA':
					output.append('\u0075');
					break;
				case '\u00FB':
					output.append('\u0075');
					break;
				case '\u00FC':
					output.append('\u0075');
					break;
				case '\u00FD':
					output.append('\u0079');
					break;
				case '\u00FF':
					output.append('\u0079');
					break;
				case '\u0100':
					output.append('\u0061');
					break;
				case '\u0101':
					output.append('\u0061');
					break;
				case '\u0102':
					output.append('\u0061');
					break;
				case '\u0103':
					output.append('\u0061');
					break;
				case '\u0104':
					output.append('\u0061');
					break;
				case '\u0105':
					output.append('\u0061');
					break;
				case '\u0106':
					output.append('\u0063');
					break;
				case '\u0107':
					output.append('\u0063');
					break;
				case '\u0108':
					output.append('\u0063');
					break;
				case '\u0109':
					output.append('\u0063');
					break;
				case '\u010A':
					output.append('\u0063');
					break;
				case '\u010B':
					output.append('\u0063');
					break;
				case '\u010C':
					output.append('\u0063');
					break;
				case '\u010D':
					output.append('\u0063');
					break;
				case '\u010E':
					output.append('\u0064');
					break;
				case '\u010F':
					output.append('\u0064');
					break;
				case '\u0110':
					output.append('\u0064');
					break;
				case '\u0111':
					output.append('\u0064');
					break;
				case '\u0112':
					output.append('\u0065');
					break;
				case '\u0113':
					output.append('\u0065');
					break;
				case '\u0114':
					output.append('\u0065');
					break;
				case '\u0115':
					output.append('\u0065');
					break;
				case '\u0116':
					output.append('\u0065');
					break;
				case '\u0117':
					output.append('\u0065');
					break;
				case '\u0118':
					output.append('\u0065');
					break;
				case '\u0119':
					output.append('\u0065');
					break;
				case '\u011A':
					output.append('\u0065');
					break;
				case '\u011B':
					output.append('\u0065');
					break;
				case '\u011C':
					output.append('\u0067');
					break;
				case '\u011D':
					output.append('\u0067');
					break;
				case '\u011E':
					output.append('\u0067');
					break;
				case '\u011F':
					output.append('\u0067');
					break;
				case '\u0120':
					output.append('\u0067');
					break;
				case '\u0121':
					output.append('\u0067');
					break;
				case '\u0122':
					output.append('\u0067');
					break;
				case '\u0123':
					output.append('\u0067');
					break;
				case '\u0124':
					output.append('\u0068');
					break;
				case '\u0125':
					output.append('\u0068');
					break;
				case '\u0126':
					output.append('\u0068');
					break;
				case '\u0127':
					output.append('\u0068');
					break;
				case '\u0128':
					output.append('\u0069');
					break;
				case '\u0129':
					output.append('\u0069');
					break;
				case '\u012A':
					output.append('\u0069');
					break;
				case '\u012B':
					output.append('\u0069');
					break;
				case '\u012C':
					output.append('\u0069');
					break;
				case '\u012D':
					output.append('\u0069');
					break;
				case '\u012E':
					output.append('\u0069');
					break;
				case '\u012F':
					output.append('\u0069');
					break;
				case '\u0130':
					output.append('\u0069');
					break;
				case '\u0131':
					output.append('\u0069');
					break;
				case '\u0134':
					output.append('\u006A');
					break;
				case '\u0135':
					output.append('\u006A');
					break;
				case '\u0136':
					output.append('\u006B');
					break;
				case '\u0137':
					output.append('\u006B');
					break;
				case '\u0138':
					output.append('\u006B');
					break;
				case '\u0139':
					output.append('\u006C');
					break;
				case '\u013A':
					output.append('\u006C');
					break;
				case '\u013B':
					output.append('\u006C');
					break;
				case '\u013C':
					output.append('\u006C');
					break;
				case '\u013D':
					output.append('\u006C');
					break;
				case '\u013E':
					output.append('\u006C');
					break;
				case '\u013F':
					output.append('\u006C');
					break;
				case '\u0140':
					output.append('\u006C');
					break;
				case '\u0141':
					output.append('\u006C');
					break;
				case '\u0142':
					output.append('\u006C');
					break;
				case '\u0143':
					output.append('\u006E');
					break;
				case '\u0144':
					output.append('\u006E');
					break;
				case '\u0145':
					output.append('\u006E');
					break;
				case '\u0146':
					output.append('\u006E');
					break;
				case '\u0147':
					output.append('\u006E');
					break;
				case '\u0148':
					output.append('\u006E');
					break;
				case '\u0149':
					output.append('\u006E');
					break;
				case '\u014A':
					output.append('\u006E');
					break;
				case '\u014B':
					output.append('\u006E');
					break;
				case '\u014C':
					output.append('\u006F');
					break;
				case '\u014D':
					output.append('\u006F');
					break;
				case '\u014E':
					output.append('\u006F');
					break;
				case '\u014F':
					output.append('\u006F');
					break;
				case '\u0150':
					output.append('\u006F');
					break;
				case '\u0151':
					output.append('\u006F');
					break;
				case '\u0154':
					output.append('\u0072');
					break;
				case '\u0155':
					output.append('\u0072');
					break;
				case '\u0156':
					output.append('\u0072');
					break;
				case '\u0157':
					output.append('\u0072');
					break;
				case '\u0158':
					output.append('\u0072');
					break;
				case '\u0159':
					output.append('\u0072');
					break;
				case '\u015A':
					output.append('\u0073');
					break;
				case '\u015B':
					output.append('\u0073');
					break;
				case '\u015C':
					output.append('\u0073');
					break;
				case '\u015D':
					output.append('\u0073');
					break;
				case '\u015E':
					output.append('\u0073');
					break;
				case '\u015F':
					output.append('\u0073');
					break;
				case '\u0160':
					output.append('\u0073');
					break;
				case '\u0161':
					output.append('\u0073');
					break;
				case '\u0162':
					output.append('\u0074');
					break;
				case '\u0163':
					output.append('\u0074');
					break;
				case '\u0164':
					output.append('\u0074');
					break;
				case '\u0165':
					output.append('\u0074');
					break;
				case '\u0166':
					output.append('\u0074');
					break;
				case '\u0167':
					output.append('\u0074');
					break;
				case '\u0168':
					output.append('\u0075');
					break;
				case '\u0169':
					output.append('\u0075');
					break;
				case '\u016A':
					output.append('\u0075');
					break;
				case '\u016B':
					output.append('\u0075');
					break;
				case '\u016C':
					output.append('\u0075');
					break;
				case '\u016D':
					output.append('\u0075');
					break;
				case '\u016E':
					output.append('\u0075');
					break;
				case '\u016F':
					output.append('\u0075');
					break;
				case '\u0170':
					output.append('\u0075');
					break;
				case '\u0171':
					output.append('\u0075');
					break;
				case '\u0172':
					output.append('\u0075');
					break;
				case '\u0173':
					output.append('\u0075');
					break;
				case '\u0174':
					output.append('\u0077');
					break;
				case '\u0175':
					output.append('\u0077');
					break;
				case '\u0176':
					output.append('\u0079');
					break;
				case '\u0177':
					output.append('\u0079');
					break;
				case '\u0178':
					output.append('\u0079');
					break;
				case '\u0179':
					output.append('\u007A');
					break;
				case '\u017A':
					output.append('\u007A');
					break;
				case '\u017B':
					output.append('\u007A');
					break;
				case '\u017C':
					output.append('\u007A');
					break;
				case '\u017D':
					output.append('\u007A');
					break;
				case '\u017E':
					output.append('\u007A');
					break;
				case '\u0180':
					output.append('\u0062');
					break;
				case '\u0181':
					output.append('\u0062');
					break;
				case '\u0182':
					output.append('\u0062');
					break;
				case '\u0183':
					output.append('\u0062');
					break;
				case '\u0184':
					output.append('\u0062');
					break;
				case '\u0185':
					output.append('\u0062');
					break;
				case '\u0187':
					output.append('\u0063');
					break;
				case '\u0188':
					output.append('\u0063');
					break;
				case '\u0189':
					output.append('\u0064');
					break;
				case '\u018A':
					output.append('\u0064');
					break;
				case '\u018B':
					output.append('\u0064');
					break;
				case '\u018C':
					output.append('\u0064');
					break;
				case '\u0191':
					output.append('\u0066');
					break;
				case '\u0192':
					output.append('\u0066');
					break;
				case '\u0193':
					output.append('\u0067');
					break;
				case '\u0197':
					output.append('\u0069');
					break;
				case '\u0198':
					output.append('\u006B');
					break;
				case '\u0199':
					output.append('\u006B');
					break;
				case '\u019A':
					output.append('\u006C');
					break;
				case '\u019D':
					output.append('\u006E');
					break;
				case '\u019E':
					output.append('\u006E');
					break;
				case '\u019F':
					output.append('\u006F');
					break;
				case '\u01A0':
					output.append('\u006F');
					break;
				case '\u01A1':
					output.append('\u006F');
					break;
				case '\u01A4':
					output.append('\u0070');
					break;
				case '\u01A5':
					output.append('\u0070');
					break;
				case '\u01AB':
					output.append('\u0074');
					break;
				case '\u01AC':
					output.append('\u0074');
					break;
				case '\u01AD':
					output.append('\u0074');
					break;
				case '\u01AE':
					output.append('\u0074');
					break;
				case '\u01AF':
					output.append('\u0075');
					break;
				case '\u01B0':
					output.append('\u0075');
					break;
				case '\u01B3':
					output.append('\u0079');
					break;
				case '\u01B4':
					output.append('\u0079');
					break;
				case '\u01B5':
					output.append('\u007A');
					break;
				case '\u01B6':
					output.append('\u007A');
					break;
				case '\u01CD':
					output.append('\u0061');
					break;
				case '\u01CE':
					output.append('\u0061');
					break;
				case '\u01CF':
					output.append('\u0069');
					break;
				case '\u01D0':
					output.append('\u0069');
					break;
				case '\u01D1':
					output.append('\u006F');
					break;
				case '\u01D2':
					output.append('\u006F');
					break;
				case '\u01D3':
					output.append('\u0075');
					break;
				case '\u01D4':
					output.append('\u0075');
					break;
				case '\u01D5':
					output.append('\u0075');
					break;
				case '\u01D6':
					output.append('\u0075');
					break;
				case '\u01D7':
					output.append('\u0075');
					break;
				case '\u01D8':
					output.append('\u0075');
					break;
				case '\u01D9':
					output.append('\u0075');
					break;
				case '\u01DA':
					output.append('\u0075');
					break;
				case '\u01DB':
					output.append('\u0075');
					break;
				case '\u01DC':
					output.append('\u0075');
					break;
				case '\u01DE':
					output.append('\u0061');
					break;
				case '\u01DF':
					output.append('\u0061');
					break;
				case '\u01E0':
					output.append('\u0061');
					break;
				case '\u01E1':
					output.append('\u0061');
					break;
				case '\u01E4':
					output.append('\u0067');
					break;
				case '\u01E5':
					output.append('\u0067');
					break;
				case '\u01E6':
					output.append('\u0067');
					break;
				case '\u01E7':
					output.append('\u0067');
					break;
				case '\u01E8':
					output.append('\u006B');
					break;
				case '\u01E9':
					output.append('\u006B');
					break;
				case '\u01EA':
					output.append('\u006F');
					break;
				case '\u01EB':
					output.append('\u006F');
					break;
				case '\u01EC':
					output.append('\u006F');
					break;
				case '\u01ED':
					output.append('\u006F');
					break;
				case '\u01F0':
					output.append('\u006A');
					break;
				case '\u01F4':
					output.append('\u0067');
					break;
				case '\u01F5':
					output.append('\u0067');
					break;
				case '\u01F8':
					output.append('\u006E');
					break;
				case '\u01F9':
					output.append('\u006E');
					break;
				case '\u01FA':
					output.append('\u0061');
					break;
				case '\u01FB':
					output.append('\u0061');
					break;
				case '\u01FE':
					output.append('\u006F');
					break;
				case '\u01FF':
					output.append('\u006F');
					break;
				case '\u0200':
					output.append('\u0061');
					break;
				case '\u0201':
					output.append('\u0061');
					break;
				case '\u0202':
					output.append('\u0061');
					break;
				case '\u0203':
					output.append('\u0061');
					break;
				case '\u0204':
					output.append('\u0065');
					break;
				case '\u0205':
					output.append('\u0065');
					break;
				case '\u0206':
					output.append('\u0065');
					break;
				case '\u0207':
					output.append('\u0065');
					break;
				case '\u0208':
					output.append('\u0069');
					break;
				case '\u0209':
					output.append('\u0069');
					break;
				case '\u020A':
					output.append('\u0069');
					break;
				case '\u020B':
					output.append('\u0069');
					break;
				case '\u020C':
					output.append('\u006F');
					break;
				case '\u020D':
					output.append('\u006F');
					break;
				case '\u020E':
					output.append('\u006F');
					break;
				case '\u020F':
					output.append('\u006F');
					break;
				case '\u0210':
					output.append('\u0072');
					break;
				case '\u0211':
					output.append('\u0072');
					break;
				case '\u0212':
					output.append('\u0072');
					break;
				case '\u0213':
					output.append('\u0072');
					break;
				case '\u0214':
					output.append('\u0075');
					break;
				case '\u0215':
					output.append('\u0075');
					break;
				case '\u0216':
					output.append('\u0075');
					break;
				case '\u0217':
					output.append('\u0075');
					break;
				case '\u0218':
					output.append('\u0073');
					break;
				case '\u0219':
					output.append('\u0073');
					break;
				case '\u021A':
					output.append('\u0074');
					break;
				case '\u021B':
					output.append('\u0074');
					break;
				case '\u021E':
					output.append('\u0068');
					break;
				case '\u021F':
					output.append('\u0068');
					break;
				case '\u0220':
					output.append('\u006E');
					break;
				case '\u0221':
					output.append('\u0064');
					break;
				case '\u0224':
					output.append('\u007A');
					break;
				case '\u0225':
					output.append('\u007A');
					break;
				case '\u0226':
					output.append('\u0061');
					break;
				case '\u0227':
					output.append('\u0061');
					break;
				case '\u0228':
					output.append('\u0065');
					break;
				case '\u0229':
					output.append('\u0065');
					break;
				case '\u022A':
					output.append('\u006F');
					break;
				case '\u022B':
					output.append('\u006F');
					break;
				case '\u022C':
					output.append('\u006F');
					break;
				case '\u022D':
					output.append('\u006F');
					break;
				case '\u022E':
					output.append('\u006F');
					break;
				case '\u022F':
					output.append('\u006F');
					break;
				case '\u0230':
					output.append('\u006F');
					break;
				case '\u0231':
					output.append('\u006F');
					break;
				case '\u0232':
					output.append('\u0079');
					break;
				case '\u0233':
					output.append('\u0079');
					break;
				case '\u0234':
					output.append('\u006C');
					break;
				case '\u0235':
					output.append('\u006E');
					break;
				case '\u0236':
					output.append('\u0074');
					break;
				case '\u023A':
					output.append('\u0061');
					break;
				case '\u023B':
					output.append('\u0063');
					break;
				case '\u023C':
					output.append('\u0063');
					break;
				case '\u023D':
					output.append('\u006C');
					break;
				case '\u023E':
					output.append('\u0074');
					break;
				case '\u023F':
					output.append('\u0073');
					break;
				case '\u0240':
					output.append('\u007A');
					break;
				case '\u1E00':
					output.append('\u0061');
					break;
				case '\u1E01':
					output.append('\u0061');
					break;
				case '\u1E02':
					output.append('\u0062');
					break;
				case '\u0E03':
					output.append('\u0062');
					break;
				case '\u0E04':
					output.append('\u0062');
					break;
				case '\u1E05':
					output.append('\u0062');
					break;
				case '\u1E06':
					output.append('\u0062');
					break;
				case '\u1E07':
					output.append('\u0062');
					break;
				case '\u1E08':
					output.append('\u0063');
					break;
				case '\u1E09':
					output.append('\u0063');
					break;
				case '\u1E0A':
					output.append('\u0064');
					break;
				case '\u1E0B':
					output.append('\u0064');
					break;
				case '\u1E0C':
					output.append('\u0064');
					break;
				case '\u1E0D':
					output.append('\u0064');
					break;
				case '\u1E0E':
					output.append('\u0064');
					break;
				case '\u1E0F':
					output.append('\u0064');
					break;
				case '\u1E10':
					output.append('\u0064');
					break;
				case '\u1E11':
					output.append('\u0064');
					break;
				case '\u1E12':
					output.append('\u0064');
					break;
				case '\u1E13':
					output.append('\u0064');
					break;
				case '\u1E14':
					output.append('\u0065');
					break;
				case '\u1E15':
					output.append('\u0065');
					break;
				case '\u1E16':
					output.append('\u0065');
					break;
				case '\u1E17':
					output.append('\u0065');
					break;
				case '\u1E18':
					output.append('\u0065');
					break;
				case '\u1E19':
					output.append('\u0065');
					break;
				case '\u1E1A':
					output.append('\u0065');
					break;
				case '\u1E1B':
					output.append('\u0065');
					break;
				case '\u1E1C':
					output.append('\u0065');
					break;
				case '\u1E1D':
					output.append('\u0065');
					break;
				case '\u1E1E':
					output.append('\u0066');
					break;
				case '\u1E1F':
					output.append('\u0066');
					break;
				case '\u1E20':
					output.append('\u0067');
					break;
				case '\u1E21':
					output.append('\u0067');
					break;
				case '\u1E22':
					output.append('\u0068');
					break;
				case '\u1E23':
					output.append('\u0068');
					break;
				case '\u1E24':
					output.append('\u0068');
					break;
				case '\u1E25':
					output.append('\u0068');
					break;
				case '\u1E26':
					output.append('\u0068');
					break;
				case '\u1E27':
					output.append('\u0068');
					break;
				case '\u1E28':
					output.append('\u0068');
					break;
				case '\u1E29':
					output.append('\u0068');
					break;
				case '\u1E2A':
					output.append('\u0068');
					break;
				case '\u1E2B':
					output.append('\u0068');
					break;
				case '\u1E2C':
					output.append('\u0069');
					break;
				case '\u1E2D':
					output.append('\u0069');
					break;
				case '\u1E2E':
					output.append('\u0069');
					break;
				case '\u1E2F':
					output.append('\u0069');
					break;
				case '\u1E30':
					output.append('\u006B');
					break;
				case '\u1E31':
					output.append('\u006B');
					break;
				case '\u1E32':
					output.append('\u006B');
					break;
				case '\u1E33':
					output.append('\u006B');
					break;
				case '\u1E34':
					output.append('\u006B');
					break;
				case '\u1E35':
					output.append('\u006B');
					break;
				case '\u1E36':
					output.append('\u006C');
					break;
				case '\u1E37':
					output.append('\u006C');
					break;
				case '\u1E38':
					output.append('\u006C');
					break;
				case '\u1E39':
					output.append('\u006C');
					break;
				case '\u1E3A':
					output.append('\u006C');
					break;
				case '\u1E3B':
					output.append('\u006C');
					break;
				case '\u1E3C':
					output.append('\u006C');
					break;
				case '\u1E3D':
					output.append('\u006C');
					break;
				case '\u1E3E':
					output.append('\u006D');
					break;
				case '\u1E3F':
					output.append('\u006D');
					break;
				case '\u1E40':
					output.append('\u006D');
					break;
				case '\u1E41':
					output.append('\u006D');
					break;
				case '\u1E42':
					output.append('\u006D');
					break;
				case '\u1E43':
					output.append('\u006D');
					break;
				case '\u1E44':
					output.append('\u006E');
					break;
				case '\u1E45':
					output.append('\u006E');
					break;
				case '\u1E46':
					output.append('\u006E');
					break;
				case '\u1E47':
					output.append('\u006E');
					break;
				case '\u1E48':
					output.append('\u006E');
					break;
				case '\u1E49':
					output.append('\u006E');
					break;
				case '\u1E4A':
					output.append('\u006E');
					break;
				case '\u1E4B':
					output.append('\u006E');
					break;
				case '\u1E4C':
					output.append('\u006F');
					break;
				case '\u1E4D':
					output.append('\u006F');
					break;
				case '\u1E4E':
					output.append('\u006F');
					break;
				case '\u1E4F':
					output.append('\u006F');
					break;
				case '\u1E50':
					output.append('\u006F');
					break;
				case '\u1E51':
					output.append('\u006F');
					break;
				case '\u1E52':
					output.append('\u006F');
					break;
				case '\u1E53':
					output.append('\u006F');
					break;
				case '\u1E54':
					output.append('\u0070');
					break;
				case '\u1E55':
					output.append('\u0070');
					break;
				case '\u1E56':
					output.append('\u0070');
					break;
				case '\u1E57':
					output.append('\u0070');
					break;
				case '\u1E58':
					output.append('\u0072');
					break;
				case '\u1E59':
					output.append('\u0072');
					break;
				case '\u1E5A':
					output.append('\u0072');
					break;
				case '\u1E5B':
					output.append('\u0072');
					break;
				case '\u1E5C':
					output.append('\u0072');
					break;
				case '\u1E5D':
					output.append('\u0072');
					break;
				case '\u1E5E':
					output.append('\u0072');
					break;
				case '\u1E5F':
					output.append('\u0072');
					break;
				case '\u1E60':
					output.append('\u0073');
					break;
				case '\u1E61':
					output.append('\u0073');
					break;
				case '\u1E62':
					output.append('\u0073');
					break;
				case '\u1E63':
					output.append('\u0073');
					break;
				case '\u1E64':
					output.append('\u0073');
					break;
				case '\u1E65':
					output.append('\u0073');
					break;
				case '\u1E66':
					output.append('\u0073');
					break;
				case '\u1E67':
					output.append('\u0073');
					break;
				case '\u1E68':
					output.append('\u0073');
					break;
				case '\u1E69':
					output.append('\u0073');
					break;
				case '\u1E6A':
					output.append('\u0074');
					break;
				case '\u1E6B':
					output.append('\u0074');
					break;
				case '\u1E6C':
					output.append('\u0074');
					break;
				case '\u1E6D':
					output.append('\u0074');
					break;
				case '\u1E6E':
					output.append('\u0074');
					break;
				case '\u1E6F':
					output.append('\u0074');
					break;
				case '\u1E70':
					output.append('\u0074');
					break;
				case '\u1E71':
					output.append('\u0074');
					break;
				case '\u1E72':
					output.append('\u0075');
					break;
				case '\u1E73':
					output.append('\u0075');
					break;
				case '\u1E74':
					output.append('\u0075');
					break;
				case '\u1E75':
					output.append('\u0075');
					break;
				case '\u1E76':
					output.append('\u0075');
					break;
				case '\u1E77':
					output.append('\u0075');
					break;
				case '\u1E78':
					output.append('\u0075');
					break;
				case '\u1E79':
					output.append('\u0075');
					break;
				case '\u1E7A':
					output.append('\u0075');
					break;
				case '\u1E7B':
					output.append('\u0075');
					break;
				case '\u1E7C':
					output.append('\u0076');
					break;
				case '\u1E7D':
					output.append('\u0076');
					break;
				case '\u1E7E':
					output.append('\u0076');
					break;
				case '\u1E7F':
					output.append('\u0076');
					break;
				case '\u1E80':
					output.append('\u0077');
					break;
				case '\u1E81':
					output.append('\u0077');
					break;
				case '\u1E82':
					output.append('\u0077');
					break;
				case '\u1E83':
					output.append('\u0077');
					break;
				case '\u1E84':
					output.append('\u0077');
					break;
				case '\u1E85':
					output.append('\u0077');
					break;
				case '\u1E86':
					output.append('\u0077');
					break;
				case '\u1E87':
					output.append('\u0077');
					break;
				case '\u1E88':
					output.append('\u0077');
					break;
				case '\u1E89':
					output.append('\u0077');
					break;
				case '\u1E8A':
					output.append('\u0078');
					break;
				case '\u1E8B':
					output.append('\u0078');
					break;
				case '\u1E8C':
					output.append('\u0078');
					break;
				case '\u1E8D':
					output.append('\u0078');
					break;
				case '\u1E8E':
					output.append('\u0079');
					break;
				case '\u1E8F':
					output.append('\u0079');
					break;
				case '\u1E90':
					output.append('\u007A');
					break;
				case '\u1E91':
					output.append('\u007A');
					break;
				case '\u1E92':
					output.append('\u007A');
					break;
				case '\u1E93':
					output.append('\u007A');
					break;
				case '\u1E94':
					output.append('\u007A');
					break;
				case '\u1E95':
					output.append('\u007A');
					break;
				case '\u1E96':
					output.append('\u0068');
					break;
				case '\u1E97':
					output.append('\u0074');
					break;
				case '\u1E98':
					output.append('\u0077');
					break;
				case '\u1E99':
					output.append('\u0079');
					break;
				case '\u1E9A':
					output.append('\u0061');
					break;
				case '\u1E9B':
					output.append('\u0066');
					break;
				case '\u1EA0':
					output.append('\u0061');
					break;
				case '\u1EA1':
					output.append('\u0061');
					break;
				case '\u1EA2':
					output.append('\u0061');
					break;
				case '\u1EA3':
					output.append('\u0061');
					break;
				case '\u1EA4':
					output.append('\u0061');
					break;
				case '\u1EA5':
					output.append('\u0061');
					break;
				case '\u1EA6':
					output.append('\u0061');
					break;
				case '\u1EA7':
					output.append('\u0061');
					break;
				case '\u1EA8':
					output.append('\u0061');
					break;
				case '\u1EA9':
					output.append('\u0061');
					break;
				case '\u1EAA':
					output.append('\u0061');
					break;
				case '\u1EAB':
					output.append('\u0061');
					break;
				case '\u1EAC':
					output.append('\u0061');
					break;
				case '\u1EAD':
					output.append('\u0061');
					break;
				case '\u1EAE':
					output.append('\u0061');
					break;
				case '\u1EAF':
					output.append('\u0061');
					break;
				case '\u1EB0':
					output.append('\u0061');
					break;
				case '\u1EB1':
					output.append('\u0061');
					break;
				case '\u1EB2':
					output.append('\u0061');
					break;
				case '\u1EB3':
					output.append('\u0061');
					break;
				case '\u1EB4':
					output.append('\u0061');
					break;
				case '\u1EB5':
					output.append('\u0061');
					break;
				case '\u1EB6':
					output.append('\u0061');
					break;
				case '\u1EB7':
					output.append('\u0061');
					break;
				case '\u1EB8':
					output.append('\u0065');
					break;
				case '\u1EB9':
					output.append('\u0065');
					break;
				case '\u1EBA':
					output.append('\u0065');
					break;
				case '\u1EBB':
					output.append('\u0065');
					break;
				case '\u1EBC':
					output.append('\u0065');
					break;
				case '\u1EBD':
					output.append('\u0065');
					break;
				case '\u1EBE':
					output.append('\u0065');
					break;
				case '\u1EBF':
					output.append('\u0065');
					break;
				case '\u1EC0':
					output.append('\u0065');
					break;
				case '\u1EC1':
					output.append('\u0065');
					break;
				case '\u1EC2':
					output.append('\u0065');
					break;
				case '\u1EC3':
					output.append('\u0065');
					break;
				case '\u1EC4':
					output.append('\u0065');
					break;
				case '\u1EC5':
					output.append('\u0065');
					break;
				case '\u1EC6':
					output.append('\u0065');
					break;
				case '\u1EC7':
					output.append('\u0065');
					break;
				case '\u1EC8':
					output.append('\u0069');
					break;
				case '\u1EC9':
					output.append('\u0069');
					break;
				case '\u1ECA':
					output.append('\u0069');
					break;
				case '\u1ECB':
					output.append('\u0069');
					break;
				case '\u1ECC':
					output.append('\u006F');
					break;
				case '\u1ECD':
					output.append('\u006F');
					break;
				case '\u1ECE':
					output.append('\u006F');
					break;
				case '\u1ECF':
					output.append('\u006F');
					break;
				case '\u1ED0':
					output.append('\u006F');
					break;
				case '\u1ED1':
					output.append('\u006F');
					break;
				case '\u1ED2':
					output.append('\u006F');
					break;
				case '\u1ED3':
					output.append('\u006F');
					break;
				case '\u1ED4':
					output.append('\u006F');
					break;
				case '\u1ED5':
					output.append('\u006F');
					break;
				case '\u1ED6':
					output.append('\u006F');
					break;
				case '\u1ED7':
					output.append('\u006F');
					break;
				case '\u1ED8':
					output.append('\u006F');
					break;
				case '\u1ED9':
					output.append('\u006F');
					break;
				case '\u1EDA':
					output.append('\u006F');
					break;
				case '\u1EDB':
					output.append('\u006F');
					break;
				case '\u1EDC':
					output.append('\u006F');
					break;
				case '\u1EDD':
					output.append('\u006F');
					break;
				case '\u1EDE':
					output.append('\u006F');
					break;
				case '\u1EDF':
					output.append('\u006F');
					break;
				case '\u1EE0':
					output.append('\u006F');
					break;
				case '\u1EE1':
					output.append('\u006F');
					break;
				case '\u1EE2':
					output.append('\u006F');
					break;
				case '\u1EE3':
					output.append('\u006F');
					break;
				case '\u1EE4':
					output.append('\u0075');
					break;
				case '\u1EE5':
					output.append('\u0075');
					break;
				case '\u1EE6':
					output.append('\u0075');
					break;
				case '\u1EE7':
					output.append('\u0075');
					break;
				case '\u1EE8':
					output.append('\u0075');
					break;
				case '\u1EE9':
					output.append('\u0075');
					break;
				case '\u1EEA':
					output.append('\u0075');
					break;
				case '\u1EEB':
					output.append('\u0075');
					break;
				case '\u1EEC':
					output.append('\u0075');
					break;
				case '\u1EED':
					output.append('\u0075');
					break;
				case '\u1EEE':
					output.append('\u0075');
					break;
				case '\u1EEF':
					output.append('\u0075');
					break;
				case '\u1EF0':
					output.append('\u0075');
					break;
				case '\u1EF1':
					output.append('\u0075');
					break;
				case '\u1EF2':
					output.append('\u0079');
					break;
				case '\u1EF3':
					output.append('\u0079');
					break;
				case '\u1EF4':
					output.append('\u0079');
					break;
				case '\u1EF5':
					output.append('\u0079');
					break;
				case '\u1EF6':
					output.append('\u0079');
					break;
				case '\u1EF7':
					output.append('\u0079');
					break;
				case '\u1EF8':
					output.append('\u0079');
					break;
				case '\u1EF9':
					output.append('\u0079');
					break;
				case '\u00C6': // Æ
					output.append("AE");
					break;
				case '\u0152':
					output.append("OE");
					break;
				case '\u00DE':
					output.append("TH");
					break;
				case '\u00E6': // æ
					output.append("ae");
					break;
				case '\u0153':
					output.append("oe");
					break;
				default:
					output.append(input.charAt(i));
					break;
			}
		}
		return output.toString();
	}

	public final static String cleanPonctuation(String input) {
		final StringBuffer output = new StringBuffer();
		for (int i = 0; i < input.length(); i++) {
			switch (input.charAt(i)) {
				case '(':
					output.append("");
					break;
				case ')':
					output.append("");
					break;
				case '&':
					output.append(" ");
					break;
				case '[':
					output.append("");
					break;
				case ']':
					output.append("");
					break;
				case ',':
					output.append(" ");
					break;
				case ';':
					output.append(" ");
					break;
				case ':':
					output.append(" ");
					break;
				case '/':
					output.append(" ");
					break;
				case '-':
					output.append(" ");
					break;
				case '=':
					output.append(" ");
					break;
				case '.':
					output.append("");
					break;
				case '@':
					output.append(" ");
					break;
				default:
					output.append(input.charAt(i));
					break;
			}
		}
		return output.toString().trim();
	}

	public final static String cleanPonctuationExceptDot(String input) {
		final StringBuffer output = new StringBuffer();
		for (int i = 0; i < input.length(); i++) {
			switch (input.charAt(i)) {
				case '(':
					output.append("");
					break;
				case ')':
					output.append("");
					break;
				case '&':
					output.append(" ");
					break;
				case '[':
					output.append("");
					break;
				case ']':
					output.append("");
					break;
				case ',':
					output.append(" ");
					break;
				case ';':
					output.append(" ");
					break;
				case ':':
					output.append(" ");
					break;
				case '/':
					output.append(" ");
					break;
				case '-':
					output.append(" ");
					break;
				case '=':
					output.append(" ");
					break;
				case '@':
					output.append(" ");
					break;
				default:
					output.append(input.charAt(i));
					break;
			}
		}
		return output.toString().trim();
	}

	public static String filter(String input) {
		input = input.toLowerCase();
		String[] words = input.split(" ");

		for (int i = 0; i < words.length; i++) {
			words[i] = removeApostrophe(words[i]);
			words[i] = removeAccents(words[i]);
		}
		return StringUtils.join(words, " ");
	}

	public static String cleanAll(String in) {
		if (in == null) {
			return null;
		}
		in = cleanPonctuation(in);
		in = removeAccents(in);
		in = removeApostrophe(in);
		return in;
	}

}