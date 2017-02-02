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
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import deconvolutionlab.Constants;

public class SystemCPU extends JLabel {
	
	private double peak;
	
	public SystemCPU() {
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
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		double l = 20;
		for (Method method : os.getClass().getDeclaredMethods()) {
			method.setAccessible(true);
			if (method.getName().startsWith("get") && Modifier.isPublic(method.getModifiers())) {
				try {
					if (method.getName().contains("ProcessCpuLoad") )
						
						l = Double.parseDouble(method.invoke(os).toString());
				}
				catch (Exception e) {}
			}
		}

	
		double used = l;
		double maxi = 100;
		peak = Math.max(used, peak);
	    super.paintComponent(g);
	    int w = getWidth();
	    g.setColor(new Color(10, 10, 10, 30));
	    for(int i=0; i<w; i+=w/10)
	    	g.drawLine(i, 0, i, 30);
	    
	    int posu = (int)Math.round(w*used/maxi);
   	    int posp = (int)Math.round(w*peak/maxi);
		String u = String.format("%3.3f", used); 

   	    g.setColor(new Color(10, 10, 160, 30));
   	    g.fillRect(0, 0, posu, 30);
   	    g.fillRect(0, 0, posp, 30);
   	    g.setColor(new Color(160, 10, 10));
  	    g.drawString(u + "%", 10, 13);
 	    g.drawString("100%", w-50, 13);
	}
}