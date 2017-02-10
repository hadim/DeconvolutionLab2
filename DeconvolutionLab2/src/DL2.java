import ij.ImagePlus;
import ij.Macro;
import ij.WindowManager;

import java.io.File;

import matlab.Converter;
import deconvolution.Deconvolution;
import deconvolutionlab.Config;
import deconvolutionlab.Lab;
import deconvolutionlab.dialog.LabDialog;

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

/**
 * This class is dedicated to the Matlab interface for DeconvolutionLab2
 * @author sage
 *
 */
public class DL2 {

	public static void lab() {
		String config = System.getProperty("user.dir") + File.separator + "DeconvolutionLab2.config";
		Config.getInstance(config);
		LabDialog dialog = new LabDialog();
		dialog.setVisible(true);
	}

	public static void run(String command) {
		new Deconvolution(Macro.getOptions()).deconvolve(false);
	}	
	
	public static void launch(String command) {
		new Deconvolution(Macro.getOptions()).launch("matlab", false);
	}	

	public static Object get(String image) {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp != null)
			return Converter.get(imp);
		return null;
	}	

	public static void run(Object image, Object psf, String algo) {
		Converter.createImage("input", image, true);
		Converter.createImage("psf", psf, true);
		String cmd = " -image platform input -psf platform psf -algorithm " + algo;
		Deconvolution d = new Deconvolution(cmd);
		d.deconvolve(false);
	}
	
	public static void help() {
		Lab.help();
	}
	
	public static void clear() {
		int ids[] = WindowManager.getIDList();
		for(int id : ids) {
			ImagePlus imp = WindowManager.getImage(id);
			if (imp != null)
				imp.close();
		}
	}

}
