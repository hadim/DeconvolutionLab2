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

import ij.gui.GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.text.BadLocationException;

import lab.component.HTMLPane;
import lab.system.SystemBar;
import deconvolutionlab.Lab;
import deconvolutionlab.monitor.StatusMonitor;
import deconvolutionlab.monitor.TableMonitor;

public class DeconvolutionDialog extends JDialog implements ActionListener, Runnable, KeyListener, DeconvolutionListener {

	public enum State {NOTDEFINED, READY, RUN, FINISH};
	
	private JButton			bnStart	= new JButton("Run");
	private JButton			bnQuit	= new JButton("Quit");
	private JButton			bnRecap	= new JButton("Recap");
	private JButton			bnImage	= new JButton("Check Image");
	private JButton			bnPSF	= new JButton("Check PSF");
	private JButton			bnAlgo	= new JButton("Check Algo");
	private JButton			bnHelp	= new JButton("Help");
	private JTabbedPane		tab		= new JTabbedPane();

	private HTMLPane		pnCommand;
	private HTMLPane		pnResume;

	private JButton			job		= null;
	private Thread			thread	= null;

	private Deconvolution	deconvolution;
	private State			state = State.NOTDEFINED;

	public DeconvolutionDialog(Deconvolution deconvolution) {
		super(new JFrame(), deconvolution.getName() + " " + new SimpleDateFormat("dd/MM/yy HH:m:s").format(new Date()));

		this.deconvolution = deconvolution;
		JProgressBar status = new JProgressBar();
		deconvolution.getMonitors().add(new StatusMonitor(status));

		pnCommand = new HTMLPane("Monaco", 100, 100);
		pnCommand.append("p", deconvolution.getCommand());
		pnResume = new HTMLPane("Verdana", 600, 150);

		tab.add("Resume", pnResume.getPane());

		pnCommand.setEditable(true);
		pnCommand.addKeyListener(this);
		JPanel bn = new JPanel();
		bn.setLayout(new GridLayout(1, 5));
		bn.setBorder(BorderFactory.createEtchedBorder());
		bn.add(bnRecap);
		bn.add(bnImage);
		bn.add(bnPSF);
		bn.add(bnAlgo);
		bn.add(bnStart);

		status.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		status.setBorder(BorderFactory.createLoweredBevelBorder());
		JToolBar statusBar = new JToolBar();
		statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
		statusBar.setFloatable(false);
		statusBar.setLayout(new BorderLayout());
		statusBar.add(bnHelp, BorderLayout.WEST);
		statusBar.add(status, BorderLayout.CENTER);
		statusBar.add(bnQuit, BorderLayout.EAST);

		JPanel bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.PAGE_AXIS));
		bottom.add(bn);
		bottom.add(new SystemBar(200));
		bottom.add(statusBar);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(pnCommand.getPane(), BorderLayout.NORTH);
		panel.add(bottom, BorderLayout.SOUTH);
		panel.add(tab, BorderLayout.CENTER);

		add(panel);
		bnQuit.addActionListener(this);
		bnStart.addActionListener(this);
		bnPSF.addActionListener(this);
		bnImage.addActionListener(this);
		bnAlgo.addActionListener(this);
		bnRecap.addActionListener(this);
		bnHelp.addActionListener(this);
		deconvolution.addDeconvolutionListener(this);

		setMinimumSize(new Dimension(400, 200));
		pack();
		GUI.center(this);
		setVisible(true);

		print(deconvolution.recap());
		state = State.READY;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == bnStart) {
			if (state == State.FINISH) 
				print(deconvolution.getDeconvolutionReports());
			else if (thread == null) {
				job = bnStart;
				thread = new Thread(this);
				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
			}
			else {
				finish();
				if (deconvolution != null)
					deconvolution.abort();
			}
		}
		else if (e.getSource() == bnRecap) {
			if (thread == null) {
				job = bnRecap;
				thread = new Thread(this);
				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
			}
		}
		else if (e.getSource() == bnImage) {
			if (thread == null) {
				job = bnImage;
				thread = new Thread(this);
				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
			}
		}
		else if (e.getSource() == bnPSF) {
			if (thread == null) {
				job = bnPSF;
				thread = new Thread(this);
				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
			}
		}
		else if (e.getSource() == bnAlgo) {
			if (thread == null) {
				job = bnAlgo;
				thread = new Thread(this);
				thread.setPriority(Thread.MIN_PRIORITY);
				thread.start();
			}
		}

		else if (e.getSource() == bnQuit) {
			dispose();
		}
		else if (e.getSource() == bnHelp)
			Lab.help();
	}

	@Override
	public void run() {
		
		bnRecap.setEnabled(false);
		bnAlgo.setEnabled(false);
		bnPSF.setEnabled(false);
		bnImage.setEnabled(false);
		bnStart.setEnabled(false);
		String command = pnCommand.getText();
		deconvolution.setCommand(command);
		if (job == bnStart) {
			if (tab.getTabCount() > 1)
				tab.setSelectedIndex(1);
			else
				print(deconvolution.recap());
			deconvolution.run();
			print(deconvolution.getDeconvolutionReports());
		}
		else if (job == bnRecap) {
			tab.setSelectedIndex(0);
			print(deconvolution.recap());
		}
		else if (job == bnImage) {
			tab.setSelectedIndex(0);
			print(deconvolution.checkImage());
		}
		else if (job == bnPSF) {
			tab.setSelectedIndex(0);
			print(deconvolution.checkPSF());
		}
		else if (job == bnAlgo) {
			tab.setSelectedIndex(0);
			print(deconvolution.checkAlgo());
		}
		bnRecap.setEnabled(true);
		bnAlgo.setEnabled(true);
		bnPSF.setEnabled(true);
		bnImage.setEnabled(true);
		bnStart.setEnabled(true);
		thread = null;
	}

	private void print(ArrayList<String> lines) {
		pnResume.clear();
		for (String line : lines)
			pnResume.append("p", line);
	}

	public void addTableMonitor(String title, TableMonitor tm) {
		tab.add(title, tm.getPanel());
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		try {
			int len = pnCommand.getDocument().getLength();
			String command = pnCommand.getDocument().getText(0, len);
			deconvolution.setCommand(command);
			print(deconvolution.recap());
		}
		catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}

	@Override
    public void started() {
	    bnStart.setEnabled(true);
	    bnStart.setText("Abort");
	    state = State.RUN;
	    tab.setSelectedIndex(0);
    }

	@Override
    public void finish() {
	    bnStart.setEnabled(true);
	    bnStart.setText("Report");
	    state = State.FINISH;
	    tab.setSelectedIndex(0);
    }

}
