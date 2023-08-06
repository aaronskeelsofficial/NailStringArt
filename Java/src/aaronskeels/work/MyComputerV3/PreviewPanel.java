package aaronskeels.work.MyComputerV3;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class PreviewPanel extends JPanel{
	public TargetPanel uploadPanel = null;
	public ComputedPanel computedPanel = null;

	public PreviewPanel() {
		setBackground(Color.blue);
		setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH; // Components fill both horizontally and vertically
        constraints.weightx = 1;
        constraints.weighty = 1;
		
		uploadPanel = new TargetPanel();
		computedPanel = new ComputedPanel();
		
		constraints.gridy = 0;
		add(uploadPanel, constraints);
		constraints.gridy = 1;
		add(computedPanel, constraints);
	}
	
}
