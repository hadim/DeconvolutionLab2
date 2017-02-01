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

package lab.system;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import lab.tools.NumFormat;

public class SystemHeap extends JLabel {
	
	private double peak;
	
	public SystemHeap() {
		super("");
		setBorder(BorderFactory.createEtchedBorder());
		setPreferredSize(new Dimension(200, 20));
		setMinimumSize(new Dimension(200, 20));
		setMaximumSize(new Dimension(200, 20));
	}
	
	public void reset() {
		peak = 0;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		MemoryUsage mem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		double used = mem.getUsed();
		double maxi = mem.getMax();
		peak = Math.max(used, peak);
	    super.paintComponent(g);
	    int w = getWidth();
	    g.setColor(new Color(10, 10, 10, 30));
	    for(int i=0; i<w; i+=w/10)
	    	g.drawLine(i, 0, i, 30);
	    
	    int posu = (int)Math.round(w*used/maxi);
   	    int posp = (int)Math.round(w*peak/maxi);
   	    int posm = w-40;
		String u = NumFormat.bytes(used); 
		String m = NumFormat.bytes(maxi);

   	    g.setColor(new Color(10, 10, 160, 30));
   	    g.fillRect(0, 0, posu, 30);
   	    g.fillRect(0, 0, posp, 30);
   	    g.setColor(new Color(160, 10, 10));
  	    g.drawString(u, posu, 13);
  	    //g.drawString(p, posp, 13);
   	    g.drawString(m, posm, 13);
	}
}