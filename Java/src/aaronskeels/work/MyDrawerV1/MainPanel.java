package aaronskeels.work.MyDrawerV1;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class MainPanel extends JPanel{
	public BufferedImage debugOverlayImage = new BufferedImage(1920, 1920, BufferedImage.TYPE_INT_RGB);
	public boolean isShowingDebug = false;
	
	public MainPanel() {
		setBackground(Color.green);
		addClickListener();
	}
	
	private void addClickListener() {
		addMouseListener(new MouseListener() {
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
				BufferedImage targetImageToCopy = null;
		        if (isShowingDebug)
		        	targetImageToCopy = debugOverlayImage;
		        else
		        	targetImageToCopy = Main.OffscreenImage;
				Main.openMaximizedImage(targetImageToCopy);
			}
		});
	}
	
	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        int width = getWidth();
        int height = getHeight();
        
        BufferedImage targetImageToCopy = null;
        if (isShowingDebug) {
        	targetImageToCopy = debugOverlayImage;
        } else {
        	targetImageToCopy = Main.OffscreenImage;
        }
        
        double scale = Math.min((double) width / targetImageToCopy.getWidth(), (double) height / targetImageToCopy.getHeight());
        int scaledCanvasWidth = (int) (targetImageToCopy.getWidth() * scale);
        int scaledCanvasHeight = (int) (targetImageToCopy.getHeight() * scale);
        g2d.drawImage(targetImageToCopy, (int) ((width - scaledCanvasWidth) / 2d), (int) ((height - scaledCanvasHeight) / 2d), scaledCanvasWidth, scaledCanvasHeight, null);
    }
	
	public void drawNails() {
		drawNails(Main.OffscreenImage);
		drawNails(debugOverlayImage, Color.pink);
	}
	private void drawNails(BufferedImage targetImage) {
		drawNails(targetImage, null);
	}
	private void drawNails(BufferedImage targetImage, Color overrideColor) {
		//Wipe old stuff
		Main.clearImage(targetImage, Color.white);
		
		Graphics2D g2d = targetImage.createGraphics();
        //Draw nails
		g2d.setColor(overrideColor == null ? Color.black : overrideColor);
		for (Point2D.Float point : Main.getNailLocations()) {
			//point x,y are in 0 -> 1 and are anchored in the topleft
			//convert so they are in in offscreen space
			float x = point.x * targetImage.getWidth();
			float y = point.y * targetImage.getHeight();
			//convert so their anchor is their center
			x -= Main.NAIL_RADIUS; //Shifts -1 -> 1 into 0 -> 1, then translates to center the oval
			y -= Main.NAIL_RADIUS; //Shifts -1 -> 1 into 0 -> 1, then translates to center the oval
			g2d.fillOval(Math.round(x), Math.round(y), Main.NAIL_RADIUS*2, Main.NAIL_RADIUS*2);
		}
		repaint();
		g2d.dispose();
	}
	
	public void drawString(int parentNailIndex, int childNailIndex, boolean doRepaint) {
		isShowingDebug = false;
		Graphics2D g2d = Main.OffscreenImage.createGraphics();
		
		int x1 = Math.round(Main.getNailLocations()[parentNailIndex].x * Main.OffscreenImage.getWidth());
		int y1 = Math.round(Main.getNailLocations()[parentNailIndex].y * Main.OffscreenImage.getHeight());
		int x2 = Math.round(Main.getNailLocations()[childNailIndex].x * Main.OffscreenImage.getWidth());
		int y2 = Math.round(Main.getNailLocations()[childNailIndex].y * Main.OffscreenImage.getHeight());
		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(1f));
		g2d.drawLine(x1, y1, x2, y2);
		
		g2d.dispose();
		if (doRepaint)
			repaint();
	}
	
	public void drawThroughStringNumber(int stringNumber) {
		Main.clearImage(Main.OffscreenImage, Color.white);
		drawNails();
		int pInd = 0, cInd = 1;
		while (cInd <= stringNumber) {
			drawString(Main.getNailOrder()[pInd], Main.getNailOrder()[cInd], false);
			pInd++;
			cInd++;
		}
		repaint();
	}
	
	public void drawAllStrings() {
		int pInd = 0, cInd = 1;
		while (cInd < Main.getCurNailOrderIndex()) {
			drawString(Main.getNailOrder()[pInd], Main.getNailOrder()[cInd], false);
			pInd++;
			cInd++;
		}
		repaint();
	}
	
	public void resetDrawing() {
		drawNails();
	}
	
}
