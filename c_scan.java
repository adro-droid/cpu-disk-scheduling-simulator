/*
 * C-SCAN Disk Scheduling
 * Owner: Martin, Adriel Ross H.
 * Course: ICS26012 - Operating Systems
 */

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

public class CScan extends JFrame {
    static final int DISK_SIZE = 200;
    static final Color PANEL_COLOR = new Color(0x44484C);
    static final Color TEXT_COLOR = new Color(0xE6E6E6);

    static final int SAMPLE_DISK_SIZE = 200;
    static final int SAMPLE_HEAD = 50;
    static final String SAMPLE_REQUESTS = "170, 80, 35, 60, 90, 10, 40, 115";

    int startHead = 0;
    int seekCount = 0;
    List<Integer> seekSequence = new ArrayList<>();

    int diskSize = DISK_SIZE;

    JTextField headField = new JTextField(6);
    JTextField requestField = new JTextField(28);
    JLabel totalLabel = new JLabel(" ");
    JTextArea sequenceArea = new JTextArea(2, 40);
    GraphPanel graph = new GraphPanel();

    JTextField diskField = new JTextField(String.valueOf(DISK_SIZE), 5);
    JTextArea stepsArea = new JTextArea(2, 40);
    JLabel averageLabel = new JLabel(" ");

    public CScan() {
        super("C-SCAN Disk Scheduling");

        JPanel inputs = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        inputs.setBackground(PANEL_COLOR);

        JLabel headLabel = new JLabel("Initial head position:");
        JLabel requestLabel = new JLabel("Request sequence:");
        headLabel.setForeground(TEXT_COLOR);
        requestLabel.setForeground(TEXT_COLOR);

        JLabel diskLabel = new JLabel("Disk size:");
        diskLabel.setForeground(TEXT_COLOR);
        inputs.add(diskLabel);
        inputs.add(diskField);

        inputs.add(headLabel);
        inputs.add(headField);
        inputs.add(requestLabel);
        inputs.add(requestField);
        JButton runButton = new JButton("Run C-SCAN");
        inputs.add(runButton);

        JButton sampleButton = new JButton("Load Sample Data");
        JButton resetButton = new JButton("Reset / Clear");
        inputs.add(sampleButton);
        inputs.add(resetButton);

        for (JTextField f : new JTextField[] {diskField, headField, requestField}) {
            f.setBackground(new Color(0x2E3134));
            f.setForeground(TEXT_COLOR);
            f.setCaretColor(TEXT_COLOR);
        }

        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 15f));
        totalLabel.setForeground(TEXT_COLOR);
        sequenceArea.setEditable(false);
        sequenceArea.setLineWrap(true);
        sequenceArea.setWrapStyleWord(true);
        sequenceArea.setOpaque(false);
        sequenceArea.setForeground(TEXT_COLOR);
        sequenceArea.setFont(totalLabel.getFont().deriveFont(Font.PLAIN, 14f));

        JPanel results = new JPanel(new BorderLayout(0, 6));
        results.setBackground(PANEL_COLOR);
        results.setBorder(BorderFactory.createEmptyBorder(10, 14, 14, 14));
        results.add(totalLabel, BorderLayout.NORTH);
        results.add(sequenceArea, BorderLayout.CENTER);

        stepsArea.setEditable(false);
        stepsArea.setLineWrap(true);
        stepsArea.setWrapStyleWord(true);
        stepsArea.setOpaque(false);
        stepsArea.setForeground(TEXT_COLOR);
        stepsArea.setFont(sequenceArea.getFont());
        averageLabel.setFont(totalLabel.getFont());
        averageLabel.setForeground(TEXT_COLOR);

        JPanel extraResults = new JPanel(new BorderLayout(0, 6));
        extraResults.setBackground(PANEL_COLOR);
        extraResults.add(stepsArea, BorderLayout.CENTER);
        extraResults.add(averageLabel, BorderLayout.SOUTH);
        results.add(extraResults, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(inputs, BorderLayout.NORTH);
        add(graph, BorderLayout.CENTER);
        add(results, BorderLayout.SOUTH);

        runButton.addActionListener(e -> run());
        diskField.addActionListener(e -> run());
        headField.addActionListener(e -> run());
        requestField.addActionListener(e -> run());
        sampleButton.addActionListener(e -> loadSampleData());
        resetButton.addActionListener(e -> resetAll());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 720);
        setMinimumSize(new Dimension(700, 500));
        setLocationRelativeTo(null);
    }

    void run() {
        int head;
        int[] requests;

        String diskText = diskField.getText().trim();
        if (diskText.isEmpty()) {
            showError("Please enter the disk size (number of cylinders).");
            return;
        }
        int newDiskSize;
        try {
            newDiskSize = Integer.parseInt(diskText);
        } catch (NumberFormatException ex) {
            showError("Disk size must be a whole number.\nExample: " + DISK_SIZE);
            return;
        }
        if (newDiskSize < 2) {
            showError("Disk size must be at least 2 cylinders.");
            return;
        }

        if (headField.getText().trim().isEmpty()) {
            showError("Please enter the initial head position.");
            return;
        }

        try {
            head = Integer.parseInt(headField.getText().trim());

            String text = requestField.getText().trim();
            if (text.isEmpty()) {
                showError("Please enter at least one request.");
                return;
            }
            String[] parts = text.split("[,\\s]+");
            requests = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                requests[i] = Integer.parseInt(parts[i]);
            }
        } catch (NumberFormatException ex) {
            showError("Please enter whole numbers only.\nExample: 170, 80, 35, 60");
            return;
        }

        if (head < 0 || head >= newDiskSize) {
            showError("Head position must be from 0 to " + (newDiskSize - 1) + ".");
            return;
        }
        for (int r : requests) {
            if (r < 0 || r >= newDiskSize) {
                showError("Request " + r + " is outside the disk (0 to " + (newDiskSize - 1) + ").");
                return;
            }
        }

        diskSize = newDiskSize;
        cscan(requests, head);

        totalLabel.setText("Total number of seek operations = " + seekCount);
        StringBuilder sb = new StringBuilder("Seek sequence:  ");
        for (int i = 0; i < seekSequence.size(); i++) {
            if (i > 0) sb.append("  →  ");
            sb.append(seekSequence.get(i));
        }
        sequenceArea.setText(sb.toString());

        List<Integer> points = new ArrayList<>();
        points.add(startHead);
        points.addAll(seekSequence);
        StringBuilder steps = new StringBuilder("Head movement per step:  ");
        for (int i = 0; i + 1 < points.size(); i++) {
            int from = points.get(i);
            int to = points.get(i + 1);
            if (i > 0) steps.append(",   ");
            steps.append(from).append(" → ").append(to).append(" = ").append(Math.abs(to - from));
        }
        stepsArea.setText(steps.toString());

        double average = (double) seekCount / requests.length;
        averageLabel.setText(String.format("Average seek distance = %d / %d = %.2f",
                seekCount, requests.length, average));

        graph.repaint();
    }

    void loadSampleData() {
        diskField.setText(String.valueOf(SAMPLE_DISK_SIZE));
        headField.setText(String.valueOf(SAMPLE_HEAD));
        requestField.setText(SAMPLE_REQUESTS);
    }

    void resetAll() {
        diskField.setText(String.valueOf(DISK_SIZE));
        headField.setText("");
        requestField.setText("");

        diskSize = DISK_SIZE;
        startHead = 0;
        seekCount = 0;
        seekSequence = new ArrayList<>();

        totalLabel.setText(" ");
        sequenceArea.setText("");
        stepsArea.setText("");
        averageLabel.setText(" ");
        graph.repaint();
    }

    void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid input", JOptionPane.ERROR_MESSAGE);
    }

    void cscan(int[] arr, int head) {
        startHead = head;
        seekCount = 0;
        seekSequence = new ArrayList<>();

        List<Integer> left = new ArrayList<>();
        List<Integer> right = new ArrayList<>();

        left.add(0);
        right.add(diskSize - 1);

        for (int track : arr) {
            if (track < head) left.add(track);
            else right.add(track);
        }

        Collections.sort(left);
        Collections.sort(right);

        for (int track : right) {
            seekSequence.add(track);
            seekCount += Math.abs(track - head);
            head = track;
        }

        head = 0;
        seekCount += (diskSize - 1);

        for (int track : left) {
            seekSequence.add(track);
            seekCount += Math.abs(track - head);
            head = track;
        }
    }

    class GraphPanel extends JPanel {
        final Color lineColor = new Color(0x39FF14);
        final Color jumpColor = new Color(0x39FF14);
        final Color gridColor = new Color(0x4D5156);
        final Color textColor = new Color(0xE6E6E6);

        GraphPanel() {
            setBackground(new Color(0x373A3D));
            setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(0x55595E)));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(getFont().deriveFont(13f));
            FontMetrics fm = g.getFontMetrics();

            if (seekSequence.isEmpty()) {
                String hint = "Enter the initial head position and request sequence above, then click Run C-SCAN.";
                g.setColor(Color.LIGHT_GRAY);
                g.drawString(hint, (getWidth() - fm.stringWidth(hint)) / 2, getHeight() / 2);
                return;
            }

            List<Integer> points = new ArrayList<>();
            points.add(startHead);
            points.addAll(seekSequence);

            int leftX = 60, rightX = getWidth() - 60;
            int axisY = 45, topY = 80, bottomY = getHeight() - 30;
            double rowGap = (double) (bottomY - topY) / Math.max(1, points.size() - 1);

            g.setColor(textColor);
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine(leftX, axisY, rightX, axisY);
            int[] ends = {0, diskSize - 1};
            for (int track : ends) {
                int x = trackToX(track, leftX, rightX);
                g.drawLine(x, axisY - 5, x, axisY + 5);
                String label = String.valueOf(track);
                g.drawString(label, x - fm.stringWidth(label) / 2, axisY - 12);
            }
            String title = "Track number";
            g.setColor(Color.LIGHT_GRAY);
            g.drawString(title, (leftX + rightX - fm.stringWidth(title)) / 2, axisY - 12);

            g.setStroke(new BasicStroke(1f));
            g.setColor(gridColor);
            for (int track : points) {
                int x = trackToX(track, leftX, rightX);
                g.drawLine(x, axisY, x, bottomY);
            }

            for (int i = 0; i + 1 < points.size(); i++) {
                int x1 = trackToX(points.get(i), leftX, rightX);
                int y1 = (int) (topY + i * rowGap);
                int x2 = trackToX(points.get(i + 1), leftX, rightX);
                int y2 = (int) (topY + (i + 1) * rowGap);

                boolean isJump = points.get(i) == diskSize - 1 && points.get(i + 1) == 0;
                if (isJump) {
                    g.setColor(jumpColor);
                    g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                            10f, new float[] {8f, 6f}, 0f));
                } else {
                    g.setColor(lineColor);
                    g.setStroke(new BasicStroke(2f));
                }
                g.drawLine(x1, y1, x2, y2);
                g.setStroke(new BasicStroke(2f));
                drawArrowHead(g, x1, y1, x2, y2);
            }

            for (int i = 0; i < points.size(); i++) {
                int track = points.get(i);
                int x = trackToX(track, leftX, rightX);
                int y = (int) (topY + i * rowGap);

                g.setColor(lineColor);
                g.fillOval(x - 5, y - 5, 10, 10);

                String label = (i == 0) ? "Head: " + track : String.valueOf(track);
                g.setColor(textColor);
                int labelX = (x > rightX - 80) ? x - 10 - fm.stringWidth(label) : x + 10;
                g.drawString(label, labelX, y - 6);
            }
        }

        int trackToX(int track, int leftX, int rightX) {
            return leftX + (int) Math.round((double) track / (diskSize - 1) * (rightX - leftX));
        }

        void drawArrowHead(Graphics2D g, int x1, int y1, int x2, int y2) {
            double midX = (x1 + x2) / 2.0, midY = (y1 + y2) / 2.0;
            double angle = Math.atan2(y2 - y1, x2 - x1);
            int size = 9;
            int ax = (int) (midX - size * Math.cos(angle - Math.PI / 7));
            int ay = (int) (midY - size * Math.sin(angle - Math.PI / 7));
            int bx = (int) (midX - size * Math.cos(angle + Math.PI / 7));
            int by = (int) (midY - size * Math.sin(angle + Math.PI / 7));
            g.fillPolygon(new int[] {(int) midX, ax, bx}, new int[] {(int) midY, ay, by}, 3);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new CScan().setVisible(true);
        });
    }
}
