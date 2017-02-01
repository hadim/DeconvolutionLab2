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

package deconvolutionlab;

import imagej.IJImager;

import java.util.ArrayList;

import plugins.sage.deconvolutionlab.IcyImager;
import deconvolutionlab.monitor.Monitors;

public enum Platform {
	
	IMAGEJ, ICY, STANDALONE, MATLAB;
	
	public static ArrayList<PlatformImager> getImagers() {
		ArrayList<PlatformImager> imagers = new ArrayList<PlatformImager>();
		try {
			PlatformImager imager = new IJImager();
			if (imager != null) {
				imagers.add(imager);
			}
		}
		catch(NoClassDefFoundError ex) {
		}

		try {
			PlatformImager imager = new IcyImager();
			if (imager != null) {
				imagers.add(imager);
			}
		}
		catch(NoClassDefFoundError ex) {
		}
		
		imagers.add(new LabImager());
		return imagers;
	}
	
	public static ArrayList<String> getNameImagers() {
		ArrayList<PlatformImager> imagers = getImagers();
		ArrayList<String> names = new ArrayList<String>();
		for(PlatformImager imager : imagers)
			names.add(imager.getName());
		return names;
	}
};
