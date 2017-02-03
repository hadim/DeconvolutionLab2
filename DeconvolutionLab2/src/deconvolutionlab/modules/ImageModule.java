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
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import deconvolution.Command;
import deconvolution.Deconvolution;
import deconvolutionlab.Config;
import deconvolutionlab.Constants;
import deconvolutionlab.ImageSelector;
import deconvolutionlab.Lab;
import deconvolutionlab.PlatformImageSelector;
import deconvolutionlab.dialog.PatternDialog;
import deconvolutionlab.dialog.SyntheticDialog;
import deconvolutionlab.monitor.Monitors;
import lab.component.CustomizedColumn;
import lab.component.CustomizedTable;
import lab.tools.Files;
import signal.RealSignal;
import signal.factory.SignalFactory;

public class ImageModule extends AbstractModule implements ActionListener, MouseListener {

	private CustomizedTable	table;
	private JButton			bnFile;
	private JButton			bnDirectory;
	private JButton			bnSynthetic;
	private JButton			bnPlatform;

	public ImageModule(boolean expanded) {
		super("Image", "-image", "Active", "Show", expanded);
	}

	@Override
	public String getCommand() {
		int row = table.getSelectedRow();
		if (row < 0)
			return "";
		return "-image " + table.getCell(row, 1) + " " + table.getCell(row, 2);
	}

	@Override
	public JPanel buildExpandedPanel() {

		ArrayList<CustomizedColumn> columns = new ArrayList<CustomizedColumn>();
		columns.add(new CustomizedColumn("Name", String.class, 100, false));
		columns.add(new CustomizedColumn("Source", String.class, 100, false));
		columns.add(new CustomizedColumn("Command", String.class, Constants.widthGUI - 200, true));
		columns.add(new CustomizedColumn("", String.class, 30, "\u232B", "Delete this image source"));

		table = new CustomizedTable(columns, true);
		table.getColumnModel().getColumn(3).setMaxWidth(30);
		table.getColumnModel().getColumn(3).setMinWidth(30);
		table.addMouseListener(this);

		bnFile = new JButton("\u2295 file");
		bnDirectory = new JButton("\u2295 directory");
		bnSynthetic = new JButton("\u2295 synthetic");
		bnPlatform = new JButton("\u2295 platform");

		JToolBar pn = new JToolBar("Controls Image");
		pn.setBorder(BorderFactory.createEmptyBorder());
		pn.setLayout(new GridLayout(1, 5));
		pn.setFloatable(false);
		pn.add(bnFile);
		pn.add(bnDirectory);
		pn.add(bnSynthetic);
		pn.add(bnPlatform);

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEtchedBorder());
		panel.setLayout(new BorderLayout());
		panel.add(pn, BorderLayout.SOUTH);
		panel.add(table.getMinimumPane(100, 100), BorderLayout.CENTER);

		table.setDropTarget(new LocalDropTarget());
		getCollapsedPanel().setDropTarget(new LocalDropTarget());

		bnFile.addActionListener(this);
		bnDirectory.addActionListener(this);
		bnSynthetic.addActionListener(this);
		bnPlatform.addActionListener(this);
		getAction1Button().addActionListener(this);
		getAction2Button().addActionListener(this);

		Config.registerTable(getName(), "image", table);

		return panel;
	}

	public void update() {
		int row = table.getSelectedRow();
		if (row >= 0) {
			setCommand(getCommand());
			setSynopsis(table.getCell(row, 0));
			Command.command();
		}
		else {
			setSynopsis("");
			setCommand("Drag your image file, here");
		}
		getAction2Button().setEnabled(table.getRowCount() > 0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		if (e.getSource() == bnFile) {
			Deconvolution deconvolution = new Deconvolution(Command.command());
			file(deconvolution.getPath());
		}
		else if (e.getSource() == bnDirectory) {
			Deconvolution deconvolution = new Deconvolution(Command.command());
			dir(deconvolution.getPath());
		}
		else if (e.getSource() == bnSynthetic)
			synthetic(false);
		else if (e.getSource() == bnPlatform)
			platform();
		else if (e.getSource() == getAction1Button()) {
			int row = -1;
			for(int i=0; i<table.getRowCount(); i++) {
				if (table.getCell(i, 0).equalsIgnoreCase("active"))
				if (table.getCell(i, 1).equalsIgnoreCase("platform"))
				if (table.getCell(i, 2).equalsIgnoreCase("active"))
					row = i;
			}
			if (row < 0) 
				table.insert(new String[] { "active", "platform", "active", "" });
			else
				table.setRowSelectionInterval(row, row);
		}
		else if (e.getSource() == getAction2Button())
			display();
		update();
	}

	public void platform() {
		PlatformImageSelector selector = new ImageSelector(Lab.getPlatform()).getImageSelector();
		String name = selector.getSelectedImage();
		if (name != null)
			if (name != "")
				table.insert(new String[] { name, "platform", name, "" });
	}

	private void file(String path) {
		File file = Files.browseFile(path);
		if (file == null)
			return;
		table.insert(new String[] { file.getName(), "file", file.getAbsolutePath(), "" });
	}

	private void dir(String path) {
		File file = Files.browseDirectory(path);
		if (file == null)
			return;
		PatternDialog dlg = new PatternDialog(file);
		dlg.setVisible(true);
		if (dlg.wasCancel())
			return;
		table.insert(new String[] { dlg.getDirName(), "directory", dlg.getCommand(), "" });
	}

	private void synthetic(boolean edit) {
		ArrayList<SignalFactory> list = SignalFactory.getImages();
		SyntheticDialog dlg = new SyntheticDialog(list);
		if (edit) {
			int row = table.getSelectedRow();
			if (row >= 0) {
				dlg.setParameters(table.getCell(row, 0), table.getCell(row, 1));
			}
		}
		dlg.setVisible(true);
		if (dlg.wasCancel())
			return;
		if (edit) {
			int row = table.getSelectedRow();
			if (row <= 0)
				table.removeRow(row);
		}
		table.insert(new String[] { dlg.getShapeName(), "synthetic", dlg.getCommand(), "" });
	}

	private void edit() {
		int row = table.getSelectedRow();

		if (row < 0)
			return;
		String name = table.getCell(row, 0).trim();
		System.out.println("edit " + row + " " + name);
		for (SignalFactory factory : SignalFactory.getAll()) {
			if (name.equals(factory.getName().trim()))
				synthetic(true);
			return;
		}
		String filename = table.getCell(row, 1).trim();
		System.out.println("edit " + row + " " + filename + " " + new File(filename).exists() + " " + new File(filename).isDirectory());
		File file = new File(filename);
		if (!file.exists())
			return;
		if (file.isFile())
			file(table.getCell(row, 21));
		else
			dir(table.getCell(row, 1));
	}

	private void display() {
		int row = table.getSelectedRow();
		if (row < 0)
			return;
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				int row = table.getSelectedRow();
				RealSignal x = new Deconvolution(getCommand()).openImage();
				if (x != null)
					Lab.show(Monitors.createDefaultMonitor(), x, table.getCell(row, 0));
			}
		});
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == table) {
			int row = table.getSelectedRow();
			if (row < 0)
				return;
			if (table.getSelectedColumn() == 3) {
				table.removeRow(row);
				if (table.getRowCount() > 0)
					table.setRowSelectionInterval(0, 0);
			}
			update();
			if (e.getClickCount() == 2) {
				edit();
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void close() {
		bnFile.removeActionListener(this);
		bnDirectory.removeActionListener(this);
		bnSynthetic.removeActionListener(this);
		bnPlatform.removeActionListener(this);
	}

	public class LocalDropTarget extends DropTarget {

		@Override
		public void drop(DropTargetDropEvent e) {
			e.acceptDrop(DnDConstants.ACTION_COPY);
			e.getTransferable().getTransferDataFlavors();
			Transferable transferable = e.getTransferable();
			DataFlavor[] flavors = transferable.getTransferDataFlavors();
			for (DataFlavor flavor : flavors) {
				if (flavor.isFlavorJavaFileListType()) {
					try {
						List<File> files = (List<File>) transferable.getTransferData(flavor);
						for (File file : files) {
							if (file.isDirectory()) {
								table.insert(new String[] { file.getName(), "directory", file.getAbsolutePath(), "" });
								table.setRowSelectionInterval(0, 0);
								update();
							}
							if (file.isFile()) {
								table.insert(new String[] { file.getName(), "file", file.getAbsolutePath(), "" });
								update();
							}
						}
					}
					catch (UnsupportedFlavorException ex) {
						ex.printStackTrace();
					}
					catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
			e.dropComplete(true);
			super.drop(e);
		}
	}
}
