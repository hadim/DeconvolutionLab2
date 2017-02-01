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

package plugins.sage.deconvolutionlab;

import icy.file.Saver;
import icy.image.IcyBufferedImage;
import icy.imagej.ImageJUtil;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import ij.ImagePlus;
import ij.io.Opener;

import java.io.File;
import java.util.ArrayList;

import signal.ComplexComponent;
import signal.ComplexSignal;
import signal.RealSignal;
import deconvolutionlab.PlatformImager;
import deconvolutionlab.monitor.Monitors;

public class IcyImager extends PlatformImager {

	
	public static RealSignal create(Sequence seq) {
		int nx = seq.getSizeX();
		int ny = seq.getSizeY();
		int nz = seq.getSizeZ();
		RealSignal signal = new RealSignal(nx, ny, nz);
		for (int k = 0; k < nz; k++) {
			float pixels[] = new float[nx * ny];
			Array1DUtil.arrayToFloatArray(seq.getDataXY(0, k, 0), pixels, seq.isSignedDataType());
			signal.setXY(k, pixels);
		}
		return signal;
	}

	@Override
    public RealSignal create() {
		return build(Icy.getMainInterface().getActiveSequence());
	}
	
	@Override
    public RealSignal create(String name) {
		ArrayList<Sequence> sequences = Icy.getMainInterface().getSequences(name);
		for(Sequence sequence : sequences)
			if (sequence.getName().equals(name))
				return build(sequence);
		return null;
	}

	@Override
    public RealSignal open(String filename) {
		//File file = new File(filename);
		//return build(Loader.loadSequence(file, 0, false));
		Opener opener = new Opener();
		ImagePlus imp = opener.openImage(filename);
		Sequence seq = ImageJUtil.convertToIcySequence(imp, null);
		return build(seq);
	}

	@Override
	public void show(RealSignal signal, String title) {
		show(signal, title, PlatformImager.Type.FLOAT);
	}
	
	@Override
	public void show(RealSignal signal, String title, PlatformImager.Type type) {
		Sequence sequence = build(signal, type);
		Icy.getMainInterface().addSequence(sequence);
	}

	@Override
    public void show(ComplexSignal signal, String title, ComplexComponent complex) {
		Sequence sequence = new Sequence();
		for (int k = 0; k < signal.nz; k++) {
			float[] plane = null;
			switch(complex) {
				case REAL: plane = signal.getRealXY(k); break;
				case IMAGINARY: plane = signal.getImagXY(k); break;
				case MODULE: plane = signal.getModuleXY(k); break;
				default: plane = signal.getModuleXY_dB(k);
			}
			IcyBufferedImage image = new IcyBufferedImage(signal.nx, signal.ny, 1, DataType.FLOAT);
			Array1DUtil.floatArrayToSafeArray(plane, image.getDataXY(0), image.isSignedDataType());
			image.dataChanged();
			sequence.setImage(0, k, image);
		}
		sequence.setName(title);
		Icy.getMainInterface().addSequence(sequence);
    }
	
	@Override
    public void save(RealSignal signal, String filename) {
		save(signal, filename, PlatformImager.Type.FLOAT);
	}	
	
	@Override
	public void save(RealSignal signal, String filename, PlatformImager.Type type) {
		Sequence sequence = build(signal, type);
		File file = new File(filename);
		Saver.save(sequence, file, false, true);
	}
	
	private RealSignal build(Sequence sequence) {
		int nx = sequence.getSizeX();
		int ny = sequence.getSizeY();
		int nz = sequence.getSizeZ();
		RealSignal signal = new RealSignal(nx, ny, nz);
		for (int k = 0; k < nz; k++) {
			float pixels[] = new float[nx * ny];
			Array1DUtil.arrayToFloatArray(sequence.getDataXY(0, k, 0), pixels, sequence.isSignedDataType());
			signal.setXY(k, pixels);
		}
		return signal;
	}
	
	private Sequence build(RealSignal signal, PlatformImager.Type type) {
		Sequence sequence = new Sequence();
		for (int k = 0; k < signal.nz; k++) {
			float[] plane = signal.getXY(k);
			IcyBufferedImage image = null;
			switch(type) {
			case BYTE: 
				byte[] b = Array1DUtil.arrayToByteArray(plane);
				image = new IcyBufferedImage(signal.nx, signal.ny, 1, DataType.BYTE);
				Array1DUtil.byteArrayToArray(b, image.getDataXY(0), image.isSignedDataType());
				break;
			case SHORT: 
				short[] s = Array1DUtil.arrayToShortArray(plane, false);
				image = new IcyBufferedImage(signal.nx, signal.ny, 1, DataType.SHORT);
				Array1DUtil.shortArrayToArray(s, image.getDataXY(0), image.isSignedDataType());
				break;
			default: 
				image = new IcyBufferedImage(signal.nx, signal.ny, 1, DataType.FLOAT);
				Array1DUtil.floatArrayToArray(signal.data[k], image.getDataXY(0));
				break;
			}
			image.dataChanged();
			sequence.setImage(0, k, image);
		}
		return sequence;
	}

	@Override
	public String getName() {
		return "Icy";
	}

	@Override
	public void appendShowLive(String key, RealSignal signal, String title) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void appendShowLive(String key, RealSignal signal, String title, Type type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void show(RealSignal signal, String title, Type type, int z) {
		// TODO Auto-generated method stub	
	}
	
	@Override
	public void show(ComplexSignal signal, String title) {
		// TODO Auto-generated method stub	
	}


}
