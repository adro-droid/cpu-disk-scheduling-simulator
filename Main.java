/*
 * Non-Preemptive Priority Scheduling
 * Owner: Ocampo, Marc Julius C.
 * Course: ICS26012 - Operating Systems
 *
 * Lower priority number means higher priority.
 * 
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class Main {

    static ArrayList<Process> processes = new ArrayList<>();

    static DefaultTableModel processModel;
    static DefaultTableModel resultModel;

    static JTextField pidField;
    static JTextField arrivalField;
    static JTextField burstField;
    static JTextField priorityField;

    static JLabel averageWTLabel;
    static JLabel averageTATLabel;
    static JLabel averageRTLabel;
    static JLabel utilizationLabel;
    static JLabel throughputLabel;

    static GanttChartPanel ganttChart;

    static final int MAX_PROCESSES = 20;
    static final long MAX_INPUT = 1000000;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createWindow());
    }

    private static void createWindow() {

        JFrame frame = new JFrame(
            "Priority Non-Preemptive Scheduling"
        );

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 720);
        frame.setMinimumSize(new Dimension(850, 620));
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(238, 240, 243));
        mainPanel.setBorder(
            BorderFactory.createEmptyBorder(15, 18, 15, 18)
        );

        
        JLabel title = new JLabel(
            "PRIORITY NON-PREEMPTIVE SCHEDULING"
        );

        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(35, 45, 60));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout(8, 12));
        topPanel.setOpaque(false);
        topPanel.add(title, BorderLayout.NORTH);

    
        JPanel inputPanel = new JPanel(
            new GridLayout(2, 1, 5, 5)
        );

        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        );

      
        JPanel inputRow = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 8, 5)
        );

        inputRow.setBackground(Color.WHITE);

        pidField = new JTextField(6);
        arrivalField = new JTextField(6);
        burstField = new JTextField(6);
        priorityField = new JTextField(6);

        inputRow.add(new JLabel("Process ID:"));
        inputRow.add(pidField);

        inputRow.add(new JLabel("Arrival Time:"));
        inputRow.add(arrivalField);

        inputRow.add(new JLabel("Burst Time:"));
        inputRow.add(burstField);

        inputRow.add(new JLabel("Priority:"));
        inputRow.add(priorityField);

     
        JPanel buttonRow = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 10, 5)
        );

        buttonRow.setBackground(Color.WHITE);

        JButton addButton = new JButton("Add Process");
        JButton runButton = new JButton("Run Scheduling");
        JButton sampleButton = new JButton("Load Sample Data");
        JButton clearButton = new JButton("Clear All");

        buttonRow.add(addButton);
        buttonRow.add(runButton);
        buttonRow.add(sampleButton);
        buttonRow.add(clearButton);

        inputPanel.add(inputRow);
        inputPanel.add(buttonRow);

        topPanel.add(inputPanel, BorderLayout.CENTER);

        mainPanel.add(topPanel, BorderLayout.NORTH);

  
        processModel = new DefaultTableModel(
            new String[]{
                "PID", "Arrival Time", "Burst Time", "Priority"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultModel = new DefaultTableModel(
            new String[]{
                "PID", "AT", "BT", "Priority",
                "CT", "TAT", "WT", "RT"
            }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable processTable = makeTable(processModel);
        JTable resultTable = makeTable(resultModel);

        ganttChart = new GanttChartPanel();


        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab(
            "Processes",
            new JScrollPane(processTable)
        );

        tabs.addTab(
            "Scheduling Results",
            new JScrollPane(resultTable)
        );

        JScrollPane chartScroll = new JScrollPane(ganttChart);

        chartScroll.setHorizontalScrollBarPolicy(
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        chartScroll.setVerticalScrollBarPolicy(
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
        );

        tabs.addTab("Gantt Chart", chartScroll);

        mainPanel.add(tabs, BorderLayout.CENTER);

        // Performance metrics.
        JPanel metricsPanel = new JPanel(
            new GridLayout(2, 3, 10, 5)
        );

        metricsPanel.setBackground(Color.WHITE);
        metricsPanel.setBorder(
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        );

        averageWTLabel = new JLabel("Average WT: -");
        averageTATLabel = new JLabel("Average TAT: -");
        averageRTLabel = new JLabel("Average RT: -");

        utilizationLabel = new JLabel("CPU Utilization: -");
        throughputLabel = new JLabel("Throughput: -");

        metricsPanel.add(averageWTLabel);
        metricsPanel.add(averageTATLabel);
        metricsPanel.add(averageRTLabel);
        metricsPanel.add(utilizationLabel);
        metricsPanel.add(throughputLabel);
        metricsPanel.add(new JLabel("Time unit: ms"));

        mainPanel.add(metricsPanel, BorderLayout.SOUTH);

        // Button actions.
        addButton.addActionListener(e -> addProcess());
        runButton.addActionListener(e -> runScheduling(tabs));
        sampleButton.addActionListener(e -> loadSampleData());
        clearButton.addActionListener(e -> clearAll());

        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    private static JTable makeTable(DefaultTableModel model) {

        JTable table = new JTable(model);

        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 13));

        table.getTableHeader().setFont(
            new Font("Arial", Font.BOLD, 13)
        );

        return table;
    }


    private static void addProcess() {

        if (processes.size() >= MAX_PROCESSES) {
            showError("Maximum of 20 processes allowed.");
            return;
        }

        String id = pidField.getText().trim().toUpperCase();

        if (!id.matches("P[1-9][0-9]*")) {
            showError("Enter a valid Process ID (example: P1).");
            return;
        }

        int processNumber;
        long arrival;
        long burst;
        long priority;

        try {

            processNumber = Integer.parseInt(id.substring(1));

            arrival = Long.parseLong(
                arrivalField.getText().trim()
            );

            burst = Long.parseLong(
                burstField.getText().trim()
            );

            priority = Long.parseLong(
                priorityField.getText().trim()
            );

        } catch (NumberFormatException ex) {

            showError("Please enter valid whole numbers.");
            return;
        }

        // Check for duplicate process IDs.
        for (Process p : processes) {

            if (p.processNumber == processNumber) {
                showError("Process ID already exists.");
                return;
            }
        }

        // Validate time and priority values.
        if (arrival < 0 || burst <= 0 || priority < 0) {

            showError(
                "Arrival and priority must be non-negative. "
                + "Burst must be positive."
            );

            return;
        }

        if (arrival > MAX_INPUT || burst > MAX_INPUT
                || priority > MAX_INPUT
                || processNumber > MAX_INPUT) {

            showError("Input values must not exceed 1,000,000.");
            return;
        }

        Process p = new Process(
            processNumber, id, arrival, burst, priority
        );

        processes.add(p);

        processModel.addRow(new Object[]{
            id, arrival, burst, priority
        });

        resetResults();

        pidField.setText("");
        arrivalField.setText("");
        burstField.setText("");
        priorityField.setText("");

        pidField.requestFocus();
    }


    private static void runScheduling(JTabbedPane tabs) {

        if (processes.isEmpty()) {
            showError("Please add at least one process.");
            return;
        }

        PriorityScheduler scheduler = new PriorityScheduler();

        PriorityScheduler.ScheduleResult result =
            scheduler.schedule(processes);

        resultModel.setRowCount(0);

        // Display processes in numerical PID order.
        ArrayList<Process> sorted =
            new ArrayList<>(processes);

        sorted.sort(
            Comparator.comparingInt(p -> p.processNumber)
        );

        for (Process p : sorted) {

            resultModel.addRow(new Object[]{
                p.id,
                p.arrivalTime,
                p.burstTime,
                p.priority,
                p.completionTime,
                p.turnaroundTime,
                p.waitingTime,
                p.responseTime
            });
        }


        averageWTLabel.setText(String.format(
            "Average WT: %.2f ms",
            result.averageWaitingTime
        ));

        averageTATLabel.setText(String.format(
            "Average TAT: %.2f ms",
            result.averageTurnaroundTime
        ));

        averageRTLabel.setText(String.format(
            "Average RT: %.2f ms",
            result.averageResponseTime
        ));

        utilizationLabel.setText(String.format(
            "CPU Utilization: %.2f%%",
            result.cpuUtilization
        ));

        throughputLabel.setText(String.format(
            "Throughput: %.4f processes/ms",
            result.throughput
        ));

        ganttChart.setSegments(
            result.segments, result.totalTime
        );

        tabs.setSelectedIndex(2);
    }


    private static void loadSampleData() {

        processes.clear();
        processModel.setRowCount(0);

        resetResults();

        addSample("P4", 0, 5, 2);
        addSample("P1", 0, 4, 2);
        addSample("P2", 2, 3, 1);
        addSample("P3", 2, 2, 3);
        addSample("P5", 10, 3, 2);
        addSample("P6", 10, 1, 2);
        addSample("P7", 20, 2, 1);
        addSample("P8", 25, 2, 2);
        addSample("P9", 25, 4, 2);
        addSample("P10", 25, 1, 2);
    }

    private static void addSample(
        String id, long arrival, long burst, long priority
    ) {

        int number = Integer.parseInt(id.substring(1));

        Process p = new Process(
            number, id, arrival, burst, priority
        );

        processes.add(p);

        processModel.addRow(new Object[]{
            id, arrival, burst, priority
        });
    }


    private static void clearAll() {

        processes.clear();
        processModel.setRowCount(0);

        resetResults();

        pidField.setText("");
        arrivalField.setText("");
        burstField.setText("");
        priorityField.setText("");

        pidField.requestFocus();
    }

    private static void resetResults() {

        resultModel.setRowCount(0);

        ganttChart.setSegments(
            new ArrayList<>(), 0
        );

        averageWTLabel.setText("Average WT: -");
        averageTATLabel.setText("Average TAT: -");
        averageRTLabel.setText("Average RT: -");

        utilizationLabel.setText("CPU Utilization: -");
        throughputLabel.setText("Throughput: -");
    }

    private static void showError(String message) {

        JOptionPane.showMessageDialog(
            null,
            message,
            "Input Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}


class Process {

    int processNumber;
    String id;

    long arrivalTime;
    long burstTime;
    long priority;

    long startTime;
    long completionTime;
    long turnaroundTime;
    long waitingTime;
    long responseTime;

    Process(
        int processNumber,
        String id,
        long arrivalTime,
        long burstTime,
        long priority
    ) {

        this.processNumber = processNumber;
        this.id = id;

        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
    }
}



class PriorityScheduler {

    static class Segment {

        String processID;
        long start;
        long end;

        Segment(String processID, long start, long end) {

            this.processID = processID;
            this.start = start;
            this.end = end;
        }
    }

    static class ScheduleResult {

        ArrayList<Segment> segments = new ArrayList<>();

        double averageWaitingTime;
        double averageTurnaroundTime;
        double averageResponseTime;

        double cpuUtilization;
        double throughput;

        long totalTime;
    }

    public ScheduleResult schedule(ArrayList<Process> processes) {

        ScheduleResult result = new ScheduleResult();

        ArrayList<Process> remaining =
            new ArrayList<>(processes);

        long currentTime = 0;
        long busyTime = 0;

        double totalWaitingTime = 0;
        double totalTurnaroundTime = 0;
        double totalResponseTime = 0;

        while (!remaining.isEmpty()) {

            Process selected = null;

            for (Process p : remaining) {

                if (p.arrivalTime <= currentTime) {

                    if (selected == null
                        || p.priority < selected.priority
                        || (p.priority == selected.priority
                            && p.arrivalTime < selected.arrivalTime)
                        || (p.priority == selected.priority
                            && p.arrivalTime == selected.arrivalTime
                            && p.processNumber < selected.processNumber)) {

                        selected = p;
                    }
                }
            }


            if (selected == null) {

                long nextArrival = Long.MAX_VALUE;

                for (Process p : remaining) {

                    if (p.arrivalTime < nextArrival) {
                        nextArrival = p.arrivalTime;
                    }
                }

                result.segments.add(
                    new Segment("IDLE", currentTime, nextArrival)
                );

                currentTime = nextArrival;
                continue;
            }


            selected.startTime = currentTime;

            selected.completionTime =
                currentTime + selected.burstTime;

            selected.turnaroundTime =
                selected.completionTime - selected.arrivalTime;

            selected.waitingTime =
                selected.turnaroundTime - selected.burstTime;

            selected.responseTime =
                selected.startTime - selected.arrivalTime;

            result.segments.add(
                new Segment(
                    selected.id,
                    currentTime,
                    selected.completionTime
                )
            );

            currentTime = selected.completionTime;

            busyTime += selected.burstTime;

            totalWaitingTime += selected.waitingTime;
            totalTurnaroundTime += selected.turnaroundTime;
            totalResponseTime += selected.responseTime;

            remaining.remove(selected);
        }

        result.totalTime = currentTime;

        int count = processes.size();

        result.averageWaitingTime =
            totalWaitingTime / count;

        result.averageTurnaroundTime =
            totalTurnaroundTime / count;

        result.averageResponseTime =
            totalResponseTime / count;

        result.cpuUtilization =
            (double) busyTime / result.totalTime * 100;

        result.throughput =
            (double) count / result.totalTime;

        return result;
    }
}



class GanttChartPanel extends JPanel implements Scrollable {

    private ArrayList<PriorityScheduler.Segment> segments =
        new ArrayList<>();

    private long totalTime;

    GanttChartPanel() {

        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(700, 230));
    }

    void setSegments(
        ArrayList<PriorityScheduler.Segment> newSegments,
        long newTotalTime
    ) {

        segments = new ArrayList<>(newSegments);
        totalTime = newTotalTime;

    

  int chartHeight = 190;

        setPreferredSize(new Dimension(700, chartHeight));

        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        if (segments.isEmpty() || totalTime <= 0) {

            g.setColor(Color.GRAY);

            g.drawString(
                "Run scheduling to display the Gantt chart.",
                25, 55
            );

            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        int left = 30;
        int top = 65;
        int chartWidth = Math.max(300, getWidth() - 60);
        int blockHeight = 55;

        g2.setFont(new Font("Arial", Font.BOLD, 13));
        g2.setColor(Color.DARK_GRAY);

        g2.drawString("Gantt Chart", left, 30);

        FontMetrics fm = g2.getFontMetrics();

 
        for (PriorityScheduler.Segment segment : segments) {

            int x1 = left + (int) Math.round(
                (double) segment.start / totalTime * chartWidth
            );

            int x2 = left + (int) Math.round(
                (double) segment.end / totalTime * chartWidth
            );

            int width = Math.max(1, x2 - x1);

            if (segment.processID.equals("IDLE")) {
                g2.setColor(new Color(205, 210, 218));
            } else {
                g2.setColor(new Color(100, 160, 220));
            }

            g2.fillRect(x1, top, width, blockHeight);

            g2.setColor(Color.DARK_GRAY);
            g2.drawRect(x1, top, width, blockHeight);

            int textWidth = fm.stringWidth(segment.processID);

            if (width >= textWidth + 8) {

                g2.drawString(
                    segment.processID,
                    x1 + (width - textWidth) / 2,
                    top + 33
                );
            }
        }

 
        ArrayList<Long> times = new ArrayList<>();

        times.add(segments.get(0).start);

        for (PriorityScheduler.Segment segment : segments) {
            times.add(segment.end);
        }

       int labelStartY = top + blockHeight + 22;

for (int i = 0; i < times.size(); i++) {

    long time = times.get(i);

    int x = left + (int) Math.round(
        (double) time / totalTime * chartWidth
    );

    String label = String.valueOf(time);
    int textWidth = fm.stringWidth(label);

    int labelX = x - textWidth / 2;

    g2.setColor(Color.DARK_GRAY);

    g2.drawLine(
        x, top + blockHeight,
        x, top + blockHeight + 5
    );

    g2.drawString(label, labelX, labelStartY);
}

        g2.dispose();
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(700, 230);
    }

    @Override
    public int getScrollableUnitIncrement(
        Rectangle visibleRect, int orientation, int direction
    ) {
        return 20;
    }

    @Override
    public int getScrollableBlockIncrement(
        Rectangle visibleRect, int orientation, int direction
    ) {
        return 50;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
