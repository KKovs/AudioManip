
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import java.io.File;


public class GUIFrame extends JFrame {

	// --fields--	
	private static final long serialVersionUID = 1L;
	
	private JLabel targetDisplay;
	private JButton chooseButton;
	private JPanel northContainer;
	private JTextField fromField;
	private JTextField toField;
	private JPanel southContainer;

	private WAVFile currWAVFile;

	private FileFilter filterForWAV;	
	private ActionListener textFieldListener;
	
	// --constructor and methods--	
	public GUIFrame() {
		// initialization of fields
		targetDisplay = new JLabel();
		chooseButton = new JButton();
		northContainer = new JPanel();
		fromField = new JTextField();
		toField = new JTextField();
		southContainer = new JPanel();
		
		// filter for JFileChooser
		filterForWAV = new FileFilter() {

			@Override
			public String getDescription() {
				return "WAV file";
			}

			@Override
			public boolean accept(File f) {
				// accept only .wav (or directories, to allow navigation inside JFileChooser)
				if (f.getName().endsWith(".wav") || f.isDirectory()) {
					return true;
				}
				return false;
			}
		};
		
		// listener for when user confirms values in fields
		textFieldListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				currWAVFile.cutFromTo(Duration.HHMMSSToSec(fromField.getText()), Duration.HHMMSSToSec(toField.getText()));
				
				// executes after cutting is done..
				targetDisplay.setText(currWAVFile.getFileName() + " - cutting succesful");
				fromField.setText(null);
				toField.setText(null);
			}
		};
		
		// schedule Event Dispatcher Thread for activation after createGUI() is done
		SwingUtilities.invokeLater(() -> createGUI());
	}

	protected void createGUI() {
		setTitle("AudioManip");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		
		// start GUI at cursor's location
		Dimension framedim = new Dimension(400, 200);
		Point mouseloc = MouseInfo.getPointerInfo().getLocation();
		
		setLocation(mouseloc.x - (framedim.width / 2), mouseloc.y - (framedim.height / 2));
		setResizable(true);
		setPreferredSize(framedim);

		// layout setup
		northContainer.setLayout(new GridLayout(2, 1));
		northContainer.add(targetDisplay);
		targetDisplay.setHorizontalAlignment(JLabel.CENTER);
		northContainer.add(chooseButton, BorderLayout.EAST);
		add(northContainer);

		southContainer.setLayout(new GridLayout(1, 2));
		southContainer.add(fromField);
		southContainer.add(toField);
		add(southContainer, BorderLayout.SOUTH);

		setupChButton();

		pack();
		setVisible(true);
	}

	// setup choose button properties
	private void setupChButton() {
		chooseButton.setText("Choose WAV to cut");

		chooseButton.addActionListener(e -> {
			// file chooser setup
			JFileChooser jfc = new JFileChooser();
			jfc.setCurrentDirectory(new File("."));
			jfc.setFileFilter(filterForWAV);
			jfc.showOpenDialog(this);

			File selected = jfc.getSelectedFile();
			if (selected == null) {
				return;
			}

			currWAVFile = new WAVFile(selected);
			
			// print general info about file
			targetDisplay.setText(
					"<html>"+currWAVFile.getFileName() + "<br>" + Duration.secToHHMMSS(currWAVFile.getAudioLength())+"<br><br>press ENTER in either field to proceed</html>");
			setupTextFields();
		});
	}

	// setup text fields properties
	private void setupTextFields() {
		fromField.setText(Duration.ZERO_TIME);
		toField.setText(Duration.secToHHMMSS(currWAVFile.getAudioLength()));

		fromField.addActionListener(textFieldListener);
		toField.addActionListener(textFieldListener);
	}
}
