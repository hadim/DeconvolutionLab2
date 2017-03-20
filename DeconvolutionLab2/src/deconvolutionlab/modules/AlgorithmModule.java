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

package deconvolutionlab.modules;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bilib.component.HTMLPane;
import deconvolution.Command;
import deconvolution.Deconvolution;
import deconvolution.DeconvolutionDialog;
import deconvolution.algorithm.AbstractAlgorithmPanel;
import deconvolution.algorithm.Algorithm;
import deconvolutionlab.Config;
import deconvolutionlab.Lab;

public class AlgorithmModule extends AbstractModule implements ActionListener, ChangeListener {

	private JComboBox<String>	cmb;
	private HTMLPane			doc;
	private JPanel				cards;

	public AlgorithmModule(boolean expanded) {
		super("Algorithm", "-algorithm", "", "Check", expanded);
		ArrayList<AbstractAlgorithmPanel> deconv = Algorithm.getAvailableAlgorithms();
		for (AbstractAlgorithmPanel panel : deconv)
			cmb.addItem(panel.getName());
		cmb.addActionListener(this);
	}

	@Override
	public String getCommand() {
		String name = (String) cmb.getSelectedItem();
		AbstractAlgorithmPanel algo = Algorithm.getPanel(name);
		String cmd = "-algorithm " + algo.getShortnames()[0] + " " + algo.getCommand();
		String synopsis = algo.getShortnames()[0] + " " + algo.getCommand();
		setSynopsis(synopsis);
		setCommand(cmd);
		return cmd;
	}

	@Override
	public JPanel buildExpandedPanel() {
		cmb = new JComboBox<String>();
		JPanel pnc = new JPanel();
		pnc.add(cmb);
		doc = new HTMLPane(100, 1000);
		cards = new JPanel(new CardLayout());
		ArrayList<AbstractAlgorithmPanel> panels = Algorithm.getAvailableAlgorithms();
		
		for (AbstractAlgorithmPanel panel : panels) {
			JScrollPane scroll = new JScrollPane(panel.getPanelParameters());
			scroll.setBorder(BorderFactory.createEmptyBorder());
			scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			cards.add(panel.getName(), scroll);
		}
		cmb.setMaximumRowCount(panels.size());

		JPanel control = new JPanel();
		control.setLayout(new BoxLayout(control, BoxLayout.PAGE_AXIS));
		Border b1 = BorderFactory.createEtchedBorder();
		Border b2 = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		control.setBorder(BorderFactory.createCompoundBorder(b1, b2));
		cards.setBorder(BorderFactory.createEtchedBorder());
		
		control.add(cmb);
		control.add(cards);

		doc.append("h1", "Documentation");

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(control, BorderLayout.NORTH);
		panel.add(doc.getPane(), BorderLayout.CENTER);
		// cmb.addActionListener(this);
		getAction2Button().setToolTipText("Human readable of the command line");
		getAction2Button().addActionListener(this);
		Config.register(getName(), "algorithm", cmb, Algorithm.getDefaultAlgorithm());
		panel.setBorder(BorderFactory.createEtchedBorder());
		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		if (e.getSource() == cmb) {
			doc.clear();
			String name = (String) cmb.getSelectedItem();
			AbstractAlgorithmPanel algo = Algorithm.getPanel(name);
			doc.append(algo.getDocumentation());
			CardLayout cl = (CardLayout) (cards.getLayout());
			cl.show(cards, name);
		}
		if (e.getSource() == getAction2Button()) {
			Deconvolution deconvolution = new Deconvolution("Check Algorithm", Command.command());
			DeconvolutionDialog d = new DeconvolutionDialog(DeconvolutionDialog.Module.ALGO, deconvolution, null, null);
			Lab.setVisible(d, false);
		}
		setSynopsis((String) cmb.getSelectedItem());
		setCommand(getCommand());
		Command.command();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		setSynopsis((String) cmb.getSelectedItem());
		setCommand(getCommand());
		Command.command();
	}

	@Override
	public void close() {
	}
}
