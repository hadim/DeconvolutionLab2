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
import signal.Operations;
import signal.RealSignal;

public class RichardsonLucy extends AbstractAlgorithm implements Callable<RealSignal> {

	public RichardsonLucy(int iter) {
		super();
		controller.setIterationMax(iter);
	}
		
	// x(k+1) = x(k) *. Hconj * ( y /. H x(k))
	@Override
	public RealSignal call() {
		ComplexSignal H = fft.transform(h);
		ComplexSignal U = new ComplexSignal(y.nx, y.ny, y.nz);
		RealSignal x = y.duplicate();
		RealSignal p = y.duplicate();
		RealSignal u = y.duplicate();
		while(!controller.ends(x)) {		
			fft.transform(x, U);
			U.times(H);
			fft.inverse(U, u);
			Operations.divide(y, u, p);
			fft.transform(p, U);
			U.timesConjugate(H);
			fft.inverse(U, u);
			x.times(u); 
		}
		return x;
	}
	
	@Override
	public String getName() {
		return "Richardson-Lucy";
	}
	
	@Override
	public String getShortname() {
		return "RL";
	}

	@Override
	public boolean isRegularized() {
		return false;
	}

	@Override
	public boolean isStepControllable() {
		return false;
	}
	
	@Override
	public boolean isIterative() {
		return true;
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
			controller.setIterationMax((int)Math.round(params[0]));
	}
	
	@Override
	public double[] getDefaultParameters() {
		return new double[] {10};
	}
	
	@Override
	public double[] getParameters() {
		return new double[] {controller.getIterationMax()};
	}
	
	@Override
	public double getRegularizationFactor() {
		return 0.0;
	}
	
	@Override
	public double getStepFactor() {
		return 0;
	}

}
