package xfssdl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Created on 21/03/2015 - 10:57
 * 
 * @author Franck
 *
 */
public class BiteFrame extends JFrame implements ActionListener, Observer {
	private static final long serialVersionUID = 1L;

	private JTextArea logArea;
	private JTextField usernameInput;
	private JTextField fileInput;
	private JCheckBox videos;
	private JCheckBox screens;
	private Downloader hook;
	private JScrollPane scroll;
	
	public BiteFrame(Downloader downloader){
		super("Xfire Screenshot Downloader");
		setSize(600, 450);
		setLayout(new BorderLayout());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		JPanel pLog = new JPanel(new GridLayout(1, 1)),
				pUser = new JPanel(new GridLayout(1, 1)),
				pFile = new JPanel(new GridLayout(1, 1)),
				pButtons = new JPanel(new GridLayout(1, 4)),
				pControls = new JPanel(new GridLayout(3, 1));
		
		logArea = new JTextArea();
		usernameInput = new JTextField();
		fileInput = new JTextField();
		
		JButton folder = new JButton("Select folder"),
				start = new JButton("Start");
		folder.addActionListener(this);
		start.addActionListener(this);
		
		videos = new JCheckBox("Videos");
		screens = new JCheckBox("Screens");
		scroll = new JScrollPane(logArea);
		
		pLog.setPreferredSize(new Dimension(600, 300));
		pUser.setPreferredSize(new Dimension(600, 50));
		pFile.setPreferredSize(new Dimension(600, 50));
		pButtons.setPreferredSize(new Dimension(600, 50));
		pControls.setPreferredSize(new Dimension(600, 150));
		
		pLog.add(scroll);
		pUser.add(usernameInput);
		pFile.add(fileInput);
		pButtons.add(folder);
		pButtons.add(screens);
		pButtons.add(videos);
		pButtons.add(start);
		
		pControls.add(pUser);
		pControls.add(pFile);
		pControls.add(pButtons);
		
		add(pLog, BorderLayout.SOUTH);
		add(pControls);
		
		hook = downloader;
		
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Select folder")){
			JFileChooser jfc = new JFileChooser();
			jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			jfc.setAcceptAllFileFilterUsed(false);
			if(jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
				fileInput.setText(jfc.getSelectedFile().getAbsolutePath());
			}
			return;
		}
		if(e.getActionCommand().equals("Start")){
			if(hook.setParameters(usernameInput.getText(), fileInput.getText(), screens.isSelected(), videos.isSelected())){
				new Thread(new Runnable(){
					public void run(){
						try {
							hook.startDownload();
						} catch (IOException | InterruptedException e) {
							e.printStackTrace();
						}
					}
				}).start();
			} else {
				JOptionPane.showMessageDialog(this, "Parameters are incorrect");
			}
			return;
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		logArea.append(arg.toString());
		JScrollBar sb = scroll.getVerticalScrollBar();
		sb.setValue(sb.getMaximum());
	}
}
