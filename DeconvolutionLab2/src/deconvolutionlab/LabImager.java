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

import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.util.HashMap;

import signal.ComplexComponent;
import signal.ComplexSignal;
import signal.RealSignal;

public class LabImager extends PlatformImager {

	private static HashMap<String, ImageStack> stacks = new HashMap<String, ImageStack>();
	private static HashMap<String, ImagePlus> images = new HashMap<String, ImagePlus>();

	public static RealSignal create(ImagePlus imp) {
		int nx = imp.getWidth();
		int ny = imp.getHeight();
		int nz = imp.getStackSize();
		RealSignal signal = new RealSignal(nx, ny, nz);
		for (int k = 0; k < nz; k++) {
			ImageProcessor ip = imp.getStack().getProcessor(k + 1).convertToFloat();
			signal.setXY(k, (float[]) ip.getPixels());
		}
		return signal;
	}	

	@Override
	public RealSignal create() {
		return build(WindowManager.getCurrentImage());
	}

	@Override
	public RealSignal create(String name) {
		ImagePlus imp = WindowManager.getImage(name);
		if (imp == null)
			imp = WindowManager.getCurrentImage();
		return build(imp);
	}

	@Override
	public RealSignal open(String filename) {
		Opener opener = new Opener();
		ImagePlus imp = opener.openImage(filename);
		return build(imp);
	}
	
	@Override
	public void show(RealSignal signal, String title) {
		show(signal, title, PlatformImager.Type.FLOAT);
	}

	@Override
	public void show(RealSignal signal, String title, PlatformImager.Type type) {
		show(signal, title, type, signal.nz/2);
	}

	@Override
	public void show(RealSignal signal, String title, PlatformImager.Type type, int z) {
		ImagePlus imp = build(signal, type);
		if (imp != null) {
			imp.setTitle(title);
			int nz = imp.getStackSize();
			imp.show();
			imp.setSlice(Math.max(1, Math.min(nz, z)));
			imp.getProcessor().resetMinAndMax();
		}
	}
	
	@Override
	public void appendShowLive(String key, RealSignal signal, String title) {
		appendShowLive(key, signal, title, PlatformImager.Type.FLOAT);
	}

	@Override
	public void appendShowLive(String key, RealSignal signal, String title, PlatformImager.Type type) {
		ImagePlus imp = build(signal, type);
		ImagePlus image = images.get(key);
		ImageStack stack = stacks.get(key);
		if (image == null || stack == null) {
			stack = new ImageStack(signal.nx, signal.ny);
			stack.addSlice(imp.getProcessor());
			image = new ImagePlus(title, stack);
			image.show();
			images.put(key, image);
			stacks.put(key, stack);
		}
		else {
			stack.addSlice(imp.getProcessor());
			image.setStack(stack);
		}
		image.updateAndDraw();
		image.setSlice(image.getStack().getSize());
		image.getProcessor().resetMinAndMax();
	}

	@Override
	public void save(RealSignal signal, String filename) {
		save(signal, filename, PlatformImager.Type.FLOAT);
	}

	@Override
	public void save(RealSignal signal, String filename, PlatformImager.Type type) {
		ImagePlus imp = build(signal, type);
		if (imp != null) {
			if (imp.getStackSize() == 1) {
				new FileSaver(imp).saveAsTiff(filename);
			}
			else {
				new FileSaver(imp).saveAsTiffStack(filename);
			}
		}
	}

	@Override
    public void show(ComplexSignal signal, String title) {
		show(signal, title, ComplexComponent.MODULE);
	}
	
	@Override
    public void show(ComplexSignal signal, String title, ComplexComponent complex) {
		ImageStack stack = new ImageStack(signal.nx, signal.ny);
		for (int k = 0; k < signal.nz; k++) {
			float[] plane = null;
			switch(complex) {
				case REAL: plane = signal.getRealXY(k); break;
				case IMAGINARY: plane = signal.getImagXY(k); break;
				case MODULE: plane = signal.getModuleXY(k); break;
				default: plane = signal.getModuleXY_dB(k);
			}
			stack.addSlice(new FloatProcessor(signal.nx, signal.ny, plane));
		}
		new ImagePlus(title, stack).show();
	}
	
	private RealSignal build(ImagePlus imp) {
		if (imp == null)
			return null;
		int nx = imp.getWidth();
		int ny = imp.getHeight();
		int nz = imp.getStackSize();
		RealSignal signal = new RealSignal(nx, ny, nz);
		for (int k = 0; k < nz; k++) {
			ImageProcessor ip = imp.getStack().getProcessor(k + 1).convertToFloat();
			signal.setXY(k, (float[]) ip.getPixels());
		}
		return signal;
	}
	
	private ImagePlus build(RealSignal signal, PlatformImager.Type type) {
		if (signal == null)
			return null;
		
		ImageStack stack = new ImageStack(signal.nx, signal.ny);
		for (int k = 0; k < signal.nz; k++) {
			ImageProcessor ip = new FloatProcessor(signal.nx, signal.ny, signal.getXY(k));
			switch(type) {
				case BYTE:
					stack.addSlice(ip.convertToByteProcessor(false));
					break;
				case SHORT:
					stack.addSlice(ip.convertToShortProcessor(false));
					break;
				case FLOAT:
					stack.addSlice(ip);
				default:
					break;
			}
		}
		return new ImagePlus("", stack);
	}
	
	@Override
	public String getName() {
		return "ImageJ";
	}
}
