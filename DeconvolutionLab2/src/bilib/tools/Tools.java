package bilib.tools;

/*
 * DeconvolutionLab2
 * 
 * Conditions of use: You are free to use this software for research or
 * educational purposes. In addition, we expect you to include adequate
 * citations and acknowledgments whenever you present or publish results that
 * are based on it.
 * 
 * Reference: DeconvolutionLab2: An Open-Source Software for Deconvolution
 * Microscopy D. Sage, L. Donati, F. Soulez, D. Fortun, G. Schmit, A. Seitz,
 * R. Guiet, C. Vonesch, M Unser, Methods of Elsevier, 2017.
 */

/*
 * Copyright 2010-2017 Biomedical Imaging Group at the EPFL.
 * 
 * This file is part of DeconvolutionLab2 (DL2).
 * 
 * DL2 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * DL2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * DL2. If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.File;
import java.security.CodeSource;

public class Tools {

	public static String[] explodeLower(String line, String regex) {
		String[] items = line.split(regex);
		String[] out = new String[items.length];
		for(int i=0; i<items.length; i++)
			out[i] = items[i].trim().toLowerCase();
		return out;
	}

	public static String[] explodeCase(String line, String regex) {
		String[] items = line.split(regex);
		String[] out = new String[items.length];
		for(int i=0; i<items.length; i++)
			out[i] = items[i].trim();
		return out;
	}
	

	public static String ellipsis(String text, int length) {
		int l = text.length();
		if (l <= length)
			return text;
		String result = 
				text.substring(0,length/2) + 
				"..." + 
				text.substring(text.length()-length/2+3, text.length());
		return result;
	}
	
	public static String getWorkingPath() {
		try {
			CodeSource codeSource = Tools.class.getProtectionDomain().getCodeSource();
			File jarFile = new File(codeSource.getLocation().toURI().getPath());
			return jarFile.getParentFile().getPath() + File.separator;
		}
		catch(Exception ex) {
			return System.getProperty("user.dir") + File.separator;
		}
	}

}
