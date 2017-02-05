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

package signal.factory;

import java.util.ArrayList;

import javax.swing.SwingWorker;

import signal.RealSignal;

public abstract class SignalFactory {

	protected double	fractXC		= 0.5;
	protected double	fractYC		= 0.5;
	protected double	fractZC		= 0.5;
	protected double	background	= 0.0;
	protected double	amplitude	= 1.0;
	protected double	xc;
	protected double	yc;
	protected double	zc;
	protected int		nx;
	protected int		ny;
	protected int		nz;

	public SignalFactory() {
	}

	public SignalFactory(double[] parameters) {
		setParameters(parameters);
	}

	public static SignalFactory get(String name) {
		ArrayList<SignalFactory> list = getAll();
		for (SignalFactory factory : list) {
			if (factory.getName().equals(name))
				return factory;
		}
		return null;
	}

	public static ArrayList<String> getAllName() {
		ArrayList<String> list = new ArrayList<String>();
		for (SignalFactory factory : getAll()) {
			list.add(factory.getName());
		}
		return list;
	}

	public static ArrayList<SignalFactory> getAll() {
		ArrayList<SignalFactory> list = new ArrayList<SignalFactory>();
		list.add(new AirySimulated(1));	
		list.add(new Astigmatism(3, 0.2));	
		list.add(new Constant());
		list.add(new Cross(1, 1, 30));
		list.add(new Cube(10 ,1));
		list.add(new Defocus(3, 10, 10));
		list.add(new DoG(3, 4));
		list.add(new DoubleHelix(3, 10, 10));
		list.add(new Gaussian(3, 3, 3));
		list.add(new GridSpots(3, 1, 10));
		list.add(new Impulse());
		//list.add(new MotionBlur(3, 30, 3));
		list.add(new Ramp(1, 0, 0));
		list.add(new RandomLines(3));
		list.add(new Sinc(3, 3, 3));
		list.add(new Sphere(10, 1));
		list.add(new Torus(10));
		return list;
	}
	
	public static ArrayList<SignalFactory> getImages() {
		ArrayList<SignalFactory> list = new ArrayList<SignalFactory>();
		list.add(new Cube(10, 1));
		list.add(new Sphere(10, 1));
		list.add(new GridSpots(3, 1, 10));
		list.add(new Constant());
		list.add(new Cross(1, 1, 30));
		list.add(new DoG(3, 4));
		list.add(new Gaussian(3, 3, 3));
		list.add(new Impulse());
		list.add(new Ramp(1, 0, 0));
		list.add(new RandomLines(3));
		list.add(new Torus(10));
		return list;
	}

	public static ArrayList<SignalFactory> getPSF() {
		ArrayList<SignalFactory> list = new ArrayList<SignalFactory>();
		list.add(new AirySimulated(1));	
		list.add(new Astigmatism(3, 0.2));	
		list.add(new Cross(3, 1, 10));
		list.add(new Cube(10, 1));
		list.add(new Defocus(3, 10, 10));
		list.add(new DoG(3, 4));
		list.add(new DoubleHelix(3, 10, 10));
		list.add(new Gaussian(3, 3, 3));
		//list.add(new MotionBlur(3, 30, 3));
		list.add(new Impulse());
 		list.add(new Sinc(3, 3, 3));
		list.add(new Sphere(10, 1));
		list.add(new RandomLines(3));
		list.add(new Torus(10));
		return list;
	}

	public static SignalFactory getFactoryByName(String name) {
		ArrayList<SignalFactory> list = getAll();
		for (SignalFactory factory : list)
			if (name.toLowerCase().equals(factory.getName().toLowerCase())) {
				return factory;
			}
		return null;
	}
	
	public SignalFactory center(double fractXC, double fractYC, double fractZC) {
		this.fractXC = fractXC;
		this.fractYC = fractYC;
		this.fractZC = fractZC;
		return this;
	}

	public SignalFactory intensity(double background, double amplitude) {
		this.background = background;
		this.amplitude = amplitude;
		return this;
	}

	public String params() {
		String name[] = getParametersName();
		double params[] = getParameters();
		if (params.length == 1)
			return name[0] + "=" + params[0];
		else if (params.length == 2)
			return name[0] + "=" + params[0] + " " + name[1] + "=" + params[1];
		else
			return name[0] + "=" + params[0] + " " + name[1] + "=" + params[2] + " " + name[2] + "=" + params[2];
	}

	public RealSignal generate(int nx, int ny, int nz) {
		this.nx = nx;
		this.ny = ny;
		this.nz = nz;
		xc = fractXC * nx;
		yc = fractYC * ny;
		zc = fractZC * nz;
		RealSignal signal = new RealSignal(nx, ny, nz);
		fill(signal);

		return signal;
	}

	public abstract String getName();

	public abstract void setParameters(double[] parameters);

	public abstract double[] getParameters();

	public abstract String[] getParametersName();

	public abstract void fill(RealSignal signal);

	public class Worker extends SwingWorker<RealSignal, String> {
		private RealSignal signal;
		public boolean done=false;
		public Worker(RealSignal signal) {
			this.signal = signal;
			done = false;
		}
		
		protected RealSignal doInBackground() throws Exception {
			fill(signal);
			done = true;
			return signal;
		}

		protected void done() {
			done = true;
		}
	
	}
}
