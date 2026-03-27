package com.f3cinema.app.ui.dashboard;

import com.f3cinema.app.entity.Movie;
import com.f3cinema.app.entity.enums.MovieStatus;
import com.formdev.flatlaf.FlatClientProperties;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.f3cinema.app.entity.Genre;

/**
 * MovieCard — Designer-Grade Component with ASYNC loading.
 * Fixes: UI Lag (EDT Blocking), Broken Image Handling, Loading States.
 */
public class MovieCard extends JPanel {

    private final Movie movie;
    private final Runnable onEdit;
    private final Runnable onDelete;

    private boolean isHovered = false;
    private double hoverAnim = 0.0;
    private Timer animTimer;
    
    // Thread-safe Cache
    private static final Map<String, Image> imageCache = new ConcurrentHashMap<>();
    private boolean isLoading = false;

    // Design Tokens
    private static final Color C_CARD_BG        = new Color(15, 23, 42, 220);
    private static final Color C_ACCENT         = Color.decode("#6366F1");
    private static final Color C_TEXT_MAIN      = Color.decode("#F8FAFC");
    private static final Color C_TEXT_SUB       = Color.decode("#94A3B8");
    private static final Color C_GLOW           = new Color(99, 102, 241, 100);

    public MovieCard(Movie movie, Runnable onEdit, Runnable onDelete) {
        this.movie = movie;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
        initUI();
        triggerAsyncLoad();
    }

    private void initUI() {
        setPreferredSize(new Dimension(240, 380));
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        animTimer = new Timer(16, e -> {
            double target = isHovered ? 1.0 : 0.0;
            hoverAnim += (target - hoverAnim) * 0.15;
            if (Math.abs(target - hoverAnim) < 0.01) {
                hoverAnim = target;
                animTimer.stop();
            }
            repaint();
            if (getParent() != null) {
                getParent().repaint(getX() - 20, getY() - 20, getWidth() + 40, getHeight() + 40);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { isHovered = true; if (!animTimer.isRunning()) animTimer.start(); }
            @Override public void mouseExited(MouseEvent e) { isHovered = false; if (!animTimer.isRunning()) animTimer.start(); }
            @Override public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) showContextMenu(e);
                else onEdit.run();
            }
        });
    }

    private void triggerAsyncLoad() {
        String url = movie.getPosterUrl();
        if (url == null || url.isBlank() || imageCache.containsKey(url)) return;

        isLoading = true;
        SwingWorker<Image, Void> loader = new SwingWorker<>() {
            @Override
            protected Image doInBackground() {
                try {
                    Image rawImg = null;
                    if (url.startsWith("http")) {
                        try (InputStream in = URI.create(url).toURL().openStream()) {
                            rawImg = ImageIO.read(in);
                        }
                    } else {
                        rawImg = ImageIO.read(new File(url));
                    }
                    
                    if (rawImg != null) {
                        BufferedImage resized = new BufferedImage(240, 320, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g = resized.createGraphics();
                        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        g.drawImage(rawImg, 0, 0, 240, 320, null);
                        g.dispose();
                        return resized;
                    }
                } catch (Exception e) {
                    System.err.println("Load failed: " + url);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    Image result = get();
                    if (result != null) imageCache.put(url, result);
                } catch (Exception ignored) {}
                isLoading = false;
                repaint();
            }
        };
        loader.execute();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        double currentScale = 1.0 + (0.04 * hoverAnim);

        if (hoverAnim > 0) {
            double tx = (w * (currentScale - 1.0)) / 2.0;
            double ty = (h * (currentScale - 1.0)) / 2.0;
            g2.translate(-tx, -ty);
            g2.scale(currentScale, currentScale);
        }

        // Shadow & Body
        if (hoverAnim > 0) {
            g2.setColor(new Color(C_GLOW.getRed(), C_GLOW.getGreen(), C_GLOW.getBlue(), (int)(100 * hoverAnim)));
            g2.fillRoundRect(0, 4, w, h, 24, 24);
        }
        g2.setColor(C_CARD_BG);
        g2.fillRoundRect(0, 0, w, h, 24, 24);

        // Poster Area
        Shape oldClip = g2.getClip();
        RoundRectangle2D posterRect = new RoundRectangle2D.Float(8, 8, w - 16, h - 90, 20, 20);
        g2.clip(posterRect); // Intersects with existing clip (like JViewport) to keep it inside boundaries
        drawPoster(g2, 8, 8, w - 16, h - 90);
        g2.setClip(oldClip); // Restore the previous clip correctly

        // Text Content
        drawInfoArea(g2, w, h);

        // Border Glow
        g2.setStroke(new BasicStroke((float)(1.0 + hoverAnim)));
        Color borderStart = new Color(255, 255, 255, 20);
        int r = (int)(borderStart.getRed() + (C_ACCENT.getRed() - borderStart.getRed()) * hoverAnim);
        int cg = (int)(borderStart.getGreen() + (C_ACCENT.getGreen() - borderStart.getGreen()) * hoverAnim);
        int b = (int)(borderStart.getBlue() + (C_ACCENT.getBlue() - borderStart.getBlue()) * hoverAnim);
        int a = (int)(borderStart.getAlpha() + (C_ACCENT.getAlpha() - borderStart.getAlpha()) * hoverAnim);
        g2.setColor(new Color(r, cg, b, a));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 24, 24);

        g2.dispose();
    }

    private void drawPoster(Graphics2D g2, int x, int y, int w, int h) {
        String url = movie.getPosterUrl();
        Image img = (url != null) ? imageCache.get(url) : null;

        if (img != null) {
            g2.drawImage(img, x, y, w, h, null);
            GradientPaint bottomGrad = new GradientPaint(x, y + h - 60, new Color(15, 23, 42, 0), x, y + h, new Color(15, 23, 42, 210));
            g2.setPaint(bottomGrad);
            g2.fillRect(x, y + h - 60, w, 60);
        } else {
            // Placeholder: Animated Loading or Icon
            GradientPaint grad = new GradientPaint(x, y, new Color(30, 41, 59), x, y + h, new Color(15, 23, 42));
            g2.setPaint(grad);
            g2.fillRect(x, y, w, h);
            
            g2.setColor(isLoading ? C_ACCENT : new Color(99, 102, 241, 100));
            g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = x + w/2; int cy = y + h/2;
            if (isLoading) {
                // Draw a simple loading circle
                g2.drawArc(cx - 15, cy - 15, 30, 30, (int)(System.currentTimeMillis()/5 % 360), 270);
            } else {
                g2.drawRoundRect(cx - 20, cy - 15, 40, 30, 4, 4);
                g2.drawLine(cx - 10, cy - 15, cx - 10, cy + 15);
                g2.drawLine(cx + 10, cy - 15, cx + 10, cy + 15);
            }
        }
    }

    private void drawInfoArea(Graphics2D g2, int w, int h) {
        g2.setColor(C_TEXT_MAIN);
        g2.setFont(new Font("Inter", Font.BOLD, 15));
        String title = movie.getTitle();
        FontMetrics fm = g2.getFontMetrics();
        if (fm.stringWidth(title) > w - 40) {
            title = title.substring(0, 15) + "...";
        }
        g2.drawString(title, 16, h - 55);

        String genresStr = movie.getGenres() != null && !movie.getGenres().isEmpty() ? 
            movie.getGenres().stream().map(Genre::getName).limit(3).collect(Collectors.joining(", ")) : "";
        if (movie.getGenres() != null && movie.getGenres().size() > 3) genresStr += "...";

        g2.setFont(new Font("Inter", Font.PLAIN, 12));
        g2.setColor(C_TEXT_SUB);
        g2.drawString(movie.getDuration() + " phút", 16, h - 35);

        if (!genresStr.isEmpty()) {
            g2.setFont(new Font("Inter", Font.PLAIN, 11));
            g2.setColor(new Color(148, 163, 184, 160));
            FontMetrics fmGen = g2.getFontMetrics();
            if (fmGen.stringWidth(genresStr) > w - 32) {
                genresStr = genresStr.substring(0, 20) + "...";
            }
            g2.drawString(genresStr, 16, h - 16);
        }

        drawStatusBadge(g2, movie.getStatus(), w - 95, h - 48);
    }

    private void drawStatusBadge(Graphics2D g2, MovieStatus status, int x, int y) {
        String text = status.getLabel();
        Color bg = new Color(34, 197, 94, 50); Color fg = Color.decode("#86EFAC");
        if (status == MovieStatus.COMING_SOON) { bg = new Color(99, 102, 241, 50); fg = Color.decode("#A5B4FC"); }
        else if (status == MovieStatus.ENDED) { bg = new Color(100, 116, 139, 50); fg = Color.decode("#CBD5E1"); }

        g2.setColor(bg); g2.fillRoundRect(x, y, 75, 22, 10, 10);
        g2.setColor(fg); g2.setFont(new Font("Inter", Font.BOLD, 10));
        g2.drawString(text, x + 8, y + 15);
    }

    private void showContextMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        menu.putClientProperty(FlatClientProperties.STYLE, "arc: 12;");
        JMenuItem editItem = new JMenuItem("Sửa"); editItem.addActionListener(al -> onEdit.run());
        JMenuItem deleteItem = new JMenuItem("Xóa"); deleteItem.setForeground(Color.decode("#F43F5E")); 
        deleteItem.addActionListener(al -> onDelete.run());
        menu.add(editItem); menu.addSeparator(); menu.add(deleteItem);
        menu.show(this, e.getX(), e.getY());
    }
}
