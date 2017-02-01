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

public class GridSpots extends SignalFactory {

	private double	radius	= 3.0;
	private double	slope	= 1;
	private double	spacing	= 10.0;

	public GridSpots(double radius, double slope, double spacing) {
		super(new double[] {radius, spacing});
	}

	@Override
	public String getName() {
		return "GridSpots";
	}

	@Override
	public String[] getParametersName() {
		return new String[] { "Radius", "Sigmoid Curve Slope", "Spacing" };
	}

	@Override
	public void setParameters(double[] parameters) {
		if (parameters.length >= 1)
			this.radius = parameters[0];
		if (parameters.length >= 2)
			this.slope = parameters[1];
		if (parameters.length >= 3)
			this.spacing = parameters[2];
	}

	@Override
	public double[] getParameters() {
		return new double[] { radius, slope, spacing };
	}

	@Override
	public void fill(RealSignal signal) {
		float b = (float) background;
		for (int x = 0; x < nx; x++)
			for (int y = 0; y < ny; y++)
				for (int z = 0; z < nz; z++)
					signal.data[z][x + nx * y] = b;

		int nc = (int) ((nx - spacing) / spacing);

		int nr = (int) ((2 * (ny - spacing)) / spacing);
		double deltaY = spacing / nr;
		int np = (int) ((2 * (nz - spacing)) / spacing);
		double deltaZ = spacing / np;
		System.out.println(" " + spacing + " " + nr + " " + nc + " " + np + " " + deltaY + " " + deltaZ);
		double y = 0;
		double z = 0;
		for (int j = 0; j < nr; j++) {
			y += (spacing - j * deltaY);
			z = 0;
			for (int k = 0; k < np; k++) {
				z += (spacing - k * deltaZ);
				for (int i = 0; i < nc; i++) {
					double x = spacing + i * spacing;
					double A = amplitude- background; //i*(amplitude-background) / (nc+1);
					spot(signal, x, y, z, A);
				}
			}
		}
	}

	private void spot(RealSignal signal, double xc, double yc, double zc, double A) {
		int x1 = (int) Math.max(0, Math.round(xc - radius - 3 * slope));
		int x2 = (int) Math.min(nx - 1, Math.round(xc + radius + 3 * slope));
		int y1 = (int) Math.max(0, Math.round(yc - radius - 3 * slope));
		int y2 = (int) Math.min(ny - 1, Math.round(yc + radius + 3 * slope));
		int z1 = (int) Math.max(0, Math.round(zc - radius - 3 * slope));
		int z2 = (int) Math.min(nz - 1, Math.round(zc + radius + 3 * slope));
		for (int x = x1; x <= x2; x++)
			for (int y = y1; y <= y2; y++)
				for (int z = z1; z <= z2; z++) {
					double dr = Math.sqrt((x - xc) * (x - xc) + (y - yc) * (y - yc) + (z - zc) * (z - zc)) - radius;
					signal.data[z][x + nx * y] = Math.max(signal.data[z][x + nx * y], (float) (A - A / (1.0 + Math.exp(-dr / slope))));
				}
	}

}
