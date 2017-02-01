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

package deconvolution.algorithm;

import java.util.concurrent.Callable;

import signal.ComplexSignal;
import signal.RealSignal;

public class TikhonovRegularizationInverseFilter extends AbstractAlgorithm implements Callable<RealSignal> {

	private double lambda = 0.1;
	
	public TikhonovRegularizationInverseFilter(double lambda) {
		super();
		this.lambda = lambda;
	}

	@Override
	public RealSignal call() {
		ComplexSignal Y = fft.transform(y);
		ComplexSignal H = fft.transform(h);
		ComplexSignal X = compute(Y, H);
		RealSignal x = fft.inverse(X);
		return x;
	}
	
	private  ComplexSignal compute(ComplexSignal Y, ComplexSignal H) {
		int nx = H.nx;
		int ny = H.ny;
		int nz = H.nz;
		int nxy = nx * ny*2;
		double ya, yb, ha, hb, fa, fb, mag, ta, tb;
		double epsilon2 = RealSignal.epsilon * RealSignal.epsilon;
		ComplexSignal result = new ComplexSignal(nx, ny, nz);
		for(int k=0; k<nz; k++)
		for(int i=0; i< nxy; i+=2) {
			ha = H.data[k][i];
			hb = H.data[k][i+1];
			ya = Y.data[k][i];
			yb = Y.data[k][i+1];
			fa = ha*ha - hb*hb + lambda;
			fb = 2f * ha * hb;
			mag = fa*fa + fb*fb;
			ta = (ha*fa + hb*fb) / (mag >= epsilon2 ? mag : epsilon2);
			tb = (hb*fa - ha*fb) / (mag >= epsilon2 ? mag : epsilon2);
			result.data[k][i] = (float)(ya*ta - yb*tb);
			result.data[k][i+1] = (float)(ya*tb + ta*yb);
		}
		return result;
	}
	
	@Override
	public String getName() {
		return "Tikhonov Regularization";
	}

	@Override
	public String getShortname() {
		return "TRIF";
	}

	@Override
	public boolean isRegularized() {
		return true;
	}

	@Override
	public boolean isStepControllable() {
		return false;
	}

	@Override
	public boolean isIterative() {
		return false;
	}
	
	@Override
	public boolean isWaveletsBased() {
		return false;
	}
	
	@Override
	public void setParameters(double[] params) {
		if (params == null)
			return;
		if (params.length > 0)
			lambda = (float)params[0];
	}
	
	@Override
	public double[] getDefaultParameters() {
		return new double[] {0.1};
	}
	
	@Override
	public double[] getParameters() {
		return new double[] {lambda};
	}
		
	@Override
	public double getRegularizationFactor() {
		return lambda;
	}
	
	@Override
	public double getStepFactor() {
		return 0.0;
	}
}
