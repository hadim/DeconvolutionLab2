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

import java.awt.BorderLayout;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BoxLayout;
import javax.swing.JToolBar;

public class SystemBar extends JToolBar {

	private Timer timer = new Timer();
	private TimerTask updater = new Updater();
			
	private SystemHeap 	heap;
	private SystemCPU   load;
	
	public SystemBar(int refreshTime) {
		super("System Bar");
		setFloatable(false);
		heap = new SystemHeap();
		load = new SystemCPU();
		restart(refreshTime);
		setLayout(new BorderLayout());
		setFloatable(true);
		setLayout(new BorderLayout());
		add(heap, BorderLayout.CENTER);
		add(load, BorderLayout.EAST);
	}
 
	public void update() {
		heap.repaint();
		load.repaint();
	}

	public void restart(int refreshTime) {
		if (updater != null) {	
			updater.cancel();
			updater = null;
		}
		updater = new Updater();
		timer.schedule(updater, 0, refreshTime);
	}

	public void close() {
		timer.cancel();
		timer = null;
	}

	private class Updater extends TimerTask {
		@Override
		public void run() {
			update();
		}
	}

}
