package aaronskeels.work.MyComputerV3;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import aaronskeels.work.MyComputer.JnaFileChooser.JnaFileChooser;

@SuppressWarnings("serial")
public class InputPanel extends JPanel{
	public JTextField scoreWeight1 = null, scoreWeight2 = null, scoreWeight3 = null, scoreWeight4 = null, scoreWeight5 = null, scoreWeight6 = null, scoreWeight7 = null;
	
	public InputPanel() {
		setBackground(Color.red);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//		setMaximumSize(new Dimension(MainV3.frame.getWidth()/4, MainV3.frame.getHeight()));
		
		//Generate Nails Stuff
		JPanel rowOnePanel = new JPanel();
		rowOnePanel.setBackground(new Color(0, 0, 0, 0));
		rowOnePanel.setOpaque(false);
		JTextField textRowOne = new JTextField(3);
		textRowOne.setText("250");
		JButton buttonRowOne = new JButton("Set Nails");
		buttonRowOne.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (textRowOne.getText() != "") {
            		try {
            			Integer.parseInt(textRowOne.getText());
            		} catch (Exception err) {
            			return;
            		}
            		int nailAmount = Integer.parseInt(textRowOne.getText());
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
            		MainV3.setNailLocations(nailLocations);
            	}
            }
        });
		rowOnePanel.add(textRowOne);
		rowOnePanel.add(buttonRowOne);
		
		//Generate Resize Compute Stuff
		JPanel rowTwoPanel = new JPanel();
		rowTwoPanel.setBackground(new Color(0, 0, 0, 0));
		rowTwoPanel.setOpaque(false);
		JTextField textRowTwo = new JTextField(3);
		textRowTwo.setText("1000");
		JButton buttonRowTwo = new JButton("Update Computed Resolution");
		buttonRowTwo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (textRowTwo.getText() != "") {
            		try {
            			Integer.parseInt(textRowTwo.getText());
            		} catch (Exception err) {
            			return;
            		}
            		int computedSize = Integer.parseInt(textRowTwo.getText());
            		MainV3.ComputedImage = new BufferedImage(computedSize, computedSize, BufferedImage.TYPE_INT_RGB);
            		MainV3.TargetImage = new BufferedImage(computedSize, computedSize, BufferedImage.TYPE_INT_RGB);
            		MainV3.previewPanel.uploadPanel.repaint();
            	}
            }
        });
		rowTwoPanel.add(textRowTwo);
		rowTwoPanel.add(buttonRowTwo);
		
		//Generate Influence Map Stuff
		JPanel rowThreePanel = new JPanel();
		rowThreePanel.setBackground(new Color(0, 0, 0, 0));
		rowThreePanel.setOpaque(false);
		JTextField textRowThree = new JTextField(3);
		textRowThree.setText("12");
		JButton buttonRowThree = new JButton("Precompute Influence Map");
		buttonRowThree.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (textRowThree.getText() != "" && MainV3.getNailLocations() != null) {
            		try {
            			Integer.parseInt(textRowThree.getText());
            		} catch (Exception err) {
            			return;
            		}
            		int fullStrengthInfluenceWeight = Integer.parseInt(textRowThree.getText());
            		MainV3.computeInfluenceMap(fullStrengthInfluenceWeight);
            	}
            }
        });
		rowThreePanel.add(textRowThree);
		rowThreePanel.add(buttonRowThree);
		
		//Generate Debug String Influence Stuff
		JPanel rowFourPanel = new JPanel();
		rowFourPanel.setBackground(new Color(0, 0, 0, 0));
		rowFourPanel.setOpaque(false);
		JTextField textRowFour = new JTextField(3);
		textRowFour.setText("0,125");
		JButton buttonRowFour = new JButton("Debug String");
		buttonRowFour.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (textRowFour.getText() != "") {
            		MainV3.mainPanel.debugDrawString(textRowFour.getText());
            	}
            }
        });
		JButton buttonRowFour_2 = new JButton("Toggle Debug View");
		buttonRowFour_2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	MainV3.mainPanel.isShowingDebug = !MainV3.mainPanel.isShowingDebug;
            	MainV3.mainPanel.repaint();
            }
        });
		JButton buttonRowFour_3 = new JButton("Debug Influence Map");
		buttonRowFour_3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (textRowFour.getText().equals("") || !MainV3.influenceMap.keySet().contains(textRowFour.getText()))
            		return;
            	
            	BufferedImage image = new BufferedImage(MainV3.ComputedImage.getWidth(), MainV3.ComputedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            	Map<Integer, Byte> localInfluenceMapping = MainV3.influenceMap.get(textRowFour.getText());
            	int pixelIndex = 0;
            	int grayscale = 0;
            	for (int y = 0;y < image.getHeight();y++) {
            		for (int x = 0;x < image.getWidth();x++,pixelIndex++) {
            			grayscale = localInfluenceMapping.containsKey(pixelIndex) ? localInfluenceMapping.get(pixelIndex) : 0;
            			image.setRGB(x, y, new Color(grayscale, grayscale, grayscale).getRGB());
            		}
            	}
            	MainV3.openMaximizedImage(image);
            }
        });
		rowFourPanel.add(textRowFour);
		rowFourPanel.add(buttonRowFour);
		rowFourPanel.add(buttonRowFour_2);
		rowFourPanel.add(buttonRowFour_3);
		
		//Generate Compute Next String Stuff
		JPanel rowFivePanel = new JPanel() {
			@Override
		    public Dimension getPreferredSize() {
				return new Dimension(MainV3.frame.getWidth()/4, MainV3.frame.getHeight());
		    }
		};
		rowFivePanel.setBackground(new Color(0, 0, 0, 0));
		rowFivePanel.setOpaque(false);
		JTextField textRowFive = new JTextField(3);
		textRowFive.setText("1");
		JButton buttonRowFive = new JButton("Compute Next String(s)");
		buttonRowFive.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (textRowFive.getText() != "" && !MainV3.influenceMap.isEmpty()) {
            		try {
            			Integer.parseInt(textRowFive.getText());
            		} catch (Exception err) {
            			return;
            		}
            		int loops = Integer.parseInt(textRowFive.getText());
            		double fullStrengthInfluenceWeightMult = MainV3.fullStrengthInfluenceWeight/100d;
            		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                        @Override
                        protected Void doInBackground() throws Exception {
                        	double[] scoreWeights = MainV3.getScoreWeights();
                        	if (scoreWeights == null)
                        		return null;
                        	for (int i = 0;i < loops;i++) {
                    			MainV3.computeNextBestString(fullStrengthInfluenceWeightMult, scoreWeights);
                    			System.out.println((i+1) + "/" + loops);
                        	}
                        	System.out.println("Total Strings Placed: " + (MainV3.getCurNailOrderIndex()-1));
                            return null;
                        }
                    };
                    worker.execute();
            	}
            }
        });
		JButton buttonRowFive_2 = new JButton("Reset String Analysis");
		buttonRowFive_2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	MainV3.resetStringAnalysis();
            }
        });
		JButton buttonRowFive_3 = new JButton("Download Nail Order");
		buttonRowFive_3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	int[] nailOrder = MainV3.getNailOrder();
            	JnaFileChooser fileChooser = new JnaFileChooser();
            	fileChooser.addFilter("Nail Order (.nailorder)", "nailorder");
            	fileChooser.setCurrentDirectory(MainV3.previewPanel.uploadPanel.targetFile.getParent());
            	fileChooser.setDefaultFileName(MainV3.getExportFilePrefix() + ".nailorder");
                boolean result = fileChooser.showSaveDialog(MainV3.frame);
                if (result == true) {
                    File selectedFile = fileChooser.getSelectedFile();
                    if (!selectedFile.getName().endsWith(".nailorder"))
                    	selectedFile = new File(selectedFile.getAbsolutePath() + ".nailorder");
                    //Two try catches is gross but I don't care this error handling is dumb because I can't try-with-resource before I create the file. Java dumb.
                    try {
						selectedFile.createNewFile();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(selectedFile))){
                    	String s = "[";
                    	for (int i : nailOrder) {
                    		if (i == -1)
                    			break;
                    		s += i + ",";
                    	}
                    	s = s.substring(0, s.length()-1); //Remove last comma
                    	s += "]";
                    	bos.write(s.getBytes());
                    	bos.flush();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                System.out.println("Saved Nail Order File!");
            }
        });
		JButton buttonRowFive_4 = new JButton("Download PNG");
		buttonRowFive_4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	JnaFileChooser fileChooser = new JnaFileChooser();
            	fileChooser.addFilter("Images", "png", "tif", "tiff", "bmp", "jpg", "jpeg", "gif");
            	fileChooser.setCurrentDirectory(MainV3.previewPanel.uploadPanel.targetFile.getParent());
            	fileChooser.setDefaultFileName(MainV3.getExportFilePrefix() + ".png");
                boolean result = fileChooser.showSaveDialog(MainV3.frame);
                if (result == true) {
                    File selectedFile = fileChooser.getSelectedFile();
                    if (!selectedFile.getName().endsWith(".png"))
                    	selectedFile = new File(selectedFile.getAbsolutePath() + ".png");
                    try {
						ImageIO.write(MainV3.OffscreenImage, "png", selectedFile);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
                }
                System.out.println("Saved PNG File!");
            }
        });
		JButton buttonRowFive_5 = new JButton("Upload Nail Order");
		buttonRowFive_5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	JnaFileChooser fileChooser = new JnaFileChooser();
            	fileChooser.addFilter("Nail Order (.nailorder)", "nailorder");
                boolean result = fileChooser.showSaveDialog(MainV3.frame);
                if (result == true) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                    	List<String> sList = Files.readAllLines(selectedFile.toPath());
						String s = sList.get(0);
						s = s.substring(1, s.length()-1);
						String[] intStrings = s.split(",");
						int[] nailOrder = new int[intStrings.length];
						for (int i = 0;i < intStrings.length;i++) {
							nailOrder[i] = Integer.parseInt(intStrings[i]);
						}
						MainV3.setNailOrder(nailOrder);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
                }
                System.out.println("Uploaded Nail Order!");
            }
        });
		JButton buttonRowFive_6 = new JButton("Download Bundle");
		buttonRowFive_6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	JnaFileChooser fileChooser = new JnaFileChooser();
            	fileChooser.addFilter("", "fillersonothingshows");
            	fileChooser.setCurrentDirectory(MainV3.previewPanel.uploadPanel.targetFile.getParent());
            	fileChooser.setDefaultFileName(MainV3.getExportFilePrefix());
                boolean result = fileChooser.showSaveDialog(MainV3.frame);
                if (result == true) {
                    File bundleSelectedFile = fileChooser.getSelectedFile();
                    try {
                    	//Download ComputedImage png
                    	File computedSelectedFile = new File(bundleSelectedFile.getAbsolutePath() + "_c.png");
                    	ImageIO.write(MainV3.ComputedImage, "png", computedSelectedFile);
                    	//Download OffscreenImage png
                    	File offscreenSelectedFile = new File(bundleSelectedFile.getAbsolutePath() + "_o.png");
                    	ImageIO.write(MainV3.OffscreenImage, "png", offscreenSelectedFile);
                    	//Download NailOrder
                    	File nailSelectedFile = new File(bundleSelectedFile.getAbsolutePath() + "_n.nailorder");
                    	try {
                    		nailSelectedFile.createNewFile();
    					} catch (IOException e1) {
    						e1.printStackTrace();
    					}
                        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(nailSelectedFile))){
                        	String s = "[";
                        	for (int i : MainV3.getNailOrder()) {
                        		if (i == -1)
                        			break;
                        		s += i + ",";
                        	}
                        	s = s.substring(0, s.length()-1); //Remove last comma
                        	s += "]";
                        	bos.write(s.getBytes());
                        	bos.flush();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
					} catch (Exception e1) {
						e1.printStackTrace();
					}
                }
                System.out.println("Downloaded Bundle!");
            }
        });
		rowFivePanel.add(textRowFive);
		rowFivePanel.add(buttonRowFive);
		rowFivePanel.add(buttonRowFive_2);
		rowFivePanel.add(buttonRowFive_3);
		rowFivePanel.add(buttonRowFive_4);
		rowFivePanel.add(buttonRowFive_5);
		rowFivePanel.add(buttonRowFive_6);
		
		//Generate Debug Button which does whatever I need
		JPanel rowSixPanel = new JPanel();
		rowSixPanel.setBackground(new Color(0, 0, 0, 0));
		rowSixPanel.setOpaque(false);
		JButton debugButton = new JButton("Debug Whatever");
		debugButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	
            }
        });
		rowSixPanel.add(debugButton);
		
		//Generate Score Weight Inputs
		JPanel rowSevenPanel = new JPanel();
		rowSevenPanel.setBackground(new Color(0, 0, 0, 0));
		rowSevenPanel.setOpaque(false);
		scoreWeight1 = new JTextField(3);scoreWeight1.setText("0");
		scoreWeight2 = new JTextField(3);scoreWeight2.setText("0");
		scoreWeight3 = new JTextField(3);scoreWeight3.setText("0");
		scoreWeight4 = new JTextField(3);scoreWeight4.setText("0");
		scoreWeight5 = new JTextField(3);scoreWeight5.setText("0");
		scoreWeight6 = new JTextField(3);scoreWeight6.setText("1");
		scoreWeight7 = new JTextField(3);scoreWeight7.setText("-1");
		rowSevenPanel.add(scoreWeight1);
		rowSevenPanel.add(scoreWeight2);
		rowSevenPanel.add(scoreWeight3);
		rowSevenPanel.add(scoreWeight4);
		rowSevenPanel.add(scoreWeight5);
		rowSevenPanel.add(scoreWeight6);
		rowSevenPanel.add(scoreWeight7);
		
		add(rowOnePanel);
		add(rowTwoPanel);
		add(rowThreePanel);
		add(rowFourPanel);
		add(rowFivePanel);
		add(rowSixPanel);
		add(rowSevenPanel);
	}
	
}
