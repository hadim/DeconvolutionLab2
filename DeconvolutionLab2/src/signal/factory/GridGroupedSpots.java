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

public class GridGroupedSpots extends SignalFactory {

	private double radius = 3.0;
	private double slope = 1;
	private double largest = 10;
	private double spacing = 10.0;
	
	public GridGroupedSpots(double side, double slope, double largest, double spacing) {
		super(new double[] {side, slope, largest, spacing});
	}

	@Override
	public String getName() {
		return "GridGroupedSpots";
	}
	 
	@Override
	public String[] getParametersName() {
		return new String[] {"Radius", "Sigmoid Curve Slope", "LargestDistance", "Spacing"};
	}	

	@Override
	public void setParameters(double[] parameters) {
		if (parameters.length >= 1)
			this.radius = parameters[0];
		if (parameters.length >= 2)
			this.slope = parameters[1];
		if (parameters.length >= 3)
			this.largest = parameters[2];
		if (parameters.length >= 4)
			this.spacing = parameters[3];
	}

	@Override
	public double[] getParameters() {
		return new double[] {radius, slope, largest, spacing};
	}
	
	@Override
	public void fill(RealSignal signal) {
		float b = (float)background;
		for(int x=0; x<nx; x++)
		for(int y=0; y<ny; y++)
		for(int z=0; z<nz; z++) 
			signal.data[z][x+nx*y] = b;
	
		int nc = (int)( (nx-spacing) / spacing);
		int nr = (int)( (ny-spacing) / spacing);
		int np = (int)( (nz-spacing) / spacing);
		
		for(int i=0; i<nc; i++)
		for(int j=0; j<nr; j++) 
		for(int k=0; k<np; k++) {
			double x = spacing + i*spacing;
			double y = spacing + j*spacing;
			double z = spacing + k*spacing;
			double dist = largest - j*largest / (nr-1);
			double distk = largest - k*largest / (np-1);
			double A = amplitude - i*(amplitude-background) / (nc+1);
			spot(signal, x, y, z, A);
			spot(signal, x+dist, y, z, A); 
			spot(signal, x, y+dist, z, A); 
			spot(signal, x+dist, y+dist, z, A); 

			spot(signal, x, y, z+distk, A);
			spot(signal, x+dist, y, z+distk, A); 
			spot(signal, x, y+dist, z+distk, A); 
			spot(signal, x+dist, y+dist, z+distk, A); 
		}
	}

	private void spot(RealSignal signal, double xc, double yc, double zc, double A) {
		int x1 = (int)Math.max(0, Math.round(xc-radius-3*slope));
		int x2 = (int)Math.min(nx-1, Math.round(xc+radius+3*slope));
		int y1 = (int)Math.max(0, Math.round(yc-radius-3*slope));
		int y2 = (int)Math.min(ny-1, Math.round(yc+radius+3*slope));
		int z1 = (int)Math.max(0, Math.round(zc-radius-3*slope));
		int z2 = (int)Math.min(nz-1, Math.round(zc+radius+3*slope));
		for(int x=x1; x<=x2; x++)
		for(int y=y1; y<=y2; y++)
		for(int z=z1; z<=z2; z++) {
			double dr = Math.sqrt((x-xc)*(x-xc) + (y-yc)*(y-yc) + (z-zc)*(z-zc)) - radius;
			signal.data[z][x+nx*y] = Math.max(signal.data[z][x+nx*y], (float)(A - A / (1.0 + Math.exp(-dr/slope))));
		}
	}


}
