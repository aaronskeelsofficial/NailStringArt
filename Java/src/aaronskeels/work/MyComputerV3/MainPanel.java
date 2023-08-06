package aaronskeels.work.MyComputerV3;

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
		        	targetImageToCopy = MainV3.OffscreenImage;
				MainV3.openMaximizedImage(targetImageToCopy);
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
        	targetImageToCopy = MainV3.OffscreenImage;
        }
        
        double scale = Math.min((double) width / targetImageToCopy.getWidth(), (double) height / targetImageToCopy.getHeight());
        int scaledCanvasWidth = (int) (targetImageToCopy.getWidth() * scale);
        int scaledCanvasHeight = (int) (targetImageToCopy.getHeight() * scale);
        g2d.drawImage(targetImageToCopy, (int) ((width - scaledCanvasWidth) / 2d), (int) ((height - scaledCanvasHeight) / 2d), scaledCanvasWidth, scaledCanvasHeight, null);
    }
	
	public void drawNails() {
		drawNails(MainV3.OffscreenImage);
		drawNails(debugOverlayImage, Color.pink);
	}
	private void drawNails(BufferedImage targetImage) {
		drawNails(targetImage, null);
	}
	private void drawNails(BufferedImage targetImage, Color overrideColor) {
		//Wipe old stuff
		MainV3.clearImage(targetImage, Color.white);
		
		Graphics2D g2d = targetImage.createGraphics();
        //Draw nails
		g2d.setColor(overrideColor == null ? Color.black : overrideColor);
		for (Point2D.Float point : MainV3.getNailLocations()) {
			//point x,y are in 0 -> 1 and are anchored in the topleft
			//convert so they are in in offscreen space
			float x = point.x * targetImage.getWidth();
			float y = point.y * targetImage.getHeight();
			//convert so their anchor is their center
			x -= MainV3.NAIL_RADIUS; //Shifts -1 -> 1 into 0 -> 1, then translates to center the oval
			y -= MainV3.NAIL_RADIUS; //Shifts -1 -> 1 into 0 -> 1, then translates to center the oval
			g2d.fillOval(Math.round(x), Math.round(y), MainV3.NAIL_RADIUS*2, MainV3.NAIL_RADIUS*2);
		}
		repaint();
		g2d.dispose();
	}
	
	public void drawString(int parentNailIndex, int childNailIndex, boolean doRepaint) {
		isShowingDebug = false;
		Graphics2D g2d = MainV3.OffscreenImage.createGraphics();
		
		int x1 = Math.round(MainV3.getNailLocations()[parentNailIndex].x * MainV3.OffscreenImage.getWidth());
		int y1 = Math.round(MainV3.getNailLocations()[parentNailIndex].y * MainV3.OffscreenImage.getHeight());
		int x2 = Math.round(MainV3.getNailLocations()[childNailIndex].x * MainV3.OffscreenImage.getWidth());
		int y2 = Math.round(MainV3.getNailLocations()[childNailIndex].y * MainV3.OffscreenImage.getHeight());
		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(1f));
		g2d.drawLine(x1, y1, x2, y2);
		
		g2d.dispose();
		if (doRepaint)
			repaint();
	}
	
	public void drawAllStrings() {
		int pInd = 0, cInd = 1;
		while (cInd < MainV3.getCurNailOrderIndex()) {
			drawString(MainV3.getNailOrder()[pInd], MainV3.getNailOrder()[cInd], false);
			pInd++;
			cInd++;
		}
		repaint();
	}
	
	public void debugDrawString(String ID) {
		int commaIndex = ID.indexOf(",");
		int childNailIndex = Integer.parseInt(ID.substring(0, commaIndex));
		int parentNailIndex = Integer.parseInt(ID.substring(commaIndex+1));
		debugDrawString(parentNailIndex, childNailIndex);
	}
	public void debugDrawString(int childNailIndex, int parentNailIndex) {
		if (!MainV3.influenceMap.containsKey(parentNailIndex + "," + childNailIndex) && !MainV3.influenceMap.containsKey(childNailIndex + "," + parentNailIndex))
			return;
		MainV3.clearImage(debugOverlayImage, Color.white);
		drawNails(debugOverlayImage, Color.pink);
		Graphics2D g2d = debugOverlayImage.createGraphics();
		g2d.setColor(Color.black);
		g2d.setStroke(new BasicStroke(1f));
		int x1 = Math.round(MainV3.getNailLocations()[parentNailIndex].x * debugOverlayImage.getWidth());
		int y1 = Math.round(MainV3.getNailLocations()[parentNailIndex].y * debugOverlayImage.getHeight());
		int x2 = Math.round(MainV3.getNailLocations()[childNailIndex].x * debugOverlayImage.getWidth());
		int y2 = Math.round(MainV3.getNailLocations()[childNailIndex].y * debugOverlayImage.getHeight());
		System.out.println("Debugging string: " + parentNailIndex + "," + childNailIndex + " :: " + x1 + "," + y1 + "  : " + x2 + "," + y2);
		byte[] targetGrayscaleCache = MainV3.loadGrayscale(MainV3.TargetImage);
        byte[] computedGrayscaleCache = MainV3.loadGrayscale(MainV3.ComputedImage);
        double fullStrengthInfluenceWeightMult = MainV3.fullStrengthInfluenceWeight/100d;
        double testScore = MainV3.computeScoreOfAddingProposedString(parentNailIndex + "," + childNailIndex, fullStrengthInfluenceWeightMult, targetGrayscaleCache, computedGrayscaleCache, MainV3.getScoreWeights());
		System.out.println("Score: " + testScore);
		g2d.drawLine(x1, y1, x2, y2);
		isShowingDebug = true;
		repaint();
		g2d.dispose();
	}
	
	public void resetDrawing() {
		drawNails();
	}
	
}
