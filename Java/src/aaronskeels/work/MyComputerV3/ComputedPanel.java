package aaronskeels.work.MyComputerV3;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ComputedPanel extends JPanel{

	public ComputedPanel() {
		setBackground(Color.magenta);
		
		addClickListener();
	}
	
	public void addClickListener() {
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
				MainV3.openMaximizedImage(MainV3.ComputedImage);
			}
		});
	}
	
	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        int width = getWidth();
        int height = getHeight();
        
        double scale = Math.min((double) width / MainV3.ComputedImage.getWidth(), (double) height / MainV3.ComputedImage.getHeight());
        int scaledCanvasWidth = (int) (MainV3.ComputedImage.getWidth() * scale);
        int scaledCanvasHeight = (int) (MainV3.ComputedImage.getHeight() * scale);
        g2d.drawImage(MainV3.ComputedImage, (int) ((width - scaledCanvasWidth) / 2d), (int) ((height - scaledCanvasHeight) / 2d), scaledCanvasWidth, scaledCanvasHeight, null);
    }
	
}
