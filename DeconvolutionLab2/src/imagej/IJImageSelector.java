package imagej;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import deconvolutionlab.PlatformImageSelector;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GUI;

public class IJImageSelector extends PlatformImageSelector {

	@Override
	public boolean isSelectable() {
		return true;
	}

	@Override
	public String getSelectedImage() {
		Dialog dialog = new Dialog();
		dialog.setVisible(true);
		if (dialog.wasCancel())
			return "";
		return dialog.getName();
	}

	public class Dialog extends JDialog implements ActionListener, WindowListener {

		private JList<String>	list;
		private JButton			bnOK		= new JButton("OK");
		private JButton			bnCancel	= new JButton("Cancel");
		private boolean			cancel		= false;
		private String			name		= "";

		public Dialog() {
			super(new JFrame(), "Image Selection");
			int[] ids = WindowManager.getIDList();
			DefaultListModel listModel = new DefaultListModel();
			if (list != null)
				for (int id : ids) {
					ImagePlus idp = WindowManager.getImage(id);
					if (idp != null) {
						((DefaultListModel) listModel).addElement(idp.getTitle());
					}
				}

			list = new JList(listModel);
			list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			JScrollPane listScroller = new JScrollPane(list);
			listScroller.setPreferredSize(new Dimension(250, 80));
			JPanel bn = new JPanel(new GridLayout(1, 2));
			bn.add(bnCancel);
			bn.add(bnOK);
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(listScroller, BorderLayout.CENTER);
			panel.add(bn, BorderLayout.SOUTH);
			panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			bnOK.addActionListener(this);
			bnCancel.addActionListener(this);
			add(panel);
			pack();
			addWindowListener(this);
			GUI.center(this);
			setModal(true);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			bnOK.removeActionListener(this);
			bnCancel.removeActionListener(this);
			if (e.getSource() == bnCancel) {
				cancel = true;
				name = "";
				dispose();
				return;
			}
			else if (e.getSource() == bnOK) {
				cancel = false;
				name = (String) list.getSelectedValue();
				dispose();
			}
		}

		public String getName() {
			return name;
		}

		public boolean wasCancel() {
			return cancel;
		}

		@Override
		public void windowOpened(WindowEvent e) {
		}

		@Override
		public void windowClosing(WindowEvent e) {
			dispose();
			cancel = true;
			name = "";
			return;
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
	}

}
