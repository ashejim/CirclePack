package frames;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * Borderless splash window.
 * Top area  : Owl.png scaled to fit.
 * Bottom strip: status label + progress bar.
 */
public class OwlSplashScreen extends JWindow {

    private static final int W     = 420;
    private static final int IMG_H = 200;
    private static final int BAR_H =  54;

    private final JProgressBar progressBar;
    private final JLabel       statusLabel;

    public OwlSplashScreen() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 180), 2));
        content.setBackground(Color.WHITE);

        content.add(buildImagePanel(), BorderLayout.CENTER);

        // --- bottom status strip ---
        JPanel statusPanel = new JPanel(new BorderLayout(0, 4));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 8, 12));
        statusPanel.setBackground(Color.WHITE);

        statusLabel = new JLabel("Starting...");
        statusLabel.setFont(statusLabel.getFont().deriveFont(11f));

        progressBar = new JProgressBar(0, 100);
        progressBar.setForeground(new Color(0, 0, 200));
        progressBar.setPreferredSize(new Dimension(0, 14));

        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(progressBar, BorderLayout.SOUTH);
        content.add(statusPanel, BorderLayout.SOUTH);

        setContentPane(content);
        setSize(W, IMG_H + BAR_H);
        setLocationRelativeTo(null);
    }

    private JPanel buildImagePanel() {
        return new JPanel() {
            {
                setPreferredSize(new Dimension(W, IMG_H));
                setBackground(Color.WHITE);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                URL url = getClass().getResource("/Owl.png");
                if (url == null) {
                    g.setFont(g.getFont().deriveFont(Font.BOLD, 22f));
                    g.setColor(new Color(30, 30, 40));
                    g.drawString("Owl App", 150, getHeight() / 2);
                    return;
                }
                ImageIcon icon  = new ImageIcon(url);
                Image     img   = icon.getImage();
                double    scale = Math.min(
                    (double) getWidth()  / icon.getIconWidth(),
                    (double) getHeight() / icon.getIconHeight()
                );
                int dw = (int)(icon.getIconWidth()  * scale);
                int dh = (int)(icon.getIconHeight() * scale);
                int x  = (getWidth()  - dw) / 2;
                int y  = (getHeight() - dh) / 2;
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.drawImage(img, x, y, dw, dh, this);
            }
        };
    }

    public void setStatus(String message, int percent) {
        statusLabel.setText(message);
        progressBar.setValue(percent);
    }
}
