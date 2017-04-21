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

import signal.RealSignal;

public class DoubleConeWave extends SignalFactory {

	private double aperture = 60;
	private double periodTopZ = 5;
	private double periodCenterZ = 15;
	private double attenuation = 10;

	public DoubleConeWave(double aperture, double periodTopZ, double periodCenterZ, double attenuation) {
		super(new double[] {aperture, periodTopZ, periodCenterZ, attenuation});
		setParameters(new double[] {aperture, periodTopZ, periodCenterZ, attenuation});
	}

	@Override
	public String getName() {
		return "DoubleConeWave";
	}

	@Override
	public String[] getParametersName() {
		return new String[] { "Aperture", "Period TopZ", "Period CenterZ", "Attenuation" };
	}

	@Override
	public void setParameters(double[] parameters) {
		if (parameters.length >= 1)
			this.aperture = parameters[0];
		if (parameters.length >= 2)
			this.periodTopZ = parameters[1];
		if (parameters.length >= 3)
			this.periodCenterZ = parameters[2];
		if (parameters.length >= 4)
			this.attenuation = parameters[3];
	}

	@Override
	public double[] getParameters() {
		return new double[]  {aperture, periodTopZ, periodCenterZ, attenuation};
	}

	@Override
	public void fill(RealSignal signal) {
		double apernorm = (2.0*aperture)/(nx+ny);
		double diag = Math.sqrt(nx*nx+ny*ny+nz*nz);
		double step = (periodCenterZ-periodTopZ)/nz; 
		for(int i=0; i<nx; i++)
		for(int j=0; j<ny; j++) {
			double r = Math.sqrt((i-xc)*(i-xc)+(j-yc)*(j-yc));
			for(int k=0; k<nz; k++) {
				double z = Math.abs(k-zc);
				double p = Math.sqrt(r*r + z*z)/diag;
				double period = Math.max(1, periodCenterZ-step*z);
				double sz = apernorm*z + period*0.25;
				double s = 1.0 / (1.0+Math.exp(-(r+sz))) - 1.0 / (1.0+Math.exp(-(r-sz)));
				double q = Math.cos(2.0*Math.PI*(r-apernorm*z)/period);
				double g = (attenuation*p+1);
				signal.data[k][i+j*nx] = (float)(amplitude * s * q * q / g);
			}
		}
	}
}
