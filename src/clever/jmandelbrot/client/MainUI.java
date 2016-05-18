package clever.jmandelbrot.client;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

/**
 * 主窗口类
 * 
 * @author Sheldon
 *
 */
@SuppressWarnings("serial")
public class MainUI extends JFrame {

	private JPanel contentPanel;
	private JLabel mStatusbarLable;
	private JPanel mCanvasPanel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainUI frame = new MainUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmSavaAsImage = new JMenuItem("Sava as Image...");
		mnFile.add(mntmSavaAsImage);

		mnFile.addSeparator();

		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		mnFile.add(mntmQuit);

		JMenu mnSetting = new JMenu("Setting");
		menuBar.add(mnSetting);

		JMenuItem mntmDrawingMode = new JMenuItem("Drawing Mode");
		mntmDrawingMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// 设置主容器面板为绘图面板对象
				if (mCanvasPanel != null)
					contentPanel.remove(mCanvasPanel);
				mCanvasPanel = new MandelbrotPanel(MainUI.this);
				contentPanel.add(mCanvasPanel, BorderLayout.CENTER);
				setVisible(true);
			}
		});
		mnSetting.add(mntmDrawingMode);

		JMenuItem mntmTestMode = new JMenuItem("Test Mode");
		mnSetting.add(mntmTestMode);

		mnSetting.addSeparator();

		JMenuItem mntmSetting = new JMenuItem("Setting");
		mnSetting.add(mntmSetting);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmHelp = new JMenuItem("Help...");
		mnHelp.add(mntmHelp);

		mnHelp.addSeparator();

		JMenuItem mntmAbout = new JMenuItem("About...");
		mnHelp.add(mntmAbout);

		contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		contentPanel.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPanel);

		JToolBar statusToolBar = new JToolBar();
		statusToolBar.setFloatable(false);
		contentPanel.add(statusToolBar, BorderLayout.SOUTH);

		mStatusbarLable = new JLabel("Welcome to JMandelbrot.");
		statusToolBar.add(mStatusbarLable);
	}

	public void SendStatusMsg(String msg) {
		mStatusbarLable.setText(msg);
	}

}
