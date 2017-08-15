
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

@SuppressWarnings("serial")
public class GUI_Frame extends JFrame {

	private JLabel targetdisplay = new JLabel();
	private JButton choosebutton = new JButton();
	private JPanel north_cont = new JPanel();

	private JTextField from_field = new JTextField();
	private JTextField to_field = new JTextField();
	private JPanel south_cont = new JPanel();

	private WAVFile curr_wavfile;

	private FileFilter filterforWAV = new FileFilter() {

		@Override
		public String getDescription() {
			return "WAV file";
		}

		@Override
		public boolean accept(File f) {
			if (f.getName().endsWith(".wav") || f.isDirectory()) {
				return true;
			}
			return false;
		}
	};

	private ActionListener textFieldListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {

			curr_wavfile.cutFromTo(Duration.HHMMSS_to_sec(from_field.getText()), Duration.HHMMSS_to_sec(to_field.getText()));
			targetdisplay.setText(curr_wavfile.getFilename() + " - cutting succesful");
			from_field.setText(null);
			to_field.setText(null);
		}
	};

	public GUI_Frame() {
		SwingUtilities.invokeLater(() -> createGUI());
	}

	protected void createGUI() {
		setTitle("AudioManip");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		Dimension framedim = new Dimension(400, 200);
		Point mouseloc = MouseInfo.getPointerInfo().getLocation();

		setLocation(mouseloc.x - (framedim.width / 2), mouseloc.y - (framedim.height / 2));
		setResizable(true);
		setPreferredSize(framedim);

		// initial setup
		north_cont.setLayout(new GridLayout(2, 1));
		north_cont.add(targetdisplay);
		targetdisplay.setHorizontalAlignment(JLabel.CENTER);
		north_cont.add(choosebutton, BorderLayout.EAST);
		add(north_cont);

		south_cont.setLayout(new GridLayout(1, 2));
		south_cont.add(from_field);
		south_cont.add(to_field);
		add(south_cont, BorderLayout.SOUTH);

		setupChButton();

		pack();
		setVisible(true);
	}

	private void setupChButton() {
		choosebutton.setText("Choose WAV to cut");

		choosebutton.addActionListener(e -> {
			JFileChooser jfc = new JFileChooser();
			jfc.setCurrentDirectory(new File("."));
			jfc.setFileFilter(filterforWAV);
			jfc.showOpenDialog(this);

			File selected = jfc.getSelectedFile();
			if (selected == null) {
				return;
			}

			curr_wavfile = new WAVFile(selected);

			targetdisplay.setText(
					"<html>"+curr_wavfile.getFilename() + "<br>" + Duration.sec_to_HHMMSS(curr_wavfile.getAudioLength())+"<br><br>press ENTER in either field to proceed</html>");
			setupTextFields();
		});
	}

	private void setupTextFields() {
		from_field.setText(Duration.ZERO_TIME);
		to_field.setText(Duration.sec_to_HHMMSS(curr_wavfile.getAudioLength()));

		from_field.addActionListener(textFieldListener);
		to_field.addActionListener(textFieldListener);
	}
}
