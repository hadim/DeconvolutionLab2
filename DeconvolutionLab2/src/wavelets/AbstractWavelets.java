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

package wavelets;

import signal.RealSignal;

public abstract class AbstractWavelets {

	protected int scales;
	
	public AbstractWavelets() {
		this.scales = 3;
	}
	
	public AbstractWavelets(int scales) {
		this.scales = scales;
	}
	
	
	public abstract void setScale(int scale);
	public abstract RealSignal analysis1(RealSignal in);
	public abstract RealSignal synthesis1(RealSignal in);
	public abstract String getName();
	public abstract String getDocumentation();
	
	public int getScales() {
		return scales;
	}
	
	public void shrinkage(float threshold, RealSignal in, RealSignal out, RealSignal buffer) {
		analysis(in, buffer);
		buffer.thresholdSoft(-threshold, threshold);
		synthesis(buffer, out);
	}

	public void analysis(RealSignal in, RealSignal out) {
		if (out == null)
			out = new RealSignal(in.nx, in.ny, in.nz);	
		int nxfine = in.nx;
		int nyfine = in.ny;
		int nzfine = in.nz;
		int nx = nxfine;
		int ny = nyfine;
		int nz = nzfine;
		for ( int i=0; i<scales; i++) {
			RealSignal sub = new RealSignal(nx, ny, nz, false);
			if (i==0)
				in.getSignal(sub);
			else
				out.getSignal(sub);
			sub = analysis1(sub);
			out.setSignal(sub);
			nx = Math.max(1, nx / 2);
			ny = Math.max(1, ny / 2);
			nz = Math.max(1, nz / 2);
		}
	}

	public void synthesis(RealSignal in, RealSignal out) {
		if (out == null)
			out = new RealSignal(in.nx, in.ny, in.nz);	
		int div = (int)Math.pow(2.0, (double)(scales-1));
		int nxcoarse = Math.max(1, in.nx / div);
		int nycoarse = Math.max(1, in.ny / div);
		int nzcoarse = Math.max(1, in.nz / div);
		int nx = nxcoarse;
		int ny = nycoarse;
		int nz = nzcoarse;
		out.copy(in);
		
		for ( int i=0; i<scales; i++) {
			RealSignal sub = new RealSignal(nx, ny, nz, false);
			out.getSignal(sub);
			sub = synthesis1(sub);
			out.setSignal(sub);
			nx = nx * 2;
			ny = ny * 2;
			if (nz > 1) 
				nz = nz * 2;
		}
	}

}
