package aaronskeels.work.MyComputerV3;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

/*
 * MainV1 works around the 32GB of precomputed influence data by splitting it into many files (one file per string)
 * This is slow. Not good.
 */
public class MainV3 {
	//Config Numbers
	public static final int NAIL_RADIUS = 5;
	private static final File rootFolder = new File(System.getProperty("user.home") + System.getProperty("file.separator") + "NailStringArt_InfluenceCache");
	//Graphics objects
	public static final JFrame frame = new JFrame("Nail String Art Drawer");
	public static final BufferedImage OffscreenImage = new BufferedImage(4000, 4000, BufferedImage.TYPE_INT_RGB);
	public static BufferedImage ComputedImage = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB), //These must resize for computation purposes
			TargetImage = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
	public static final InputPanel inputPanel = new InputPanel();
	public static final MainPanel mainPanel = new MainPanel();
	public static final PreviewPanel previewPanel = new PreviewPanel();
	//Other global objects
	private static Point2D.Float[] nailLocations = null;
	private static int[] nailOrder = new int[50000];
	public static Set<String> remainingStringsForConsideration = new HashSet<String>();
	public static boolean isDoneAddingStrings = false;
	public static double fullStrengthInfluenceWeight = 0;
	public static Map<String, Map<Integer, Byte>> influenceMap = new HashMap<>(); // <ID: <pixelIndex: influence>>
	//Beta vars
	public static double pixelDistanceTraveled = 0;
	//Distance to Influence objects
		//Version 1: 5px wide total, 1px wide 100%, 2px wide 50%. Technically, line is problematically offset by 1 pixel up and to the right from where "string" would be
//	private static final float[] INFLUENCE_distArr = new float[] {0f,0.7f,2.1f};
//	private static final byte[] INFLUENCE_influenceArr = new byte[] {100,50,0};
		//Version 2: 3px wide total, 1px wide 100%, 1px wide 50%. Technically, line is problematically offset by 1 pixel up and to the right from where "string" would be
	private static final float[] INFLUENCE_distArr = new float[] {0f,0.7f,1.4f};
	private static final byte[] INFLUENCE_influenceArr = new byte[] {100,50,0};
	
	
	public static void main(String[] args) {
		//Setup frame config
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH; // Components fill both horizontally and vertically
        constraints.weighty = 1;

        //Initialize Vars
        resetNailOrder();
        
        //Add components to frame
        constraints.gridx = 0;
        constraints.weightx = .5;
        frame.add(inputPanel, constraints);
        constraints.gridx = 1;
        constraints.weightx = 2.0;
        frame.add(mainPanel, constraints);
        constraints.gridx = 2;
        constraints.weightx = 1.0;
        frame.add(previewPanel, constraints);
        
        //Finally show frame
        frame.setVisible(true);
	}
	
	public static Point2D.Float[] getNailLocations() {
		return nailLocations;
	}
	public static void setNailLocations(Point2D.Float[] value) {
		nailLocations = value;
		
		mainPanel.drawNails();
		resetRemainingStringsForConsideration();
	}
	public static int[] getNailOrder() {
		return nailOrder;
	}
	public static void setNailOrder(int[] value) {
		resetNailOrder();
		System.arraycopy(value, 0, nailOrder, 0, value.length);
		mainPanel.drawAllStrings();
	}
	
	public static void updateMainPanel() {
		mainPanel.repaint();
	}
	
	public static void addStringToComputed(String ID) {
		int commaIndex = ID.indexOf(",");
		int nailIndex1 = Integer.parseInt(ID.substring(0, commaIndex));
		int nailIndex2 = Integer.parseInt(ID.substring(commaIndex+1));
		int parentNailIndex, childNailIndex;
		if (getCurNailOrderIndex() == 0 || getNailOrder()[getCurNailOrderIndex()-1] == nailIndex1) { //If nail order is empty or take note of the last touched nail
			parentNailIndex = nailIndex1;
			childNailIndex = nailIndex2;
		} else {
			childNailIndex = nailIndex1;
			parentNailIndex = nailIndex2;
		}
		
		//Draw onto ComputedImage
		Map<Integer, Byte> localInfluenceMapping = influenceMap.get(ID);
		int pixelIndex = 0;
		double fullStrengthPixelInfluencePerStringMult = fullStrengthInfluenceWeight/100d;
		for (int y = 0;y < ComputedImage.getHeight();y++) {
			for (int x = 0;x < ComputedImage.getWidth();x++,pixelIndex++) {
				if (!localInfluenceMapping.containsKey(pixelIndex))
					continue;
				
				int curGrayscale = new Color(ComputedImage.getRGB(x, y)).getRed();
				int newGrayscale = Math.min(curGrayscale + (int) (getByteUnsigned(localInfluenceMapping.get(pixelIndex))*fullStrengthPixelInfluencePerStringMult), 255);
				ComputedImage.setRGB(x, y, new Color(newGrayscale, newGrayscale, newGrayscale).getRGB());
			}
		}
		
		//Draw in MainPanel
		mainPanel.drawString(parentNailIndex, childNailIndex, true);
		
		//Update panel
		previewPanel.computedPanel.repaint();
		
		//Update nail order
		if (getCurNailOrderIndex() == 0) { //This means nothing has been added yet
			getNailOrder()[getCurNailOrderIndex()] = parentNailIndex;
		}
		getNailOrder()[getCurNailOrderIndex()] = childNailIndex;
		
		//Remove from remaining strings in database
		remainingStringsForConsideration.remove(ID);
		
		//Update distance travelled
		Point2D.Float parentNailLoc = nailLocations[parentNailIndex];
		Point2D.Float childNailLoc = nailLocations[childNailIndex];
		// x,y are mapped 0 -> 1
		float parentX = parentNailLoc.x, parentY = parentNailLoc.y;
		float childX = childNailLoc.x, childY = childNailLoc.y;
		// Convert to 0 -> Circle Diameter
		parentX *= ComputedImage.getWidth(); parentY *= ComputedImage.getWidth();
		childX *= ComputedImage.getWidth(); childY *= ComputedImage.getWidth();
		pixelDistanceTraveled += Math.sqrt(Math.pow(parentX - childX, 2) + Math.pow(parentY - childY, 2));
//		System.out.println("(" + parentNailLoc.x + "," + parentNailLoc.y + ") -> (" + childNailLoc.x + "," + childNailLoc.y + ")");
	}
	
	public static void computeInfluenceMap(int fullStrengthInfluenceWeight) {
		File file = new File(rootFolder, nailLocations.length + "_" + ComputedImage.getWidth() + ".dat");
		if (influenceMap.keySet().size() == nChooseK(nailLocations.length, 2)) {
			System.out.println("This influence is already loaded. Updating fullStrength.");
			resetRemainingStringsForConsideration();
			MainV3.mainPanel.resetDrawing();
			MainV3.fullStrengthInfluenceWeight = fullStrengthInfluenceWeight;
			return;
		}
		if (file.exists()) {
			System.out.println("Influence Already Generated. Attempting to load...");
			loadInfluenceMap();
			resetRemainingStringsForConsideration();
			MainV3.fullStrengthInfluenceWeight = fullStrengthInfluenceWeight;
			return;
		}
		
		Set<String> completedIDList = new HashSet<String>();
		String ID = "";
		Point2D.Float parentNailLoc = null,
				childNailLoc = null;
		double computedToNormalizedSpace = 1d / ((double) ComputedImage.getWidth()), normalizedToComputedSpace = ((double) ComputedImage.getWidth()) / 1d;
		for (int parentNailIndex = 0;parentNailIndex < nailLocations.length;parentNailIndex++) {
			System.out.println("Computing Parent Nail Index: " + parentNailIndex);
			parentNailLoc = nailLocations[parentNailIndex];
			for (int childNailIndex = 0;childNailIndex < nailLocations.length;childNailIndex++) {
				childNailLoc = nailLocations[childNailIndex];
				ID = parentNailIndex + "," + childNailIndex;
				if (parentNailIndex == childNailIndex || completedIDList.contains(ID))
					continue;
				
				//Load influence locally
				int influencePixelIndex = 0;
				Map<Integer, Byte> influenceLocalMapping = influenceMap.containsKey(ID) ? influenceMap.get(ID) : new HashMap<Integer, Byte>();
				for (int py = 0;py < ComputedImage.getHeight();py++) {
					for (int px = 0;px < ComputedImage.getWidth();px++,influencePixelIndex++) {
						double distance = distancePointToLine(px, py, parentNailLoc.x, parentNailLoc.y, childNailLoc.x, childNailLoc.y, computedToNormalizedSpace, normalizedToComputedSpace);
						byte influence = distanceToInfluence(distance);
//						if (parentNailIndex == 0 && childNailIndex == 1 && influence != 0)
//							System.out.println("(" + px + "," + py + "," + influencePixelIndex + "): " + distance + " : " + influence);
						if (influence > 0) {
							influenceLocalMapping.put(influencePixelIndex, influence);
						}
					}
				}
				
				//Transfer to global
				influenceMap.put(ID, influenceLocalMapping);
				
				completedIDList.add(ID);
				completedIDList.add(childNailIndex + "," + parentNailIndex);
			}
		}
		
		System.out.println("Attempting to save influence map...");
		saveInfluenceMap(influenceMap);
		System.out.println("Saved influence map!");
		resetRemainingStringsForConsideration();
		MainV3.fullStrengthInfluenceWeight = fullStrengthInfluenceWeight;
	}
	
	public static void computeNextBestString(double fullStrengthInfluenceMult, double[] scoreWeights) {
		if (isDoneAddingStrings) {
			System.out.println("Already done computing strings!");
	        return;
		}

		Set<String> remainingStringsForConsideration = null;
	    if (getCurNailOrderIndex() == 0) { //Is first string being added (so check ALL combos, and note both parent and child nail)
	    	resetStringAnalysis();
	        remainingStringsForConsideration = MainV3.remainingStringsForConsideration;
	        System.out.println("Computing first string. This may take longer.");
	    } else { // Is not the very first string (so only check from parent, only note child nail)
	    	remainingStringsForConsideration = new HashSet<String>();
	    	int lastUsedNail = getNailOrder()[getCurNailOrderIndex()-1];
	    	for (String ID : MainV3.remainingStringsForConsideration) {
	    		if (ID.startsWith(lastUsedNail + ",") || ID.endsWith("," + lastUsedNail)) //We don't want a bloat of data so instead of (nail #)*(nail #) duplicated data, we have "(nail #) choose 2" data which requires this custom naming check
	                remainingStringsForConsideration.add(ID);
	    	}
	    }
	    
        // Loop through all possible combos for the most beneficial starting location.
        double maximumScore = 0;
        String maximumID = "";
        byte[] targetGrayscaleCache = loadGrayscale(TargetImage);
        byte[] computedGrayscaleCache = loadGrayscale(ComputedImage);
        for (int i = 0;i < remainingStringsForConsideration.size();i++) {
            String ID = (String) remainingStringsForConsideration.toArray()[i];
            double testScore = computeScoreOfAddingProposedString(ID, fullStrengthInfluenceMult, targetGrayscaleCache, computedGrayscaleCache, scoreWeights);
            if(testScore > maximumScore) {
                maximumScore = testScore;
                maximumID = ID;
            }
        }
        //Apply most beneficial to canvas and add to nail traversal database
        if (!maximumID.equals("")) {
            String ID = maximumID;
            addStringToComputed(ID);
//            System.out.println("Best String is ID: " + ID + " (" + maximumScore + ")" +  "   Index: " + i);
        } else {
            isDoneAddingStrings = true;
            System.out.println("Done adding strings now");
            return;
        }
	}
	
	public static double computeScoreOfAddingProposedString(String ID, double fullStrengthPixelInfluencePerStringMult, byte[] targetGrayscaleCache, byte[] computedGrayscaleCache, double[] weights) {
		if (!remainingStringsForConsideration.contains(ID)) {
			System.out.println("ERROR: Trying to compute score of invalid ID: " + ID);
	        return -999999999; //Has to be more influential than a realistic score, but not so large it overflows when added
		}
        
		//V2
	    double transitionUnderdrawnDeltaScore = 0; //This represents the score encountered upon which the deltaValue leads to a transition to/beyond targetValue (character fill)
	    double transitionOverdrawnDeltaScore = 0; //This represents the score encountered upon which the deltaValue supercedes the targetValue during a transition (character fill)
	    double overdrawingPitchBlackScore = 0; //This represents the score of the pitch black pixels influenced (background)
	    double completeOverdrawingScore = 0; //This represents the score drawing where we have already surpassed targetValue from the beginning (overdrawn character)
	    double avoidEncounteredScore = 0; //This represents the score of how many "avoid" pixels are encountered
	    double rawPositiveInfluenceScore = 0; //This represents the entire amount of influence still missing. This is good for prioritizing painting where larger gaps exist
	    double rawNegativeInfluenceScore = 0; //This represents the entire amount of influence abundance. This is good for prioritizing avoiding painting where larger gaps exist
	    double targetValue = 0, curValue = 0, deltaValue = 0, addTemp = 0;
	    Map<Integer, Byte> localInfluenceMapping = influenceMap.get(ID);
	    if (localInfluenceMapping == null) {
	    	System.out.println(ID + " yields null localInfluenceMapping");
	    }
	    
	    for (int pixelIndex : localInfluenceMapping.keySet()) {
	    	deltaValue = localInfluenceMapping.get(pixelIndex)*fullStrengthPixelInfluencePerStringMult;
	    	targetValue = getByteUnsigned(targetGrayscaleCache[pixelIndex]); //We must do bitwise because Java is stupid and ONLY has signed byte
	    	curValue = getByteUnsigned(computedGrayscaleCache[pixelIndex]); //We must do bitwise because Java is stupid and ONLY has signed byte
	    	
	    	if (targetValue == 0) { //If we are attempting to color a pitch black pixel
	            overdrawingPitchBlackScore += deltaValue;
	            continue;
	    	} else if (targetValue == 20) { //If we have encountered a predetermined pixel value that represents "avoid"
            	avoidEncounteredScore += deltaValue;
            	continue;
	        }
	    	
	    	if (curValue < targetValue) { //If we are currently below the targetvalue
	    		rawPositiveInfluenceScore += targetValue - curValue;
	    	} else {
	    		rawNegativeInfluenceScore += curValue - targetValue;
	    	}
	    	
	    	if (curValue > targetValue) { //If we are already overdrawn, subtract the entire delta from the score (weighted)
	    		completeOverdrawingScore += deltaValue;
	            continue;
	        }
	    	
			addTemp = curValue + deltaValue;
			if (addTemp <= targetValue) { //If we are so underdrawn that adding the entire delta doesn't matter
				transitionUnderdrawnDeltaScore += deltaValue;
			} else if (addTemp > targetValue) { //If we are crossing the threshold from underdrawn -> overdrawn
				transitionUnderdrawnDeltaScore += (targetValue - curValue);
				transitionOverdrawnDeltaScore += (addTemp - targetValue);
			}
	    }
	    return transitionUnderdrawnDeltaScore*weights[0]
	    	+ transitionOverdrawnDeltaScore*weights[1]
	    	+ overdrawingPitchBlackScore*weights[2]
	    	+ completeOverdrawingScore*weights[3]
	    	+ avoidEncounteredScore*weights[4]
	    	+ rawPositiveInfluenceScore*weights[5]
			+ rawNegativeInfluenceScore*weights[6];
		
//		//V1
////		double NEGATIVEWEIGHT = .0001;
////        double NEGATIVESUBTRACT = .0004;
//        double positiveScore = 0;
//        double targetValue = 0, curValue = 0, deltaValue = 0, addTemp = 0, positiveInfluence = 0;
//        Map<Integer, Byte> localInfluenceMapping = influenceMap.get(ID);
//        if (localInfluenceMapping == null) {
//        	System.out.println(ID + " yields null localInfluenceMapping");
//        }
//        
//        for (int pixelIndex : localInfluenceMapping.keySet()) {
//        	deltaValue = localInfluenceMapping.get(pixelIndex)*fullStrengthPixelInfluencePerStringMult;
//        	targetValue = getByteUnsigned(targetGrayscaleCache[pixelIndex]); //We must do bitwise because Java is stupid and ONLY has signed byte
//        	curValue = getByteUnsigned(computedGrayscaleCache[pixelIndex]); //We must do bitwise because Java is stupid and ONLY has signed byte
//        	
//        	if (targetValue == 0) { //If we are attempting to color a pitch black pixel
////        		System.out.println(i + "a: target value == 0");
//	            positiveScore -= negativeSubtractMult; //This should be deltaValue * NEGATIVEWEIGHT but because deltaValue is != 0, we can assume it's 1
//	            continue;
//	        } else if (curValue > targetValue) { //If we are already overdrawn, subtract the entire delta from the score (weighted)
////        		System.out.println(i + "b: " + curValue + " > " + targetValue);
//	            positiveScore -= negativeSubtractMult; //This should be deltaValue * NEGATIVEWEIGHT but because deltaValue is != 0, we can assume it's 1
//	            continue;
//	        }
//        	
//        	// Positive Influence explanation
//			/*
//			* If curValue + influence < or = targetValue, then positive influence is complete influence
//			*
//			* If curValue + influence > targetValue, then positive influence is only segment of influence which contributes to meeting targetValue
//			*  (even though the total addition supercedes, we just ignore the amount of deltaValue aka influence which passes the target)
//			*/
//			addTemp = curValue + deltaValue;
//			if (addTemp <= targetValue) { //If we are so underdrawn that adding the entire delta doesn't matter
////          	System.out.println(i + "c: " + addTemp + " <= " + targetValue);
//				positiveInfluence = deltaValue;
//			} else if (addTemp > targetValue) { //If we are crossing the threshold from underdrawn -> overdrawn
////          	System.out.println(i + "d: " + addTemp + " <= " + targetValue);
//				double underdrawnChange = targetValue - curValue;
//				double overdrawnChange = addTemp - targetValue;
//				positiveInfluence = underdrawnChange - overdrawnChange*1;
////				positiveInfluence = underdrawnChange;
//			}
//			positiveScore += positiveInfluence;
//        }
//        return positiveScore;
	}
	
	public static double distancePointToLine(float px, float py, float x1, float y1, float x2, float y2, double computedToNormalizedSpace, double normalizedToComputedSpace) {
		// I could have done this myself but I got this from ChatGPT for sake of time
	    // Also I had to modify it because it didn't account for horizontal slopes
		//px,py is in 0 -> ComputedImage.width space
		//x1,y1,x2,y2 is in 0 -> 1 space

	    if (x1 == x2 && y1 == y2)
	        return 9999999d;
	    
	    // x1,y1,x2,y2 are in 0->1 space
	    // px,py are in computed space
	    // convert px,py into 0->1 space
	    px *= computedToNormalizedSpace;
	    py *= computedToNormalizedSpace;

	    //Implement Bounding Box Optimization Conditions
	    final float minX = Math.min(x1, x2);
	    final float minY = Math.min(y1, y2);
	    final float maxX = Math.max(x1, x2);
	    final float maxY = Math.max(y1, y2);
	    if (px <= maxX && px >= minX && py <= maxY && py >= minY) { //If is in bounding box
	    	if (Float.floatToIntBits(y1) == Float.floatToIntBits(y2)) {
//		    	System.out.println("case 1");
		        double distance = Math.abs(py - y1);
		        // Distance is in Nail space, we must convert to Downscale space
		        distance *= normalizedToComputedSpace;
		        return distance;
		    } else if (Float.floatToIntBits(x1) == Float.floatToIntBits(x2)) {
//		    	System.out.println("case 2");
		        double distance = Math.abs(px - x1);
		        // Distance is in Nail space, we must convert to Downscale space
		        distance *= normalizedToComputedSpace;
		        return distance;
		    }
//	    	System.out.println(px + "," + py + " is case 3");
	        // Step 1: Calculate the equation of the line (y = mx + c)
	        float m = (y2 - y1) / (x2 - x1); // Slope
	        final float c = y1 - m * x1; // Y-intercept
	        // Step 2: Find the perpendicular line passing through the point (px, py)
	        final float mPerpendicular = -1 / m; // Perpendicular line has a negative reciprocal slope
	        final float cPerpendicular = py - mPerpendicular * px;
	        // Step 3: Calculate the intersection point of the perpendicular line and the original line
	        final float intersectionX = (cPerpendicular - c) / (m - mPerpendicular);
	        final float intersectionY = m * intersectionX + c;
	        // Step 4: Calculate the distance between the point (px, py) and the intersection point
	        double distance = Math.sqrt(Math.pow(px - intersectionX, 2) + Math.pow(py - intersectionY, 2));
	        // Step 5: Distance is in Nail space, we must convert to Downscale space
	        distance *= normalizedToComputedSpace;
	        return distance;
	    } else { //If not in bounding box
//	    	System.out.println(px + "," + py + " is case 4");
	        float distanceFromP1 = Math.abs(px - x1) + Math.abs(py - y1);
	        float distanceFromP2 = Math.abs(px - x2) + Math.abs(py - y2);
	        float distance = (distanceFromP1 < distanceFromP2) ? distanceFromP1 : distanceFromP2;
	        distance *= normalizedToComputedSpace; //Convert to downscale space from nail space
	        return distance;
	    }
	}
	public static byte distanceToInfluence(double distance) {
	    if (distance > INFLUENCE_distArr[INFLUENCE_distArr.length-1])
	        return 0;

	    // let cacheMultip = FullStrengthPixelInfluencePerString/100;
	    for (int i = INFLUENCE_distArr.length-1;i >= 0;i--) {
	        if (distance >= INFLUENCE_distArr[i]) {
	            return INFLUENCE_influenceArr[i];
	            // return PixelInfluenceCurve.influenceArr[i]*cacheMultip;
	        }
	    }
	    
		return 0;
	}
	
	public static void loadInfluenceMap() {
		File file = new File(rootFolder, nailLocations.length + "_" + ComputedImage.getWidth() + ".dat");
		if (!file.exists()) {
			System.out.println("Error: Influence map can't be loaded. Doesn't exist!");
			return;
		}
		
		//250 nails, 1000 resolution, 9 seconds to load w/ buffer. I'm SURE that's faster than without.
		try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            @SuppressWarnings("unchecked")
			Map<String, Map<Integer, Byte>> map = (Map<String, Map<Integer, Byte>>) ois.readObject();
            influenceMap = map;
            System.out.println("Map loaded from " + file.getName());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
	}
	
	public static void resetRemainingStringsForConsideration() {
		remainingStringsForConsideration.clear();
		for (String s : influenceMap.keySet()) {
			remainingStringsForConsideration.add(s);
		}
	}
	public static void resetNailOrder() {
		Arrays.fill(nailOrder, -1);
	}
	public static void resetStringAnalysis() {
		resetNailOrder();
		resetRemainingStringsForConsideration();
		isDoneAddingStrings = false;
		//Wipe ComputedImage
		for (int y = 0;y < ComputedImage.getHeight();y++) {
			for (int x = 0;x < ComputedImage.getWidth();x++) {
				ComputedImage.setRGB(x, y, Color.black.getRGB());
			}
		}
		previewPanel.computedPanel.repaint();
		mainPanel.resetDrawing();
		//Update distance travelled
		pixelDistanceTraveled = 0;
	}
	
	public static void saveInfluenceMap(Map<String, Map<Integer, Byte>> map) {
		File file = new File(rootFolder, nailLocations.length + "_" + ComputedImage.getWidth() + ".dat");
		if (!rootFolder.exists())
			rootFolder.mkdir();
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		//250 nails, 1000 resolution, 2:39 to compute, 8:14 to save no buffer
		//250 nails, 1000 resolution, 2:36 to compute, 0:30 to save w/ buffer (VERY clear winner)
		try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            oos.writeObject(map);
            oos.flush();
            System.out.println("Map saved to " + file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	//
	// MISC UTILITIES
	//
	
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
	
	public static void firstNonzeroIndex(byte[] arr) {
		for (int i = 0;i < arr.length;i++) {
			if (arr[i] != 0) {
				System.out.println("Index " + i + " is " + arr[i]);
				return;
			}
		}
	}
	
	public static int getByteUnsigned(byte b) {
		return ((int)(b)) & 0xFF;
	}
	
	public static int getCurNailOrderIndex() {
		for (int i = 0;i < getNailOrder().length;i++) {
			if (getNailOrder()[i] == -1)
				return i;
		}
		return -1;
	}
	
	public static String getExportFilePrefix() {
		return stripExtension(previewPanel.uploadPanel.targetFile.getName()) + "_"
				+ nailLocations.length + "_"
				+ ComputedImage.getWidth() + "_"
				+ fullStrengthInfluenceWeight + "_"
//				+ inputPanel.scoreWeight1 + "_"
//				+ inputPanel.scoreWeight2 + "_"
//				+ inputPanel.scoreWeight3 + "_"
//				+ inputPanel.scoreWeight4 + "_"
//				+ inputPanel.scoreWeight5 + "_"
				+ getCurNailOrderIndex();
	}
	public static String getExportFilePathPrefix() {
		return previewPanel.uploadPanel.targetFile.getParentFile().getAbsolutePath() + File.pathSeparator + stripExtension(previewPanel.uploadPanel.targetFile.getName()) + "_"
				+ nailLocations.length + "_"
				+ ComputedImage.getWidth() + "_"
				+ ((int)fullStrengthInfluenceWeight) + "_"
//				+ inputPanel.scoreWeight1 + "_"
//				+ inputPanel.scoreWeight2 + "_"
//				+ inputPanel.scoreWeight3 + "_"
//				+ inputPanel.scoreWeight4 + "_"
//				+ inputPanel.scoreWeight5 + "_"
				+ getCurNailOrderIndex();
	}
	
	public static double[] getScoreWeights() {
		try {
			double[] scoreWeights = new double[] {Double.parseDouble(inputPanel.scoreWeight1.getText()), Double.parseDouble(inputPanel.scoreWeight2.getText()), Double.parseDouble(inputPanel.scoreWeight3.getText()),
					Double.parseDouble(inputPanel.scoreWeight4.getText()), Double.parseDouble(inputPanel.scoreWeight5.getText()), Double.parseDouble(inputPanel.scoreWeight6.getText()), Double.parseDouble(inputPanel.scoreWeight7.getText())};
			return scoreWeights;
		} catch (Exception e) {
			System.out.println("Error: Weights aren't floats");
			return null;
		}
	}
	
	public static double nChooseK(int n, int k) {
		double[] logf = new double[] {0,0,0.6931471805599453,1.791759469228055,3.1780538303479453,4.787491742782046,6.579251212010101,8.525161361065415,10.60460290274525,12.80182748008147,15.104412573075518,17.502307845873887,19.98721449566189,22.552163853123425,25.191221182738683,27.899271383840894,30.671860106080675,33.50507345013689,36.39544520803305,39.339884187199495,42.335616460753485,45.38013889847691,48.47118135183523,51.60667556776438,54.784729398112326,58.003605222980525,61.26170176100201,64.55753862700634,67.88974313718154,71.25703896716801,74.65823634883017,78.09222355331532,81.55795945611504,85.05446701758153,88.5808275421977,92.13617560368711,95.71969454214322,99.33061245478744,102.96819861451382,106.63176026064347,110.3206397147574,114.0342117814617,117.77188139974507,121.53308151543864,125.3172711493569,129.12393363912722,132.95257503561632,136.80272263732638,140.67392364823428,144.5657439463449,148.47776695177305,152.40959258449737,156.3608363030788,160.33112821663093,164.3201122631952,168.32744544842768,172.35279713916282,176.39584840699737,180.45629141754378,184.5338288614495,188.6281734236716,192.7390472878449,196.86618167288998,201.00931639928152,205.1681994826412,209.34258675253685,213.53224149456327,217.73693411395422,221.95644181913033,226.1905483237276,230.43904356577696,234.70172344281826,238.97838956183432,243.2688490029827,247.57291409618688,251.8904022097232,256.22113555000954,260.5649409718632,264.92164979855283,269.29109765101987,273.67312428569375,278.0675734403662,282.47429268763045,286.89313329542705,291.32395009427034,295.76660135076065,300.22094864701415,304.6868567656687,309.16419358014696,313.6528299498791,318.15263962020936,322.6634991267262,327.18528770377526,331.71788719692853,336.2611819791985,340.8150588707991,345.3794070622669,349.9541180407703,354.5390855194409,359.1342053695755,363.7393755555636,368.35449607240486,372.97946888568913,377.6141978739188,382.2585887730602,386.91254912321773,391.5759882173298,396.24881705179166,400.9309482789159,405.622296161145,410.32277652693745,415.0323067282498,419.7508055995449,424.4781934182572,429.2143918666517,433.9593239950149,438.7129141861213,443.47508812091905,448.2457727453847,453.02489623849624,457.8123879812783,462.608178526875,467.41219957160826,472.2243839269807,477.04466549258575,481.8729792298881,486.70926113683953,491.5534482232981,496.40547848721775,501.2652908915794,506.132825342035,511.0080226652362,515.8908245878225,520.7811737160442,525.6790135159952,530.5842882944336,535.4969431801696,540.4169241059977,545.344177791155,550.2786517242856,555.220294146895,560.1690540372731,565.1248810948744,570.0877257251343,575.0575390247103,580.0342727671309,585.0178793888392,590.008311975618,595.0055242493821,600.0094705553275,605.0201058494238,610.0373856862387,615.061266207085,620.0917041284775,625.1286567308912,630.1720818478104,635.22193785506,640.2781836604083,645.3407786934353,650.4096828956555,655.4848567108893,660.5662610758737,665.6538574111062,670.7476076119129,675.8474740397371,680.9534195136376,686.0654073019941,691.1834011144109,696.3073650938142,701.4372638087373,706.5730622457876,711.7147258022902,716.8622202791037,722.0155118736014,727.174567172816,732.3393531467394,737.5098371417776,742.6859868743513,747.8677704246434,753.0551562304842,758.2481130813744,763.4466101126402,768.650616799717,773.8601029525585,779.0750387101674,784.2953945352457,789.5211412089589,794.7522498258135,799.9886917886435,805.2304388037031,810.4774628758636,815.7297363039102,820.9872316759379,826.2499218648428,831.5177800239061,836.7907795824698,842.0688942417003,847.3520979704383,852.6403650011329,857.9336698258574,863.2319871924054,868.5352921004645,873.8435597978657,879.1567657769075,884.4748857707517,889.7978957498901,895.1257719186797,900.458490711945,905.7960287916463,911.1383630436111,916.4854705743286,921.8373287078047,927.1939149824767,932.5552071481861,937.921183163208,943.2918211913357,948.6670995990198,954.0469969525603,959.4314920153495,964.8205637451659,970.2141912915183,975.6123539930361,981.0150313749084,986.4222031463685,991.8338491982236,997.249949600428,1002.6704845997002,1008.0954346171816,1013.5247802461361,1018.9585022496904,1024.3965815586137,1029.8389992691355,1035.2857366408018,1040.7367750943674,1046.1920962097252,1051.6516817238694,1057.115513528895,1062.58357367003,1068.0558443437017,1073.5323078956333,1079.0129468189753,1084.4977437524658,1089.9866814786226,1095.4797429219632,1100.9769111472565,1106.478169357801,1111.9835008937334,1117.4928892303615,1123.0063179765264,1128.523770872991,1134.0452317908532,1139.570684729985,1145.1001138174965,1150.6335033062242,1156.1708375732428,1161.7121011184013,1167.2572785628809,1172.806354647776,1178.3593142326977,1183.916142294397,1189.4768239254126,1195.0413443327354,1200.6096888364966,1206.1818428686745,1211.7577919718208,1217.337521797807,1222.9210181065887,1228.508266764989,1234.0992537455,1239.6939651251018,1245.2923870841003,1250.89450590498,1256.500307971276,1262.109779766461,1267.7229078728492,1273.3396789705157,1278.9600798362328,1284.5840973424201,1290.2117184561107,1295.842930237932,1301.4777198411014,1307.116074510435,1312.757981581373,1318.4034284790164,1324.0524027171775,1329.7048918974463,1335.360883708266,1341.0203659240258,1346.6833264041618,1352.349753092274,1358.0196340152547,1363.6929572824263,1369.3697110846945,1375.0498836937115,1380.7334634610502,1386.42043881739,1392.1107982717142,1397.804530410517,1403.5016238970222,1409.2020674704129,1414.9058499450691,1420.612960209818,1426.323387227193,1432.0371200327024,1437.7541477341088,1443.474459510716,1449.1980446126686,1454.9248923602559,1460.6549921432295,1466.3883334201273,1472.1249057176065,1477.8646986297856,1483.6077018175952,1489.3539050081354,1495.1032979940437,1500.8558706328693,1506.6116128464562,1512.3705146203336,1518.1325660031137,1523.8977571058986,1529.6660781016924,1535.4375192248224,1541.2120707703668,1546.9897230935894,1552.7704666093819,1558.5542917917116,1564.3411891730784,1570.1311493439757,1575.92416295236,1581.7202207031253,1587.5193133575858,1593.321431732963,1599.1265667018795,1604.9347091918598,1610.7458501848366,1616.5599807166616,1622.3770918766247,1628.197174806977,1634.0202207024602,1639.8462208098406,1645.675166427451,1651.5070489047343,1657.3418596417969,1663.1795900889629,1669.0202317463363,1674.8637761633677,1680.7102149384255,1686.5595397183722,1692.4117421981466,1698.266814120349,1704.1247472748323,1709.985533498298,1715.8491646738962,1721.7156327308296,1727.5849296439633,1733.4570474334387,1739.3319781642906,1745.2097139460702,1751.090246932471,1756.9735693209593,1762.8596733524096,1768.7485513107424,1774.640195522568,1780.534598356833,1786.4317522244696,1792.331649578052,1798.2342829114534,1804.139644759508,1810.047727697677,1815.9585243417175,1821.8720273473557,1827.7882294099632,1833.7071232642363,1839.62870168388,1845.5529574812947,1851.4798835072652,1857.409472650655,1863.341717838103,1869.2766120337226,1875.214148238805,1881.1543194915253,1887.097118866652,1893.0425394752585,1898.990574464439,1904.9412170170267,1910.8944603513146,1916.8502977207795,1922.8087224138094,1928.7697277534326,1934.733307097051,1940.6994538361746,1946.66816139616,1952.6394232359505,1958.6132328478197,1964.5895837571177,1970.5684695220189,1976.5498837332734,1982.5338200139606,1988.520272019245,1994.509233436135,2000.500697983243,2006.4946594105497,2012.4911114991687,2018.4900480611154,2024.4914629390767,2030.4953500061831,2036.5017031657849,2042.5105163512276,2048.5217835256317,2054.5354986816747,2060.551655841373,2066.570249055869,2072.5912724052187,2078.6147199981797,2084.640585972005,2090.6688644922356,2096.6995497524968,2102.7326359742956,2108.7681174068202,2114.8059883267424,2120.84624303802,2126.8888758717026,2132.9338811857388,2138.981253364785,2145.030986820017,2151.0830759889413,2157.1375153352105,2163.194299348439,2169.253422544021,2175.3148794629487,2181.378664671636,2187.44477276174,2193.513198349984,2199.5839360779864,2205.656980612087,2211.732326643176,2217.809968886525,2223.8899020816207,2229.972120991997,2236.0566204050724,2242.1433951319846,2248.2324400074313,2254.323749889509,2260.4173196595543,2266.5131442219867,2272.611218504153,2278.711537456173,2284.8140960507867,2290.9188892832017,2297.025912170944,2303.1351597537086,2309.2466270932114,2315.3603092730436,2321.476201398527,2327.594298596568,2333.714596015519,2339.8370888250333,2345.9617722159273,2352.0886414000415,2358.217691610102,2364.3489180995853,2370.482316142582,2376.6178810336637,2382.75560808775,2388.895492639976,2395.0375300455635,2401.181715679689,2407.3280449373583,2413.476513233276,2419.6271160017222,2425.779848696426,2431.9347067904428,2438.0916857760285,2444.2507811645205,2450.4119884862157,2456.5753032902503,2462.7407211444815,2468.90823763537,2475.0778483678614,2481.2495489652724,2487.4233350691743,2493.59920233928,2499.7771464533307,2505.9571631069834,2512.1392480137,2518.323396904638,2524.5096055285385,2530.697869651621,2536.8881850574744,2543.080547546949,2549.274952938054,2555.4713970658486,2561.669875782341,2567.8703849563835,2574.0729204735712,2580.27747823614,2586.4840541628646,2592.6926441889614,2598.903244265986,2605.1158503617376,2611.3304584601597,2617.5470645612445,2623.7656646809364,2629.986254851036,2636.2088311191073,2642.433389548383,2648.65992621767,2654.8884372212615,2661.11891866884,2667.3513666853905,2673.585777411109,2679.8221470013127,2686.0604716263524,2692.300747471523,2698.5429707369785,2704.7871376376424,2711.033244403124,2717.2812872776326,2723.531262519892,2729.783166403058,2736.036995214634,2742.292745256387,2748.55041284427,2754.809994308335,2761.0714859926557,2767.3348842552473,2773.600185467985,2779.8673860165263,2786.1364823002327,2792.407470732091,2798.680347738637,2804.9551097598787,2811.23175324922,2817.510274673386,2823.790670512346,2830.072937259242,2836.3570714203124,2842.6430695148215,2848.930928074983,2855.220643645892,2861.51221278545,2867.805632064297,2874.1008980657366,2880.3980073856706,2886.6969566325265,2892.9977424271897,2899.3003614029344,2905.6048102053564,2911.9110854923047,2918.2191839338143,2924.529102212041,2930.8408370211937,2937.1543850674707,2943.469743068993,2949.7869077557402,2956.1058758694867,2962.4266441637374,2968.7492094036647,2975.073568366046,2981.399717839201,2987.72765462293,2994.0573755284527,3000.3888773783465,3006.722157006486,3013.0572112579844,3019.3940369891307,3025.732631067334,3032.0729903710617,3038.4151117897827,3044.758992223909,3051.1046285847374,3057.452017794393,3063.801156785773,3070.152042502488,3076.5046718988074,3082.8590419396046,3089.2151496003003,3095.5729918668085,3101.9325657354807,3108.293868213054,3114.656896316594,3121.0216470734463,3127.3881175211777,3133.756304707528,3140.1262056903565,3146.497817537588,3152.8711373271653,3159.2461621469934,3165.622889094892,3172.0013152785436,3178.3814378154434,3184.7632538328494,3191.146760467733,3197.5319548667308,3203.9188341860936,3210.3073955916393,3216.6976362587047,3223.0895533720973,3229.483144126048,3235.8784057241637,3242.27533537938,3248.673930313915,3255.074187759224,3261.476104955951,3267.8796791538857,3274.2849076119164,3280.6917875979857,3287.100316389045,3293.5104912710112,3299.922309538721,3306.3357684958883,3312.7508654550597,3319.167597737572,3325.585962673508,3332.005957601655,3338.4275798694616,3344.850826832995,3351.2756958569007,3357.7021843143584,3364.130289587043,3370.5600090650823,3376.9913401470158,3383.424280239755,3389.8588267585424,3396.294977126912,3402.7327287766484,3409.1720791477487,3415.6130256883816,3422.0555658548496,3428.49969711155,3434.9454169309356,3441.392722793477,3447.841612187624,3454.292082609768,3460.744131564205,3467.1977565630978,3473.652955126438,3480.1097247820103,3486.568063065355,3493.0279675197326,3499.4894356960863,3505.952465153007,3512.417053456697,3518.8831981809344,3525.350896907039,3531.8201472238347,3538.290946727617,3544.763293022118,3551.23718371847,3557.7126164351744,3564.189588798064,3570.668098440273,3577.1481430021995,3583.629720131476,3590.1128274829334,3596.5974627185687,3603.083623507513,3609.571307525998,3616.060512457323,3622.5512359918257,3629.043475826846,3635.537229666698,3642.0324952226347,3648.5292702128204,3655.027552362297,3661.5273394029527,3668.028629073493,3674.531419119409,3681.0357072929455,3687.5414913530735,3694.0487690654586,3700.5575382024304,3707.0677965429536,3713.5795418725984,3720.092771983511,3726.607484674383,3733.123677750426,3739.641349023338,3746.1604963112786,3752.681117438837,3759.203210237007,3765.7267725431566,3772.251802201,3778.778297060571,3785.3062549781935,3791.8356738164557,3798.3665514441814,3804.898885736404,3811.4326745743374,3817.967915845351,3824.5046074429424,3831.04274726671,3837.582333222328,3844.123363221518,3850.6658351820247,3857.2097470275894,3863.755096687924,3870.3018820986845,3876.8501012014467,3883.3997519436807,3889.950832278724,3896.503340165759,3903.0572735697847,3909.6126304615955,3916.1694088177537,3922.727606620566,3929.287221858059,3935.8482525239556,3942.410696617649,3948.9745521441814,3955.539817114217,3962.10648954402,3968.6745674554318,3975.244048875846,3981.8149318381857,3988.38721438088,3994.9608945478403,4001.53597038844,4008.1124399574883,4014.690301315209,4021.2695525272193,4027.850191664504,4034.432216803397,4041.015626025556,4047.6004174179416,4054.1865890727963,4060.7741390876213,4067.363065565155,4073.9533666133516,4080.54504034536,4087.1380848795025,4093.7324983392523,4100.328278853213,4106.9254245551,4113.523933583715,4120.123804082928,4126.725034201657,4133.327622093846,4139.931565918447,4146.536863839395,4153.143514025593,4159.751514650889,4166.360863894056,4172.971559938774,4179.583600973607,4186.196985191987,4192.811710792191,4199.427775977324,4206.045178955298,4212.663917938815,4219.283991145345,4225.905396797109,4232.528133121058,4239.152198348858,4245.777590716866,4252.404308466115,4259.032349842295,4265.661713095732,4272.292396481374,4278.92439825877,4285.557716692051,4292.192350049912,4298.8282966055995,4305.465554636884,4312.104122426051,4318.743998259877,4325.385180429617,4332.027667230985,4338.671456964133,4345.3165479336385,4351.962938448486,4358.610626822049,4365.259611372074,4371.909890420661,4378.561462294251,4385.2143253236045,4391.868477843787,4398.523918194155,4405.1806447183335,4411.838655764204,4418.497949683888,4425.158524833728,4431.820379574273,4438.483512270263,4445.147921290614,4451.813605008397,4458.480561800826,4465.148790049243,4471.818288139101,4478.4890544599475,4485.161087405409,4491.834385373176,4498.5089467649905,4505.184769986626,4511.861853447873,4518.5401955625275,4525.219794748372,4531.9006494271625,4538.582758024612,4545.266118970379,4551.950730698047,4558.636591645115,4565.323700252981,4572.012054966928,4578.701654236107,4585.392496513526,4592.0845802560325,4598.777903924302,4605.472465982823,4612.168264899882,4618.865299147548,4625.563567201663,4632.263067541825,4638.963798651373,4645.665759017375,4652.368947130616,4659.07336148558,4665.77900058044,4672.485862917043,4679.1939470008965,4685.903251341155,4692.613774450608,4699.325514845664,4706.03847104634,4712.7526415762495,4719.468024962584,4726.184619736106,4732.9024244311295,4739.621437585514,4746.34165774065,4753.0630834414405,4759.785713236296,4766.509545677117,4773.2345793192835,4779.9608127216425,4786.688244446494,4793.416873059578,4800.146697130068,4806.87771523055,4813.609925937018,4820.343327828855,4827.077919488828,4833.813699503071,4840.550666461073,4847.288818955669,4854.028155583026,4860.768674942632,4867.510375637284,4874.253256273076,4880.997315459387,4887.742551808871,4894.488963937444,4901.236550464274,4907.985310011765,4914.735241205553,4921.4863426744905,4928.238613050632,4934.99205096923,4941.746655068718,4948.502423990702,4955.259356379949,4962.017450884377,4968.77670615504,4975.537120846124,4982.298693614928,4989.06142312186,4995.825308030422,5002.590347007203,5009.356538721863,5016.123881847128,5022.892375058777,5029.66201703563,5036.432806459539,5043.2047420153785,5049.977822391034,5056.752046277392,5063.527412368328,5070.303919360701,5077.081565954336,5083.860350852021,5090.640272759493,5097.421330385429,5104.203522441436,5110.98684764204,5117.771304704677,5124.556892349685,5131.34360930029,5138.131454282599,5144.920426025592,5151.710523261106,5158.5017447238315,5165.294089151303,5172.0875552838825,5178.8821418647585,5185.677847639932,5192.474671358207,5199.272611771182,5206.07166763324,5212.871837701543,5219.673120736014,5226.475515499339,5233.279020756948,5240.08363527701,5246.889357830427,5253.696187190819,5260.504122134519,5267.313161440562,5274.123303890678,5280.934548269279,5287.746893363456,5294.560337962967,5301.374880860228,5308.190520850302,5315.007256730897,5321.825087302352,5328.644011367627,5335.4640277323015,5342.285135204558,5349.107332595178,5355.930618717534,5362.754992387578,5369.580452423833,5376.406997647389,5383.234626881892,5390.063338953533,5396.893132691046,5403.724006925692,5410.555960491258,5417.388992224044,5424.223100962858,5431.058285549005,5437.894544826282,5444.7318776409675,5451.570282841815,5458.409759280044,5465.250305809333,5472.091921285811,5478.9346045680495,5485.778354517056,5492.623169996265,5499.469049871529,5506.315993011114,5513.1639982856905,5520.013064568324,5526.86319073447,5533.7143756619635,5540.566618231015,5547.419917324201,5554.274271826456,5561.129680625067,5567.986142609661,5574.843656672207,5581.702221706998,5588.561836610652,5595.4225002821,5602.284211622581,5609.146969535633,5616.010772927086,5622.8756207050565,5629.74151177994,5636.608445064402,5643.4764194733725,5650.345433924038,5657.215487335836,5664.086578630447,5670.958706731785,5677.831870565998,5684.706069061451,5691.581301148727,5698.457565760618,5705.334861832116,5712.213188300408,5719.0925441048685,5725.972928187054,5732.8543394906965,5739.736776961694,5746.620239548107,5753.50472620015,5760.390235870184,5767.276767512715,5774.16432008438,5781.052892543946,5787.9424838523,5794.833092972447,5801.724718869499,5808.6173605106715,5815.5110168652745,5822.405686904708,5829.301369602456,5836.198063934079,5843.095768877208,5849.994483411538,5856.894206518822,5863.794937182867,5870.696674389524,5877.599417126682,5884.503164384267,5891.407915154228,5898.31366843054,5905.220423209189,5912.128178488171};
		//Fast n choose k calculation
		// From: https://stackoverflow.com/questions/37679987/efficient-computation-of-n-choose-k-in-node-js @le_m
		// Notes: n and k must be within the range 0 -> 1000 (they definitely will for this project)
		return Math.round(Math.exp(logf[n] - logf[n-k] - logf[k]));
		//Code used to precompute logf array
		// var size = 1000, logf = new Array(size);
		// logf[0] = 0;
		// for (var i = 1; i <= size; ++i) logf[i] = logf[i-1] + Math.log(i);
	}
	
//	private static byte[] data;
//	private static int[] middleman;
//	private static byte[] data = new byte[1000*1000]; //Start it oversized
	public static byte[] loadGrayscale(BufferedImage targetImage) {
		//250 strings, 300 res, AddisonV3, 500 computes = 1:09
		byte[] data = new byte[targetImage.getWidth()*targetImage.getHeight()];
		int index = 0;
		for (int y = 0;y < targetImage.getHeight();y++) {
			for (int x = 0;x < targetImage.getWidth();x++,index++) {
				data[index] = (byte) new Color(targetImage.getRGB(x, y)).getRed();
			}
		}
		return data;
		
		//250 strings, 300 res, AddisonV3, 500 computes = 1:22
//		data = new byte[targetImage.getWidth()*targetImage.getHeight()];
//		middleman = targetImage.getRGB(0, 0, targetImage.getWidth(), targetImage.getHeight(), null, 0, targetImage.getWidth());
//		for (int i = 0;i < middleman.length;i++) {
//			data[i] = (byte) ((middleman[i] >> 16) & 0xFF);
//		}
//		return data;
		
		//250 strings, 300 res, AddisonV3, 500 computes = 1:09
//		int index = 0;
//		for (int y = 0;y < targetImage.getHeight();y++) {
//			for (int x = 0;x < targetImage.getWidth();x++,index++) {
//				data[index] = (byte) new Color(targetImage.getRGB(x, y)).getRed();
//			}
//		}
//		return Arrays.copyOfRange(data, 0, index);
	}
	private static int loadGrayscale_iterationsUntilRecache = 25;
	private static Map<String, byte[]> loadGrayscale_CacheMap = new HashMap<String, byte[]>();
	public static byte[] loadGrayscaleWithCache(BufferedImage targetImage, String hashID, int iterationNum) {
		//250 strings, 300 res, AddisonV3, 500 computes = //TODO
		//Check if need to recache or initialize cache, and do it if necessary
		if (iterationNum % loadGrayscale_iterationsUntilRecache == 0 || !loadGrayscale_CacheMap.containsKey(hashID)) {
			byte[] data = new byte[targetImage.getWidth()*targetImage.getHeight()];
			int index = 0;
			for (int y = 0;y < targetImage.getHeight();y++) {
				for (int x = 0;x < targetImage.getWidth();x++,index++) {
					data[index] = (byte) new Color(targetImage.getRGB(x, y)).getRed();
				}
			}
			loadGrayscale_CacheMap.put(hashID, data);
			return data;
		} else {
			return loadGrayscale_CacheMap.get(hashID);
		}
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
	
	public static String stripExtension(String fileName) {
		int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1) {
            return fileName.substring(0, lastDotIndex);
        } else {
            return fileName;
        }
	}
	
}


/*We will have the following images
 * - Target (1000x1000): This is a resolution-adjusted offscreen static image we try to recreate
 * - Computed (1000x1000): This is the offscreen image where computational additions happen
 * - Offscreen (1920x1920): This is the offscreen full res drawing with nails and strings
 */
/*We will have the following panels
 * - Input (WhateverxWhatever): This is the panel for input and config stuff
 * - Main (VisiblexVisible): This is the central panel w/ downscaled "Offscreen"
 * - Preview (WhateverxWhatever): This is the right side panel
 * 		- Target (halfheightxhalfheight): This will be a downscaled copy of "Target" image
 * 		- Computed (halfheightxhalfheight): This will be a downscaled copy of "Computed" image
 */