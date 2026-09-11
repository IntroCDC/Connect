package br.com.introcdc.connect.server.gui;
/*
 * Written by IntroCDC, Bruno Coêlho at 11/09/2026 - 00:48
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

public class InteractiveImagePanel extends JPanel {

    private BufferedImage image;
    private double zoom = 1.0;
    private int offsetX = 0;
    private int offsetY = 0;
    private Point dragStart = null;
    private boolean screen;

    public InteractiveImagePanel(boolean screen) {
        this.screen = screen;

        if (screen) {
            // Setup mouse listeners for panning and zooming
            addMouseWheelListener(e -> {
                if (e.isControlDown()) {
                    double oldZoom = zoom;
                    if (e.getWheelRotation() < 0) {
                        zoom *= 1.1; // zoom in
                    } else {
                        zoom /= 1.1; // zoom out
                    }
                    if (zoom < 0.1) zoom = 0.1;
                    if (zoom > 10.0) zoom = 10.0;

                    repaint();
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isMiddleMouseButton(e)) {
                        dragStart = e.getPoint();
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (SwingUtilities.isMiddleMouseButton(e) && dragStart != null) {
                        offsetX += e.getX() - dragStart.x;
                        offsetY += e.getY() - dragStart.y;
                        dragStart = e.getPoint();
                        repaint();
                    }
                }
            });
        }
    }

    public void setImage(BufferedImage img) {
        this.image = img;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null) return;

        Graphics2D g2 = (Graphics2D) g;

        double scaleX = (double) getWidth() / image.getWidth();
        double scaleY = (double) getHeight() / image.getHeight();
        double fitScale = Math.min(scaleX, scaleY);

        double currentScale = fitScale * zoom;
        int drawWidth = (int) (image.getWidth() * currentScale);
        int drawHeight = (int) (image.getHeight() * currentScale);

        int drawX = (getWidth() - drawWidth) / 2 + offsetX;
        int drawY = (getHeight() - drawHeight) / 2 + offsetY;

        g2.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
    }

    public Point getOriginalPoint(int mouseX, int mouseY) {
        if (image == null) return new Point(0, 0);
        double scaleX = (double) getWidth() / image.getWidth();
        double scaleY = (double) getHeight() / image.getHeight();
        double fitScale = Math.min(scaleX, scaleY);
        double currentScale = fitScale * zoom;

        int drawWidth = (int) (image.getWidth() * currentScale);
        int drawHeight = (int) (image.getHeight() * currentScale);

        int drawX = (getWidth() - drawWidth) / 2 + offsetX;
        int drawY = (getHeight() - drawHeight) / 2 + offsetY;

        int origX = (int) ((mouseX - drawX) / currentScale);
        int origY = (int) ((mouseY - drawY) / currentScale);
        return new Point(origX, origY);
    }

}
