package com.pwha.gui;

import com.pwha.model.node.ContextLeaf;
import com.pwha.model.node.HNode;
import com.pwha.model.node.InternalNode;
import com.pwha.model.node.SimpleLeaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * A Custom Swing Component to Visualize the Huffman Tree.
 * <p>
 * This class draws the hierarchical structure of the compression algorithm.
 * It supports:
 * 1. **Visualization:** Renders nodes, connections (edges), and binary codes (0/1).
 * 2. **Navigation:** Allows clicking on a 'Context' node to dive into its specific 'Pattern Sub-Tree'.
 * 3. **Zooming:** Supports scaling the view to inspect large trees.
 */
public class HuffmanTreePainter extends JPanel {

    // The root of the entire Super-Tree (Context Tree).
    private final HNode globalRoot;

    // The root currently being displayed (can be the Global Root or a Sub-Tree Root).
    private HNode currentRoot;

    // Maps each node to its calculated (x, y) coordinates on the screen.
    private final Map<HNode, Point> nodePositions = new HashMap<>();

    // Visual Settings (Dimensions and Spacing)
    private final int NODE_WIDTH = 60;
    private final int NODE_HEIGHT = 50;
    private final int VERTICAL_GAP = 90;
    private final int HORIZONTAL_GAP = 30;

    // Zoom Settings
    private double scaleFactor = 1.0;
    public final double MIN_ZOOM = 0.2; // Public so App.java can access limits
    public final double MAX_ZOOM = 3.0;

    // Layout Calculation Helpers
    private int leafCounter = 0; // Tracks X-position for leaf nodes
    private int maxDepth = 0;    // Tracks tree height to adjust panel size

    // Callback to notify the parent frame when a sub-tree is selected.
    private Runnable onSubTreeSelected;

    public HuffmanTreePainter(HNode root) {
        this.globalRoot = root;
        this.currentRoot = root;
        this.setBackground(Color.WHITE);

        // Initial layout calculation
        recalculateLayout();

        // Mouse Listener for handling node clicks (Navigation)
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Adjust click coordinates based on the current zoom level
                int logicalX = (int) (e.getX() / scaleFactor);
                int logicalY = (int) (e.getY() / scaleFactor);
                handleNodeClick(new Point(logicalX, logicalY));
            }
        });

        // MouseWheelListener REMOVED (Zoom is handled externally via Spinner in App.java)
    }

    /**
     * Sets the zoom level from the external controller (App.java).
     *
     * @param newScale The new scale factor (e.g., 1.5 for 150% zoom).
     */
    // NEW ADDED METHOD: For external zoom control.
    public void setScaleFactor(double newScale) {
        // Clamp values to safe limits
        if (newScale < MIN_ZOOM) newScale = MIN_ZOOM;
        if (newScale > MAX_ZOOM) newScale = MAX_ZOOM;

        this.scaleFactor = newScale;

        recalculateLayout(); // Update panel size based on new scale
        revalidate();        // Notify ScrollPane to update scrollbars
        repaint();           // Redraw the tree
    }

    /**
     * Recalculates the positions of all nodes and updates the panel size.
     * Called whenever the tree changes or zoom level updates.
     */
    private void recalculateLayout() {
        if (currentRoot == null) return;

        nodePositions.clear();
        leafCounter = 0;
        maxDepth = 0;

        // Recursive layout calculation starting from root
        calculateCoordinates(currentRoot, 0);

        // Calculate total canvas size required
        int totalWidth = (int) (((leafCounter * (NODE_WIDTH + HORIZONTAL_GAP)) + 200) * scaleFactor);
        int totalHeight = (int) ((((maxDepth + 2) * VERTICAL_GAP) + 100) * scaleFactor);

        this.setPreferredSize(new Dimension(totalWidth, totalHeight));
    }

    /**
     * Recursive algorithm to calculate (X, Y) coordinates for each node.
     * <p>
     * Logic:
     * - **Leaves:** Placed sequentially from left to right.
     * - **Internal Nodes:** Placed exactly in the middle of their children.
     *
     * @param node  The current node.
     * @param depth The current depth in the tree (determines Y position).
     * @return The X coordinate of the current node.
     */
    private int calculateCoordinates(HNode node, int depth) {
        if (node == null) return 0;
        if (depth > maxDepth) maxDepth = depth;

        if (node instanceof InternalNode) {
            InternalNode internal = (InternalNode) node;

            // Process children first to determine their positions
            int leftX = calculateCoordinates(internal.getLeft(), depth + 1);
            int rightX = calculateCoordinates(internal.getRight(), depth + 1);

            // Parent is centered above children
            int myX = (leftX + rightX) / 2;
            nodePositions.put(node, new Point(myX, depth * VERTICAL_GAP + 50));
            return myX;
        } else {
            // Leaf nodes are placed side-by-side
            int myX = (leafCounter * (NODE_WIDTH + HORIZONTAL_GAP)) + 50;
            leafCounter++;
            nodePositions.put(node, new Point(myX, depth * VERTICAL_GAP + 50));
            return myX;
        }
    }

    /**
     * Handles mouse clicks to navigate the tree.
     * If a 'ContextLeaf' is clicked, it switches the view to its 'Sub-Tree'.
     */
    private void handleNodeClick(Point clickPoint) {
        for (Map.Entry<HNode, Point> entry : nodePositions.entrySet()) {
            HNode node = entry.getKey();
            Point p = entry.getValue();

            int drawX = p.x - NODE_WIDTH / 2;

            // Check if click is inside the node bounds
            if (clickPoint.x >= drawX && clickPoint.x <= drawX + NODE_WIDTH &&
                    clickPoint.y >= p.y && clickPoint.y <= p.y + NODE_HEIGHT) {

                if (node instanceof ContextLeaf) {
                    ContextLeaf leaf = (ContextLeaf) node;

                    // Navigate to Sub-Tree if it exists
                    if (leaf.getSubTreeRoot() != null) {
                        currentRoot = leaf.getSubTreeRoot();
                        recalculateLayout();
                        if (onSubTreeSelected != null) onSubTreeSelected.run();
                        repaint();
                    } else {
                        String info = getNodeText(node);
                        JOptionPane.showMessageDialog(this, "Context '" + info + "' has no sub-patterns.");
                    }
                }
                break;
            }
        }
    }

    /**
     * Resets the view back to the main Context Tree (Super-Tree).
     */
    public void resetToGlobal() {
        this.currentRoot = globalRoot;
        recalculateLayout();
        repaint();
    }

    /**
     * Checks if we are currently viewing a sub-tree.
     */
    public boolean isShowingSubTree() {
        return currentRoot != globalRoot;
    }

    public void setOnSubTreeSelected(Runnable listener) {
        this.onSubTreeSelected = listener;
    }

    /**
     * Standard Swing painting method.
     * Handles scaling (zoom) and triggers the tree drawing process.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentRoot != null) {
            Graphics2D g2 = (Graphics2D) g;

            // Apply scaling transformation for zoom
            g2.scale(scaleFactor, scaleFactor);
            // Enable Anti-Aliasing for smoother text and lines
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw Header/Title
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2.setColor(new Color(60, 60, 60));
            String zoomText = String.format(" (Zoom: %.1fx)", scaleFactor);
            if (currentRoot == globalRoot) {
                g2.drawString("MAIN TREE" + zoomText, 20, 30);
            } else {
                g2.drawString("SUB-PATTERN TREE" + zoomText, 20, 30);
            }

            // Start recursive drawing
            drawTree(g2, currentRoot, "");
        }
    }

    /**
     * Recursively draws nodes and connecting lines.
     *
     * @param g         Graphics context.
     * @param node      Current node to draw.
     * @param codeSoFar The Huffman code generated so far (e.g., "011").
     */
    private void drawTree(Graphics2D g, HNode node, String codeSoFar) {
        if (node == null) return;

        Point p = nodePositions.get(node);
        int x = p.x - NODE_WIDTH / 2;
        int y = p.y;

        // Draw connections (branches) for Internal Nodes
        if (node instanceof InternalNode) {
            InternalNode internal = (InternalNode) node;
            g.setStroke(new BasicStroke(2));
            g.setColor(Color.GRAY);

            if (internal.getLeft() != null) {
                Point pLeft = nodePositions.get(internal.getLeft());
                g.drawLine(p.x, p.y + NODE_HEIGHT, pLeft.x, pLeft.y);
                drawTree(g, internal.getLeft(), codeSoFar + "0");

                // Draw '0' label for left branch
                g.setColor(Color.RED);
                g.drawString("0", (p.x + pLeft.x)/2 - 10, (p.y + NODE_HEIGHT + pLeft.y)/2);
                g.setColor(Color.GRAY);
            }
            if (internal.getRight() != null) {
                Point pRight = nodePositions.get(internal.getRight());
                g.drawLine(p.x, p.y + NODE_HEIGHT, pRight.x, pRight.y);
                drawTree(g, internal.getRight(), codeSoFar + "1");

                // Draw '1' label for right branch
                g.setColor(Color.BLUE);
                g.drawString("1", (p.x + pRight.x)/2 + 5, (p.y + NODE_HEIGHT + pRight.y)/2);
                g.setColor(Color.GRAY);
            }
        }

        // Draw Node Background (Rectangle)
        Color nodeColor = getNodeColor(node);
        g.setColor(nodeColor);
        g.fillRoundRect(x, y, NODE_WIDTH, NODE_HEIGHT, 15, 15);

        // Draw Node Border
        g.setColor(Color.DARK_GRAY);
        g.setStroke(new BasicStroke(node instanceof ContextLeaf ? 2 : 1)); // Thicker border for clickable nodes
        g.drawRoundRect(x, y, NODE_WIDTH, NODE_HEIGHT, 15, 15);

        // Draw Node Text (Character or Pattern)
        String text = getNodeText(node);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Consolas", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (NODE_WIDTH - fm.stringWidth(text)) / 2;
        int textY = y + 20;
        g.drawString(text, textX, textY);

        // Draw Leaf Details (Code and Frequency)
        if (node.isLeaf()) {
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            g.setColor(new Color(0, 100, 0));

            // Format code string to fit
            String codeText = codeSoFar.isEmpty() ? "Root" : codeSoFar;
            if (codeText.length() > 8) codeText = ".." + codeText.substring(codeText.length()-6);

            int codeX = x + (NODE_WIDTH - g.getFontMetrics().stringWidth(codeText)) / 2;
            g.drawString(codeText, codeX, y + 35);

            g.setColor(Color.GRAY);
            String freqText = String.valueOf(node.getFrequency());
            int freqX = x + (NODE_WIDTH - g.getFontMetrics().stringWidth(freqText)) / 2;
            g.drawString(freqText, freqX, y + 48);
        }
    }

    /**
     * Determines the color of the node based on its type.
     * Blue: Context (Clickable), Green: Pattern, Gray: Internal.
     */
    private Color getNodeColor(HNode node) {
        if (node instanceof ContextLeaf) return new Color(135, 206, 250); // Light Blue
        if (node instanceof SimpleLeaf) return new Color(144, 238, 144);  // Light Green
        return new Color(245, 245, 245); // Light Gray
    }

    /**
     * Extracts readable text from a node.
     * Handles special characters like Newline, Tab, Space.
     */
    private String getNodeText(HNode node) {
        if (node instanceof ContextLeaf) {
            byte b = ((ContextLeaf) node).getData();
            if (b == 32) return "SP"; // Space
            if (b == 10) return "\\n"; // Newline
            if (b == 13) return "\\r"; // Carriage Return
            if (b == 9) return "\\t";  // Tab
            return Character.isISOControl(b) ? "?" : String.valueOf((char) b);
        }
        if (node instanceof SimpleLeaf) {
            SimpleLeaf sl = (SimpleLeaf) node;
            String val = sl.convertString();
            if (val.length() > 5) return val.substring(0, 4) + "."; // Truncate long patterns
            return val;
        }
        return "";
    }
}