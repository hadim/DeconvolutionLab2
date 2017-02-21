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

package deconvolution;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import deconvolution.modules.AlgorithmDModule;
import deconvolution.modules.RecapDModule;
import deconvolution.modules.ImageDModule;
import deconvolution.modules.PSFDModule;
import deconvolution.modules.ReportDModule;
import deconvolutionlab.Config;
import deconvolutionlab.Lab;
import deconvolutionlab.TableStats;
import deconvolutionlab.monitor.TableMonitor;
import lab.component.BorderToggledButton;
import lab.component.CustomizedTable;
import lab.component.JPanelImage;

public class DeconvolutionDialog extends JDialog implements ActionListener, Runnable, DeconvolutionListener {

	public enum State {
		NOTDEFINED, READY, RUN, FINISH
	};

	private JButton				bnStart		= new JButton("Run");
	private JButton				bnStop		= new JButton("Stop");
	private JButton				bnReset		= new JButton("Reset");
	private JButton				bnHelp		= new JButton("Help");
	private JButton				bnQuit		= new JButton("Quit");

	private BorderToggledButton	bnRecap		= new BorderToggledButton("Recap");
	private BorderToggledButton	bnImage		= new BorderToggledButton("Image");
	private BorderToggledButton	bnPSF		= new BorderToggledButton("PSF");
	private BorderToggledButton	bnAlgo		= new BorderToggledButton("Algorithm");
	private BorderToggledButton	bnReport	= new BorderToggledButton("Report");
	private BorderToggledButton	bnMonitor	= new BorderToggledButton("Monitor");
	private BorderToggledButton	bnStats		= new BorderToggledButton("Stats");

	private String				job			= "";
	private Thread				thread		= null;

	private Deconvolution		deconvolution;
	private State				state		= State.NOTDEFINED;
	private JProgressBar		progress	= new JProgressBar();

	public static Point			location	= new Point(0, 0);

	private JPanel				cards		= new JPanel(new CardLayout());

	private boolean				flagMonitor	= false;
	private boolean				flagStats	= false;

	private ImageDModule			image;
	private	PSFDModule				psf;
	private RecapDModule			recap;
	private AlgorithmDModule		algorithm;
	private ReportDModule			report;
	
	public DeconvolutionDialog(Deconvolution deconvolution, TableMonitor tableMonitor, TableStats tableStats) {
		super(new JFrame(), deconvolution.getName());

		this.deconvolution = deconvolution;
		
		image = new ImageDModule(deconvolution);
		psf = new PSFDModule(deconvolution);
		recap = new RecapDModule(deconvolution);
		algorithm = new AlgorithmDModule(deconvolution);
		report = new ReportDModule(deconvolution);
		
		// Panel tool with all buttons
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		tool.setLayout(new GridLayout(1, 6));
		tool.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		tool.add(bnRecap);
		tool.add(bnImage);
		tool.add(bnPSF);
		tool.add(bnAlgo);
		tool.add(bnMonitor);
		tool.add(bnStats);
		tool.add(bnReport);

		// Panel buttons
		JPanelImage buttons = new JPanelImage("celegans.jpg");
		buttons.setLayout(new FlowLayout());
		buttons.setBorder(BorderFactory.createEtchedBorder());
		buttons.add(bnReset);
		buttons.add(bnStop);
		buttons.add(bnStart);

		// Panel status bar on bottom
		progress.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		progress.setBorder(BorderFactory.createLoweredBevelBorder());
		JToolBar statusBar = new JToolBar();
		statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
		statusBar.setFloatable(false);
		statusBar.setLayout(new BorderLayout());
		statusBar.add(bnHelp, BorderLayout.WEST);
		statusBar.add(progress, BorderLayout.CENTER);
		statusBar.add(bnQuit, BorderLayout.EAST);

		// Panel bottom
		JPanel bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.PAGE_AXIS));
		bottom.add(buttons);
		bottom.add(statusBar);

		// Panel Image
		cards.add(recap.getName(), recap.getPane());
		cards.add(image.getName(), image.getPane());
		cards.add(psf.getName(), psf.getPane());
		cards.add(algorithm.getName(), algorithm.getPane());
		cards.add(report.getName(), report.getPane());
		if (tableMonitor != null)
			cards.add("Monitor", tableMonitor.getPanel());
		if (tableStats != null)
			cards.add("Stats", tableStats.getPanel());

		// Panel Main
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(tool, BorderLayout.NORTH);
		panel.add(cards, BorderLayout.CENTER);
		panel.add(bottom, BorderLayout.SOUTH);

		add(panel);
		bnReset.addActionListener(this);
		bnQuit.addActionListener(this);
		bnStart.addActionListener(this);
		bnStop.addActionListener(this);
		bnPSF.addActionListener(this);
		bnImage.addActionListener(this);
		bnAlgo.addActionListener(this);
		bnRecap.addActionListener(this);
		bnHelp.addActionListener(this);
		bnMonitor.addActionListener(this);
		bnStats.addActionListener(this);
		deconvolution.addDeconvolutionListener(this);

		setMinimumSize(new Dimension(400, 200));
		pack();
		Config.registerFrame("DeconvolutionLab", "DeconvolutionDialog", this);
		
		
		Rectangle rect = Config.getDialog("DeconvolutionLab.DeconvolutionDialog");
		if (rect.x > 0 && rect.y > 0)
			setLocation(rect.x, rect.y);

		bnStop.setEnabled(false);

		toggle(bnRecap);
		state = State.READY;
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == bnStart) {
			if (flagMonitor)
				toggle(bnMonitor);
			else if (flagStats)
				toggle(bnStats);
			
			if (thread == null) {
				job = bnStart.getText();
				thread = new Thread(this);
				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
			}
		}
		else if (e.getSource() == bnStop) {
			toggle(bnReport);
			finish();
			if (deconvolution != null)
				deconvolution.abort();
		}
		else if (e.getSource() == bnRecap) {
			toggle(bnRecap);
			if (thread == null) {
				job = bnRecap.getText();
				thread = new Thread(this);
				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
			}
		}
		else if (e.getSource() == bnImage) {
			toggle(bnImage);
			if (thread == null) {
				job = bnImage.getText();
				thread = new Thread(this);
				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
			}
		}
		else if (e.getSource() == bnPSF) {
			toggle(bnPSF);
			if (thread == null) {
				job = bnPSF.getText();
				thread = new Thread(this);
				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
			}
		}
		else if (e.getSource() == bnAlgo) {
			toggle(bnAlgo);
			if (thread == null) {
				job = bnAlgo.getText();
				thread = new Thread(this);
				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
			}
		}
		else if (job.equals(bnRecap.getText())) {
			toggle(bnRecap);
			recap.update();
		}
		else if (e.getSource() == bnReport) {
			toggle(bnReport);
			report.update();
		}
		else if (e.getSource() == bnReset) {
			toggle(bnRecap);
			state = State.READY;
			bnStart.setEnabled(true);
		}
		else if (e.getSource() == bnQuit) {
			deconvolution.finalize();
			deconvolution = null;
			dispose();
		}
		else if (e.getSource() == bnHelp)
			Lab.help();
		else if (e.getSource() == bnMonitor)
			toggle(bnMonitor);
		else if (e.getSource() == bnStats)
			toggle(bnStats);
	}

	@Override
	public void run() {
		bnRecap.setEnabled(false);
		bnImage.setEnabled(false);
		bnPSF.setEnabled(false);
		bnAlgo.setEnabled(false);

		deconvolution.setCommand(recap.getCommand());
		if (job.equals(bnStart.getText())) {
			bnStart.setEnabled(false);
			bnStop.setEnabled(true);
			deconvolution.run();
			toggle(bnReport);
			bnStop.setEnabled(false);
		}
		else if (job.equals(bnImage.getText()))
			image.update();
		else if (job.equals(bnPSF.getText())) 
			psf.update();
		else if (job.equals(bnAlgo.getText())) 
			algorithm.update();
		
		bnRecap.setEnabled(true);
		bnAlgo.setEnabled(true);
		bnPSF.setEnabled(true);
		bnImage.setEnabled(true);
		thread = null;
	}
	
	@Override
	public void started() {
		state = State.RUN;
	}

	@Override
	public void finish() {
		state = State.FINISH;
	}

	public JProgressBar getProgressBar() {
		return progress;
	}

	public static void setLocationLaunch(Point l) {
		location = l;
	}

	private void toggle(BorderToggledButton bn) {
		((CardLayout) (cards.getLayout())).show(cards, bn.getText());
		bnRecap.setSelected(false);
		bnImage.setSelected(false);
		bnPSF.setSelected(false);
		bnAlgo.setSelected(false);
		bnMonitor.setSelected(false);
		bnStats.setSelected(false);
		bnReport.setSelected(false);
		bn.setSelected(true);

	}

}
