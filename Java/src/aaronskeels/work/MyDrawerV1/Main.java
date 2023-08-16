package aaronskeels.work.MyDrawerV1;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import aaronskeels.work.MyComputer.JnaFileChooser.JnaFileChooser;
import aaronskeels.work.MyComputerV3.ZoomableJPanel;

public class Main {
	public static final JFrame frame = new JFrame("Nail String Art Drawer");
	public static final JFrame loaderFrame = new JFrame("Loader");
	public static final BufferedImage OffscreenImage = new BufferedImage(4000, 4000, BufferedImage.TYPE_INT_RGB);
	public static JLabel nailNumText;
	public static JTextArea nailNumHistory;
	public static JPanel leftPanel;
	public static MainPanel mainPanel;
	private static Point2D.Float[] nailLocations = null;
	private static final int[] nailOrder = new int[50000];
	public static int curStringNumber = 0;
	public static final int NAIL_RADIUS = 5;
	public static File nailOrderChosenFile;
	
	public static void main(String[] args) {
		//Setup loader frame config
		loaderFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		loaderFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		JButton loadNailOrderButton = new JButton("Load Nail Order");
		loadNailOrderButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	JnaFileChooser fileChooser = new JnaFileChooser();
            	fileChooser.addFilter("Nail Order (.nailorder)", "nailorder");
                boolean result = fileChooser.showSaveDialog(loaderFrame);
                if (result == true) {
                    File selectedFile = fileChooser.getSelectedFile();
                    nailOrderChosenFile = selectedFile;
                    try {
                    	//Parse nail order
                    	List<String> sList = Files.readAllLines(selectedFile.toPath());
						String s = sList.get(0);
						s = s.substring(1, s.length()-1);
						String[] intStrings = s.split(",");
						int[] nailOrder = new int[intStrings.length];
						for (int i = 0;i < intStrings.length;i++) {
							nailOrder[i] = Integer.parseInt(intStrings[i]);
						}
						setNailOrder(nailOrder);
						frame.setVisible(true);
						loaderFrame.dispose();
						
						//Load progress from cache file
						File progress = new File(nailOrderChosenFile.getAbsolutePath() + "x");
						if (progress.exists()) {
							FileInputStream FIS = new FileInputStream(progress);
							Scanner scanner = new Scanner(FIS);
							int i = scanner.nextInt();
							curStringNumber = i;
							updateCurrentString(i);
							FIS.close();
							scanner.close();
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
                }
                System.out.println("Uploaded Nail Order!");
            }
        });
		loaderFrame.add(loadNailOrderButton);
		
		//Setup frame config
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH; // Components fill both horizontally and vertically
        constraints.weighty = 1;
        
        //Init frame keyboad listener
        frame.addKeyListener(new KeyListener() {
        	@Override
			public void keyTyped(KeyEvent e) {}
			@Override
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
					if (e.isAltDown()) {
						curStringNumber = Math.min(curStringNumber + 10, getCurNailOrderIndex());
						updateCurrentString(curStringNumber);
					} else if (e.isShiftDown()) {
						curStringNumber = Math.min(curStringNumber + 100, getCurNailOrderIndex());
						updateCurrentString(curStringNumber);
					} else if (e.isControlDown()) {
						curStringNumber = Math.min(curStringNumber + 1000, getCurNailOrderIndex());
						updateCurrentString(curStringNumber);
					} else {
						curStringNumber = Math.min(curStringNumber + 1, getCurNailOrderIndex());
						updateCurrentString(curStringNumber);
					}
				} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
					if (e.isAltDown()) {
						curStringNumber = Math.max(curStringNumber - 10, 0);
						updateCurrentString(curStringNumber);
					} else if (e.isShiftDown()) {
						curStringNumber = Math.max(curStringNumber - 100, 0);
						updateCurrentString(curStringNumber);
					} else if (e.isControlDown()) {
						curStringNumber = Math.max(curStringNumber - 1000, 0);
						updateCurrentString(curStringNumber);
					} else {
						curStringNumber = Math.max(curStringNumber - 1, 0);
						updateCurrentString(curStringNumber);
					}
				}
			}
		});
        
        //Initialize frame components
        leftPanel = new JPanel();
        nailNumText = new JLabel();
        leftPanel.add(nailNumText);
        nailNumHistory = new JTextArea();
        nailNumHistory.setEditable(false);
        nailNumHistory.setFocusable(false);
        nailNumHistory.setLineWrap(true);
        leftPanel.add(nailNumHistory);
        mainPanel = new MainPanel();

        //Initialize Vars
        resetNailOrder();
        
        //Add components to frame
        constraints.gridx = 0;
        constraints.weightx = .5;
        frame.add(leftPanel, constraints);
        constraints.gridx = 1;
        constraints.weightx = 2.0;
        frame.add(mainPanel, constraints);
        
        //Finally show frame
//        frame.setVisible(true);
        loaderFrame.setVisible(true);
	}
	
	public static void clearImage(BufferedImage targetImage) {
		Graphics2D g2d = targetImage.createGraphics();
		g2d.setColor(new Color(0, 0, 0, 0)); // Transparent black color
        g2d.fillRect(0, 0, targetImage.getWidth(), targetImage.getHeight());
        g2d.dispose();
	}
	public static void clearImage(BufferedImage targetImage, Color overrideColor) {
		Graphics2D g2d = targetImage.createGraphics();
		g2d.setColor(overrideColor); // Transparent black color
        g2d.fillRect(0, 0, targetImage.getWidth(), targetImage.getHeight());
        g2d.dispose();
	}
	
	public static int getCurNailOrderIndex() {
		for (int i = 0;i < getNailOrder().length;i++) {
			if (getNailOrder()[i] == -1)
				return i;
		}
		return -1;
	}
	
	public static Point2D.Float[] getNailLocations() {
		return nailLocations;
	}
	
	public static int[] getNailOrder() {
		return nailOrder;
	}
	
	public static void openMaximizedImage(BufferedImage targetImage) {
		JFrame frame = new JFrame("Image Preview");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        //Create JScroll
        JScrollPane pane = new JScrollPane();
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        //Create JPanel
        ZoomableJPanel panel = new ZoomableJPanel(targetImage);
        pane.setViewportView(panel);
        
        //Add MouseWheelListener for zooming
        pane.addMouseWheelListener(e -> {
            int notches = -e.getWheelRotation();
            boolean isControlDown = e.isControlDown();

            if (isControlDown) {
                double scaleFactor = Math.pow(1.05, notches);
                double newScale = panel.getScale() * scaleFactor;

                // Limit the scale to a reasonable range
                if (newScale > 0.1 && newScale < 10.0) {
                    panel.setScale(newScale);
                    Dimension scaledSize = new Dimension((int) (panel.getPreferredSize().width * panel.getScale()), (int) (panel.getPreferredSize().height * panel.getScale()));
                    panel.setPreferredSize(scaledSize);
                    pane.revalidate();
                }
            }
        });
        //Adjust scroll speed
        JScrollBar verticalScrollBar = pane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(20);
        JScrollBar horizontalScrollBar = pane.getHorizontalScrollBar();
        horizontalScrollBar.setUnitIncrement(20);
        
        frame.add(pane);
        
        frame.setVisible(true);
	}
	
	public static void parseNailLocations() {
		int nailAmount = 250; //I am lazy but this should be dynamic
		double dThetaDeg = 360d / nailAmount;
		double curThetaDeg = 0d;
		double DegToRadMultip = Math.PI/180d;
		int nailLocationsIndex = 0;
		Point2D.Float[] nailLocations = new Point2D.Float[nailAmount];
		for (int i = 0;i < nailAmount;i++,nailLocationsIndex++) {
			float x = (float) Math.cos(curThetaDeg*DegToRadMultip);
			float y = (float) Math.sin(curThetaDeg*DegToRadMultip);
			//x,y are in -1 -> 1 (unitcircle) space
			//convert to 0 -> 1 (normalized) space
			x = (x/2f) + .5f;
			y = (y/2f) + .5f;
			nailLocations[nailLocationsIndex] = new Point2D.Float(x, y);
			curThetaDeg += dThetaDeg;
		}
		Main.setNailLocations(nailLocations);
	}
	
	public static void resetNailOrder() {
		Arrays.fill(nailOrder, -1);
	}
	
	public static void setNailLocations(Point2D.Float[] value) {
		nailLocations = value;
		
		mainPanel.drawNails();
	}
	
	public static void setNailOrder(int[] value) {
		resetNailOrder();
		System.arraycopy(value, 0, nailOrder, 0, value.length);
		parseNailLocations();
		mainPanel.drawThroughStringNumber(0);
	}
	
	public static void updateCurrentString(int stringNum) {
		nailNumText.setText(stringNum + "/" + getCurNailOrderIndex());
		
		//Set history text
		int[] nailOrder = getNailOrder();
		String history = "";
		for (int i = stringNum;i >= Math.max(0, stringNum-20);i--) {
			history += nailOrder[i] + ",";
		}
		nailNumHistory.setText(history);
		
		//Draw
		mainPanel.drawThroughStringNumber(stringNum);
		
		//Update file
		File progress = new File(nailOrderChosenFile.getAbsolutePath() + "x");
		try {
			if (!progress.exists())
				progress.createNewFile();
			FileOutputStream FOS = new FileOutputStream(progress);
			FOS.write(Integer.toString(stringNum).getBytes());
			FOS.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
