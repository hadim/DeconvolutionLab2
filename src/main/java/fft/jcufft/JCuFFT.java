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

package fft.jcufft;

import java.util.HashMap;
import java.util.Map;

import deconvolutionlab.monitor.Monitors;
import fft.AbstractFFT;
import fft.Separability;
import jcuda.jcufft.JCufft;
import jcuda.jcufft.cufftHandle;
import signal.ComplexSignal;
import signal.RealSignal;

public class JCuFFT extends AbstractFFT {

	private static final Map<Integer, String> CUFFT_ERRORS = new HashMap<Integer, String>();
	static {
		CUFFT_ERRORS.put(0, "CUFFT_SUCCESS");
		CUFFT_ERRORS.put(1, "CUFFT_INVALID_PLAN");
		CUFFT_ERRORS.put(2, "CUFFT_ALLOC_FAILED");
		CUFFT_ERRORS.put(3, "CUFFT_INVALID_TYPE");
		CUFFT_ERRORS.put(4, "CUFFT_INVALID_VALUE");
		CUFFT_ERRORS.put(5, "CUFFT_INTERNAL_ERROR");
		CUFFT_ERRORS.put(6, "CUFFT_EXEC_FAILED");
		CUFFT_ERRORS.put(7, "CUFFT_SETUP_FAILED");
		CUFFT_ERRORS.put(8, "CUFFT_INVALID_SIZE");
		CUFFT_ERRORS.put(9, "CUFFT_UNALIGNED_DATA");
	}

	private boolean is3D;

	public JCuFFT() {
		super(Separability.XYZ);
	}

	@Override
	public void init(Monitors monitors, int nx, int ny, int nz) {
		super.init(monitors, nx, ny, nz);
		try {
			if (nz > 1)
				is3D = true;
			else
				is3D = false;
		} catch (Exception ex) {
			System.out.println("check " + ex + ". " + nx + " " + ny + " " + nz);
		}
	}

	@Override
	public void transformInternal(RealSignal x, ComplexSignal X) {
		float[] interleave = x.getInterleaveXYZAtReal();
		cufftHandle plan = new cufftHandle();
		int result;

		if (is3D) {
			result = JCufft.cufftPlan3d(plan, nz, ny, nx, jcuda.jcufft.cufftType.CUFFT_C2C);
		} else {
			result = JCufft.cufftPlan2d(plan, ny, nx, jcuda.jcufft.cufftType.CUFFT_C2C);
		}
		handleResult(result);

		result = JCufft.cufftExecC2C(plan, interleave, interleave, JCufft.CUFFT_FORWARD);
		handleResult(result);

		result = JCufft.cufftDestroy(plan);
		handleResult(result);

		X.setInterleaveXYZ(interleave);
	}

	@Override
	public void inverseInternal(ComplexSignal X, RealSignal x) {
		float[] interleave = X.getInterleaveXYZ();
		cufftHandle plan = new cufftHandle();
		int result;

		if (is3D) {
			result = JCufft.cufftPlan3d(plan, nz, ny, nx, jcuda.jcufft.cufftType.CUFFT_C2C);
		} else {
			result = JCufft.cufftPlan2d(plan, ny, nx, jcuda.jcufft.cufftType.CUFFT_C2C);
		}
		handleResult(result);

		result = JCufft.cufftExecC2C(plan, interleave, interleave, JCufft.CUFFT_INVERSE);
		handleResult(result);

		result = JCufft.cufftDestroy(plan);
		handleResult(result);

		x.setInterleaveXYZAtReal(interleave);
	}

	@Override
	public String getName() {
		return "JCuFFT";
	}

	@Override
	public boolean isMultithreadable() {
		return true;
	}

	private void handleResult(int result) {
		if (result != 0) {
			try {
				throw new CuFFTException(CUFFT_ERRORS.get(result));
			} catch (CuFFTException e) {
				e.printStackTrace();
			}
		}
	}

}