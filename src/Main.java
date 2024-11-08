import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.LineBorder;

class DrawVolume extends JPanel {
    public int volume;
    Point point1;
    Point point2;
    Line2D currentLine;
    JLabel volumeLabel;
    private final ArrayList<Line2D> lines = new ArrayList<>();

    public DrawVolume(JLabel volumeLabel) {
        this.volumeLabel = volumeLabel;
        setPreferredSize(new Dimension(250, 40));
        setMinimumSize(new Dimension(100, 20));
        setMaximumSize(new Dimension(100, 20));
        setBackground(Color.WHITE);
        setBorder(new LineBorder(Color.BLACK, 1));
        MouseHandler handler = new MouseHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);
    }
    void resetBar(){
        lines.clear();
        volume = 0;
        updateVolume();
        repaint();
}
        @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1f));

        for (Line2D line : lines) {
            g2d.draw(line);
        }

        if (point1 != null && point2 != null) {
            g2d.draw(currentLine);
        }
    }

    private int getPercentage() {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        paint(image.getGraphics());

        int blackPixels = 0;
        int totalPixels = getWidth() * getHeight();

        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                if (rgb == Color.BLACK.getRGB()) {
                    blackPixels++;
                }
            }
        }

        return (blackPixels * 100) / totalPixels;
    }

    private void updateVolume(){
        volume = getPercentage();
        changeVolume(volume);
        volumeLabel.setText("Volume: " + volume + "%");
    }

    private class MouseHandler extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            point1 = e.getPoint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Point p = e.getPoint();
            p.x = Math.max(0, Math.min(p.x, getWidth() - 1));
            p.y = Math.max(0, Math.min(p.y, getHeight() - 1));

            point2 = p;
            currentLine = new Line2D.Double(point1, point2);
            lines.add(currentLine);
            point1 = point2;
            getPercentage();
            updateVolume();
            repaint();
            System.out.println(volume);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (currentLine != null) {
                lines.add(currentLine);
                currentLine = null;
                point1 = null;
                point2 = null;
                repaint();
            }
        }
    }

    public void changeVolume(int volumeToBeChanged) {
        try {
            String[] command = { "osascript", "-e", "set volume output volume " + volumeToBeChanged};
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}


class UI extends JPanel {
    UI() {
        Font myFont = new Font("Times New Roman", Font.BOLD, 30);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(500, 400));

        JPanel drawingContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        drawingContainer.setBackground(Color.LIGHT_GRAY);

        JLabel volumeLabel = new JLabel("Volume: 0%");

        DrawVolume volumeBar = new DrawVolume(volumeLabel);
        drawingContainer.add(volumeBar);

        volumeLabel.setFont(myFont);
        JButton resetButton = new JButton("Clear");
        resetButton.setFont(myFont);
        resetButton.addActionListener(e -> {
            volumeBar.resetBar();
        });
        drawingContainer.add(resetButton);
        drawingContainer.add(volumeLabel, 0);
        add(drawingContainer);
    }
}

public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame("Line Drawer");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            UI ui = new UI();
            frame.add(ui);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}