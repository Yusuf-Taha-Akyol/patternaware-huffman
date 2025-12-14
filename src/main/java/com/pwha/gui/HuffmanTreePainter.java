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

public class HuffmanTreePainter extends JPanel {
    private final HNode globalRoot;
    private HNode currentRoot;
    private final Map<HNode, Point> nodePositions = new HashMap<>();

    // Görsel Ayarlar
    private final int NODE_WIDTH = 60;
    private final int NODE_HEIGHT = 50;
    private final int VERTICAL_GAP = 90;
    private final int HORIZONTAL_GAP = 30;

    // Zoom Ayarları
    private double scaleFactor = 1.0;
    public final double MIN_ZOOM = 0.2; // Public yaptık ki App.java erişebilsin
    public final double MAX_ZOOM = 3.0;

    private int leafCounter = 0;
    private int maxDepth = 0;

    private Runnable onSubTreeSelected;

    public HuffmanTreePainter(HNode root) {
        this.globalRoot = root;
        this.currentRoot = root;
        this.setBackground(Color.WHITE);

        recalculateLayout();

        // Tıklama Olayı (Aynı kalıyor)
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int logicalX = (int) (e.getX() / scaleFactor);
                int logicalY = (int) (e.getY() / scaleFactor);
                handleNodeClick(new Point(logicalX, logicalY));
            }
        });

        // MouseWheelListener KALDIRILDI (Artık mouse ile zoom yok)
    }

    // YENİ EKLENEN METOT: Dışarıdan zoom ayarı yapmak için
    public void setScaleFactor(double newScale) {
        if (newScale < MIN_ZOOM) newScale = MIN_ZOOM;
        if (newScale > MAX_ZOOM) newScale = MAX_ZOOM;

        this.scaleFactor = newScale;

        recalculateLayout(); // Panelin boyutunu güncelle
        revalidate();        // ScrollPane'i uyar
        repaint();           // Yeniden çiz
    }

    private void recalculateLayout() {
        if (currentRoot == null) return;

        nodePositions.clear();
        leafCounter = 0;
        maxDepth = 0;

        calculateCoordinates(currentRoot, 0);

        int totalWidth = (int) (((leafCounter * (NODE_WIDTH + HORIZONTAL_GAP)) + 200) * scaleFactor);
        int totalHeight = (int) ((((maxDepth + 2) * VERTICAL_GAP) + 100) * scaleFactor);

        this.setPreferredSize(new Dimension(totalWidth, totalHeight));
    }

    private int calculateCoordinates(HNode node, int depth) {
        if (node == null) return 0;
        if (depth > maxDepth) maxDepth = depth;

        if (node instanceof InternalNode) {
            InternalNode internal = (InternalNode) node;
            int leftX = calculateCoordinates(internal.getLeft(), depth + 1);
            int rightX = calculateCoordinates(internal.getRight(), depth + 1);
            int myX = (leftX + rightX) / 2;
            nodePositions.put(node, new Point(myX, depth * VERTICAL_GAP + 50));
            return myX;
        } else {
            int myX = (leafCounter * (NODE_WIDTH + HORIZONTAL_GAP)) + 50;
            leafCounter++;
            nodePositions.put(node, new Point(myX, depth * VERTICAL_GAP + 50));
            return myX;
        }
    }

    private void handleNodeClick(Point clickPoint) {
        for (Map.Entry<HNode, Point> entry : nodePositions.entrySet()) {
            HNode node = entry.getKey();
            Point p = entry.getValue();

            int drawX = p.x - NODE_WIDTH / 2;

            if (clickPoint.x >= drawX && clickPoint.x <= drawX + NODE_WIDTH &&
                    clickPoint.y >= p.y && clickPoint.y <= p.y + NODE_HEIGHT) {

                if (node instanceof ContextLeaf) {
                    ContextLeaf leaf = (ContextLeaf) node;
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

    public void resetToGlobal() {
        this.currentRoot = globalRoot;
        recalculateLayout();
        repaint();
    }

    public boolean isShowingSubTree() {
        return currentRoot != globalRoot;
    }

    public void setOnSubTreeSelected(Runnable listener) {
        this.onSubTreeSelected = listener;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentRoot != null) {
            Graphics2D g2 = (Graphics2D) g;

            g2.scale(scaleFactor, scaleFactor);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2.setColor(new Color(60, 60, 60));
            String zoomText = String.format(" (Zoom: %.1fx)", scaleFactor);
            if (currentRoot == globalRoot) {
                g2.drawString("MAIN TREE" + zoomText, 20, 30);
            } else {
                g2.drawString("SUB-PATTERN TREE" + zoomText, 20, 30);
            }

            drawTree(g2, currentRoot, "");
        }
    }

    private void drawTree(Graphics2D g, HNode node, String codeSoFar) {
        if (node == null) return;

        Point p = nodePositions.get(node);
        int x = p.x - NODE_WIDTH / 2;
        int y = p.y;

        if (node instanceof InternalNode) {
            InternalNode internal = (InternalNode) node;
            g.setStroke(new BasicStroke(2));
            g.setColor(Color.GRAY);

            if (internal.getLeft() != null) {
                Point pLeft = nodePositions.get(internal.getLeft());
                g.drawLine(p.x, p.y + NODE_HEIGHT, pLeft.x, pLeft.y);
                drawTree(g, internal.getLeft(), codeSoFar + "0");
                g.setColor(Color.RED);
                g.drawString("0", (p.x + pLeft.x)/2 - 10, (p.y + NODE_HEIGHT + pLeft.y)/2);
                g.setColor(Color.GRAY);
            }
            if (internal.getRight() != null) {
                Point pRight = nodePositions.get(internal.getRight());
                g.drawLine(p.x, p.y + NODE_HEIGHT, pRight.x, pRight.y);
                drawTree(g, internal.getRight(), codeSoFar + "1");
                g.setColor(Color.BLUE);
                g.drawString("1", (p.x + pRight.x)/2 + 5, (p.y + NODE_HEIGHT + pRight.y)/2);
                g.setColor(Color.GRAY);
            }
        }

        Color nodeColor = getNodeColor(node);
        g.setColor(nodeColor);
        g.fillRoundRect(x, y, NODE_WIDTH, NODE_HEIGHT, 15, 15);

        g.setColor(Color.DARK_GRAY);
        g.setStroke(new BasicStroke(node instanceof ContextLeaf ? 2 : 1));
        g.drawRoundRect(x, y, NODE_WIDTH, NODE_HEIGHT, 15, 15);

        String text = getNodeText(node);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Consolas", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (NODE_WIDTH - fm.stringWidth(text)) / 2;
        int textY = y + 20;
        g.drawString(text, textX, textY);

        if (node.isLeaf()) {
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            g.setColor(new Color(0, 100, 0));
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

    private Color getNodeColor(HNode node) {
        if (node instanceof ContextLeaf) return new Color(135, 206, 250);
        if (node instanceof SimpleLeaf) return new Color(144, 238, 144);
        return new Color(245, 245, 245);
    }

    private String getNodeText(HNode node) {
        if (node instanceof ContextLeaf) {
            byte b = ((ContextLeaf) node).getData();
            if (b == 32) return "SP";
            if (b == 10) return "\\n";
            if (b == 13) return "\\r";
            if (b == 9) return "\\t";
            return Character.isISOControl(b) ? "?" : String.valueOf((char) b);
        }
        if (node instanceof SimpleLeaf) {
            SimpleLeaf sl = (SimpleLeaf) node;
            String val = sl.convertString();
            if (val.length() > 5) return val.substring(0, 4) + ".";
            return val;
        }
        return "";
    }
}