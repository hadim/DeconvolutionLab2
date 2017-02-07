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

package deconvolutionlab.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import deconvolution.Command;
import deconvolution.Deconvolution;
import deconvolution.DeconvolutionDialog;
import deconvolutionlab.Config;
import deconvolutionlab.Constants;
import deconvolutionlab.Lab;
import deconvolutionlab.Platform;
import deconvolutionlab.modules.AboutModule;
import deconvolutionlab.modules.AbstractModule;
import deconvolutionlab.modules.AlgorithmModule;
import deconvolutionlab.modules.BatchModule;
import deconvolutionlab.modules.BorderModule;
import deconvolutionlab.modules.CommandModule;
import deconvolutionlab.modules.ConfigModule;
import deconvolutionlab.modules.ControllerModule;
import deconvolutionlab.modules.FFTModule;
import deconvolutionlab.modules.GroupedModulePanel;
import deconvolutionlab.modules.ImageModule;
import deconvolutionlab.modules.LanguageModule;
import deconvolutionlab.modules.LicenceModule;
import deconvolutionlab.modules.OutputModule;
import deconvolutionlab.modules.PSFModule;
import deconvolutionlab.modules.WatcherModule;
import lab.component.JPanelImage;
import lab.system.SystemPanel;

public class LabDialog extends JDialog implements ComponentListener, ActionListener, ChangeListener, WindowListener {

	private JTabbedPane			tab			= new JTabbedPane();
	private JButton				bnHelp		= new JButton("Help");
	private JButton				bnClose		= new JButton("Close");
	private JButton				bnQuit		= new JButton("Quit");
	private JButton				bnBatch		= new JButton("Batch");
	private JButton				bnRun		= new JButton("Run");
	private JButton				bnLaunch	= new JButton("Launch");
	private JButton				bnSystem	= new JButton("System");

	private ImageModule			image;
	private PSFModule			psf;
	private AlgorithmModule		algo;
	private AboutModule			about;
	private LicenceModule		licence;
	private OutputModule		output;
	private FFTModule			fourier;
	private BorderModule		border;
	private ConfigModule		config;
	private BatchModule			batch;
	private LanguageModule		language;
	private CommandModule		command;
	private WatcherModule		watcher;

	private ControllerModule	controller;

	private GroupedModulePanel	panelDeconv;
	private GroupedModulePanel	panelAdvanc;
	private GroupedModulePanel	panelScript;
	private GroupedModulePanel	panelAbout;
	private AbstractModule		modules[];

	public LabDialog() {
		super(new JFrame(), Constants.name);
		image = new ImageModule(false);
		psf = new PSFModule(false);
		algo = new AlgorithmModule(true);
		output = new OutputModule(true);
		fourier = new FFTModule(false);
		border = new BorderModule(false);
		controller = new ControllerModule(false);
		batch = new BatchModule(false);
		language = new LanguageModule(false);
		about = new AboutModule(true);
		licence = new LicenceModule(false);
		config = new ConfigModule(false);
		command = new CommandModule();
		watcher = new WatcherModule(false);

		doDialog();
		modules = new AbstractModule[] { image, psf, algo, output, controller, border, fourier, watcher, batch };
	
		Command.active(modules, command);
		Command.command();

		addWindowListener(this);
		addComponentListener(this);
		((GroupedModulePanel) tab.getSelectedComponent()).organize();
		setMinimumSize(new Dimension(500, 500));
		Config.registerFrame("DeconvolutionLab", "MainDialog", this);

		pack();
		setVisible(true);
		Config.load();
		sizeModule();
		Command.command();
		image.update();
		psf.update();
		output.update();
	}

	private void doDialog() {

		panelDeconv = new GroupedModulePanel(buildDeconvolutionPanel(), this);
		panelAdvanc = new GroupedModulePanel(buildAdvancedPanel(), this);
		panelScript = new GroupedModulePanel(buildProgrammingPanel(), this);
		panelAbout = new GroupedModulePanel(buildAboutPanel(), this);
		JPanelImage bottom = new JPanelImage("celegans.jpg");
		bottom.setLayout(new GridLayout(1, 7));
		Border b2 = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		bottom.setBorder(b2);

		bottom.add(bnHelp);
		bottom.add(bnSystem);
		bottom.add(bnQuit);
		bottom.add(bnClose);
		bottom.add(bnBatch);
		bottom.add(bnRun);
		bottom.add(bnLaunch);
		
		tab.add("Deconvolution", panelDeconv);
		tab.add("Advanced", panelAdvanc);
		tab.add("Scripting", panelScript);
		tab.add("About", panelAbout);
		tab.addChangeListener(this);

		JPanel base = new JPanel(new BorderLayout());
		base.add(bottom, BorderLayout.CENTER);
		//base.add(statusBar, BorderLayout.SOUTH);
		//PanelImage pi = new PanelImage("logo.png", 380, 75);
		//pi.setPreferredSize(new Dimension(400, 75));
		//add(pi, BorderLayout.NORTH);
		add(tab, BorderLayout.CENTER);
		add(base, BorderLayout.SOUTH);

		bnBatch.addActionListener(this);
		bnRun.addActionListener(this);
		bnLaunch.addActionListener(this);
		bnClose.addActionListener(this);
		bnQuit.addActionListener(this);
		bnHelp.addActionListener(this);
		bnSystem.addActionListener(this);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == bnHelp) {
			Lab.help();
		}
		else if (e.getSource() == bnSystem) {
			SystemPanel.show(400, 400);
		}
		else if (e.getSource() == bnClose) {
			Config.store();
			dispose();
		}
		else if (e.getSource() == bnQuit) {
			dispose();
		}
		else if (e.getSource() == bnBatch) {
			tab.setSelectedIndex(2);
			batch.expand();
			sizeModule();
			new BatchDialog(batch);
		}
		else if (e.getSource() == bnLaunch) {
			new Deconvolution(Command.command()).launch("", false);
		}
		else if (e.getSource() == bnRun) {
			new Deconvolution(Command.command()).deconvolve(false);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		((GroupedModulePanel) tab.getSelectedComponent()).organize();
		Command.command();
	}

	private ArrayList<AbstractModule> buildDeconvolutionPanel() {
		ArrayList<AbstractModule> list = new ArrayList<AbstractModule>();
		list.add(image);
		list.add(psf);
		list.add(algo);
		list.add(watcher);
		return list;
	}

	private ArrayList<AbstractModule> buildAdvancedPanel() {
		ArrayList<AbstractModule> list = new ArrayList<AbstractModule>();
		list.add(output);
		list.add(controller);
		list.add(border);
		list.add(fourier);
		return list;
	}

	private ArrayList<AbstractModule> buildProgrammingPanel() {
		ArrayList<AbstractModule> list = new ArrayList<AbstractModule>();
		list.add(batch);
		list.add(command);
		list.add(language);
		return list;
	}

	private ArrayList<AbstractModule> buildAboutPanel() {
		ArrayList<AbstractModule> list = new ArrayList<AbstractModule>();
		list.add(about);
		list.add(licence);
		list.add(config);
		return list;
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		Config.store();
		dispose();
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	private void close() {
		for (AbstractModule module : modules)
			module.close();
		bnLaunch.removeActionListener(this);
		bnRun.removeActionListener(this);
		bnBatch.removeActionListener(this);
		bnClose.removeActionListener(this);
		bnHelp.removeActionListener(this);
		removeWindowListener(this);
	}

	@Override
	public void dispose() {
		super.dispose();
		if (Lab.getPlatform() == Platform.STANDALONE) System.exit(0);
	}

	@Override
	public void componentResized(ComponentEvent e) {
		sizeModule();
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		Point p = this.getLocation();
		p.x += this.getWidth();
		DeconvolutionDialog.setLocationLaunch(p);
	}

	@Override
	public void componentShown(ComponentEvent e) {
		sizeModule();
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	public void sizeModule() {
		if (tab.getSelectedIndex() == 0) sizePanel(panelDeconv);
		if (tab.getSelectedIndex() == 1) sizePanel(panelAdvanc);
		if (tab.getSelectedIndex() == 2) sizePanel(panelScript);
		if (tab.getSelectedIndex() == 3) sizePanel(panelAbout);
	}

	private void sizePanel(GroupedModulePanel panel) {
		Dimension dim = getSize();
		int hpc = 60;
		int npc = hpc * panel.getModules().size();
		Dimension small = new Dimension(dim.width, hpc);
		Dimension large = new Dimension(dim.width, dim.height - npc);
		for (AbstractModule module : panel.getModules()) {
			if (module.isExpanded()) {
				module.setPreferredSize(large);
				module.setMaximumSize(large);
				module.setMinimumSize(small);
				module.getExpandedPanel().setPreferredSize(large);
				module.getExpandedPanel().setMaximumSize(large);
				module.getExpandedPanel().setMinimumSize(small);

			}
			else {
				module.setPreferredSize(small);
				module.setMaximumSize(small);
				module.setMinimumSize(small);
				module.getCollapsedPanel().setPreferredSize(small);
				module.getCollapsedPanel().setMaximumSize(small);
				module.getCollapsedPanel().setMinimumSize(small);
			}
		}
	}
}
