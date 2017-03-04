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

package deconvolutionlab;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import deconvolution.Command;
import deconvolution.Deconvolution;
import deconvolution.DeconvolutionDialog;
import deconvolutionlab.dialog.BatchDialog;
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
import deconvolutionlab.modules.RunningModule;
import deconvolutionlab.system.SystemInfo;
import lab.component.JPanelImage;

public class LabPanel extends JPanel implements ActionListener, ChangeListener {

	private JTabbedPane			tab			= new JTabbedPane();
	private JButton				bnHelp		= new JButton("Help");
	private JButton				bnQuit		= new JButton("Quit");
	private JButton				bnSystem	= new JButton("System");
	private JButton				bnBatch		= new JButton("Batch");
	private JButton				bnRun		= new JButton("Run");
	private JButton				bnLaunch	= new JButton("Launch");
	private JButton				bnClose;

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
	private RunningModule		running;

	private ControllerModule	controller;

	private GroupedModulePanel	panelDeconv;
	private GroupedModulePanel	panelAdvanc;
	private GroupedModulePanel	panelScript;
	private GroupedModulePanel	panelAbout;
	private AbstractModule		modules[];
	
	public LabPanel(JButton bnClose) {
		this.bnClose = bnClose;
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
		running = new RunningModule(false);
		modules = new AbstractModule[] { image, psf, algo, output, controller, border, fourier, batch, running };
		Command.active(modules, command);
		Command.command();

		panelDeconv = new GroupedModulePanel(buildDeconvolutionPanel(), this);
		panelAdvanc = new GroupedModulePanel(buildAdvancedPanel(), this);
		panelScript = new GroupedModulePanel(buildProgrammingPanel(), this);
		panelAbout = new GroupedModulePanel(buildAboutPanel(), this);
		Border border = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		JPanelImage bottom = new JPanelImage("celegans.jpg");
		bottom.setBorder(border);

		bottom.setLayout(new GridLayout(1, 6));
		bottom.setBorder(border);

		bottom.add(bnHelp);
		bottom.add(bnSystem);
		bottom.add(bnClose);
		bottom.add(bnBatch);
		bottom.add(bnRun);
		bottom.add(bnLaunch);

		tab.add("Deconvolution", panelDeconv);
		tab.add("Advanced", panelAdvanc);
		tab.add("Scripting", panelScript);
		tab.add("About", panelAbout);
		tab.addChangeListener(this);

		setLayout(new BorderLayout());
		add(tab, BorderLayout.CENTER);
		add(bottom, BorderLayout.SOUTH);

		bnBatch.addActionListener(this);
		bnRun.addActionListener(this);
		bnLaunch.addActionListener(this);
		bnClose.addActionListener(this);
		bnQuit.addActionListener(this);
		bnHelp.addActionListener(this);
		bnSystem.addActionListener(this);

		((GroupedModulePanel) tab.getSelectedComponent()).organize();
		setMinimumSize(new Dimension(500, 500));

		Config.load();
		running.init();
		//sizeModule();
		Command.command();
		running.update();
		image.update();
		psf.update();
		output.update();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == bnHelp)
			Lab.help();
		else if (e.getSource() == bnClose)
			Config.store();
		else if (e.getSource() == bnSystem)
			SystemInfo.activate();
		else if (e.getSource() == bnBatch) {
			tab.setSelectedIndex(2);
			batch.expand();
			sizeModule();
			BatchDialog dlg = new BatchDialog(batch);
			Lab.setVisible(dlg, true);
		}
		
		else if (e.getSource() == bnLaunch) {
			String job = language.getJobName() + " " + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
			Deconvolution d = new Deconvolution(job, Command.command(), Deconvolution.Finish.ALIVE);
			d.launch();
		}
		else if (e.getSource() == bnRun) {
			String job = language.getJobName() + " " + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
			Deconvolution d = new Deconvolution(job, Command.command());
			d.deconvolve();
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
		list.add(running);
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

	public void close() {
		for (AbstractModule module : modules)
			module.close();
		bnLaunch.removeActionListener(this);
		bnRun.removeActionListener(this);
		bnBatch.removeActionListener(this);
		bnClose.removeActionListener(this);
		bnHelp.removeActionListener(this);
		
		Lab.close();
	}

	public void sizeModule() {
		if (tab.getSelectedIndex() == 0)
			sizePanel(panelDeconv);
		if (tab.getSelectedIndex() == 1)
			sizePanel(panelAdvanc);
		if (tab.getSelectedIndex() == 2)
			sizePanel(panelScript);
		if (tab.getSelectedIndex() == 3)
			sizePanel(panelAbout);
	}

	private void sizePanel(GroupedModulePanel panel) {
		Dimension dim = getSize();
		int hpc = 70;
		int npc = hpc * panel.getModules().size();
		Dimension small = new Dimension(dim.width, hpc);
		Dimension large = new Dimension(dim.width, dim.height - npc);
		setMinimumSize(new Dimension(Constants.widthGUI, 4*hpc));
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
