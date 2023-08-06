package aaronskeels.work.MyComputerV3;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;

import aaronskeels.work.MyComputer.JnaFileChooser.JnaFileChooser;

@SuppressWarnings("serial")
public class TargetPanel extends JPanel{
	public JPanel visualPanel = null;
	public File targetFile = null;
	
	public TargetPanel() {
		setBackground(Color.magenta);
		setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH; // Components fill both horizontally and vertically
        constraints.weightx = 1;
		
		//Generate Input Stuff
		JButton button = new JButton("Load Source Image");
		button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	JnaFileChooser fc = new JnaFileChooser();
            	fc.addFilter("Images", "png", "tif", "tiff", "bmp", "jpg", "jpeg", "gif");
            	if (fc.showOpenDialog((Window) MainV3.frame)) {
            		File f = fc.getSelectedFile();
            		updateImage(f);
            	}
            }
        });
		JButton button2 = new JButton("Invert");
		button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	invertImage();
            }
        });
		
		//Generate Visual
		visualPanel = new JPanel() {
			@Override
		    protected void paintComponent(Graphics g) {
		        super.paintComponent(g);
		        Graphics2D g2d = (Graphics2D) g;
		        
		        int width = getWidth();
		        int height = getHeight();
		        
		        double scale = Math.min((double) width / MainV3.TargetImage.getWidth(), (double) height / MainV3.TargetImage.getHeight());
		        int scaledCanvasWidth = (int) (MainV3.TargetImage.getWidth() * scale);
		        int scaledCanvasHeight = (int) (MainV3.TargetImage.getHeight() * scale);
		        g2d.drawImage(MainV3.TargetImage, (int) ((width - scaledCanvasWidth) / 2d), (int) ((height - scaledCanvasHeight) / 2d), scaledCanvasWidth, scaledCanvasHeight, null);
		        g2d.dispose();
		    }
		};
		visualPanel.setBackground(Color.orange);
		visualPanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {
				MainV3.openMaximizedImage(MainV3.TargetImage);
			}
		});
		
		//Add components
		constraints.gridy = 0;
		constraints.weighty = 1;
		add(button, constraints);
		constraints.gridy = 1;
		constraints.weighty = 1;
		add(button2, constraints);
		constraints.gridy = 2;
		constraints.weighty = 10;
		add(visualPanel, constraints);
	}
	
	public void updateImage(File f) {
		targetFile = f;
		
		BufferedImage bufferedImage = null;
		try {
			bufferedImage = ImageIO.read(f);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
        Graphics2D g2d = MainV3.TargetImage.createGraphics();
        g2d.drawImage(bufferedImage, 0, 0, MainV3.TargetImage.getWidth(), MainV3.TargetImage.getHeight(), null);
        g2d.dispose();
        visualPanel.repaint();
	}
	
	public void invertImage() {
		BufferedImage img = MainV3.TargetImage;
		int[] rgbArray = MainV3.TargetImage.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
		for (int i = 0;i < rgbArray.length;i++) {
			Color tempColor = new Color(rgbArray[i]);
			int grayscale = 255 - tempColor.getRed();
			rgbArray[i] = new Color(grayscale, grayscale, grayscale).getRGB();
		}
		img.setRGB(0, 0, img.getWidth(), img.getHeight(), rgbArray, 0, img.getWidth());
        visualPanel.repaint();
	}
	
}
