# CPU and Disk Scheduling Simulator

ICS26012 – Operating Systems · Project – Alternative Assessment
1st Semester, A.Y. 2026–2027 (Finals Period) · Section: 3ITB · Group: 5
University of Santo Tomas, College of Information and Computing Sciences
Submitted to: Asst. Prof. Bernard G. Sanidad, DIT

A Java Swing application that simulates two CPU scheduling algorithms and one disk scheduling algorithm. The user enters the problem values, and the program shows the schedule or the head's path together with the computed results, so hand-solved answers can be checked against the program.

## Members and assigned algorithms

| Member | Category | Assigned algorithm | Coordination role |
|---|---|---|---|
| Robin Gabriel Montoya | CPU – Preemptive | Shortest Remaining Time First (SRTF) | Leader |
| Marc Julius Ocampo | CPU – Non-preemptive | Priority (Non-Preemptive) | Testing Lead |
| Adriel Ross Martin | Disk Scheduling | C-SCAN | Documentation Lead |

## Repository

https://github.com/adro-droid/cpu-disk-scheduling-simulator

## Requirements

- Java 8 or later to run the executable JAR (a JRE is enough).
- JDK 11 or later to run the source file directly; any JDK 8 or later to compile it.
- Only the Java standard library is used (Swing and AWT). No database and no internet connection are required.

## How to run

Double-click `SchedulingSimulator.jar`, or run it from a terminal in the folder that contains the JAR:

```
java -jar SchedulingSimulator.jar
```

To run the source file directly (JDK 11 or later):

```
java SchedulingSimulator.java
```

## How to build from source

The whole program is in one file, `SchedulingSimulator.java`. Compile and run it:

```
javac -encoding UTF-8 SchedulingSimulator.java
java SchedulingSimulator
```

To create an executable JAR that runs on Java 8 or later, run these in a folder that contains only `SchedulingSimulator.java`:

```
javac --release 8 -encoding UTF-8 SchedulingSimulator.java
jar cfe SchedulingSimulator.jar SchedulingSimulator *.class
```

With JDK 8, leave out `--release 8` (that option exists only in JDK 9 and later).

## Program structure

`SchedulingSimulator.java` has four parts:

| Part | Class(es) | Owner | Purpose |
|---|---|---|---|
| 1 | `SchedulingSimulator` | Robin Gabriel Montoya | Main menu, About and Exit; switches between the screens (CardLayout) |
| 2 | `SRTF`, `SRTFProcess`, `SRTFScheduler`, `SRTFGanttChartPanel` | Robin Gabriel Montoya | Shortest Remaining Time First |
| 3 | `Main`, `Process`, `PriorityScheduler`, `GanttChartPanel` | Marc Julius Ocampo | Priority (Non-Preemptive) |
| 4 | `CScan` | Adriel Ross Martin | C-SCAN disk scheduling |

The scheduling logic (`SRTFScheduler`, `PriorityScheduler`, and the `cscan()` method) is kept separate from the screen code.

## Features

- Main menu that lists the algorithms under CPU Scheduling and Disk Scheduling, with About (members and assigned algorithms) and Exit.
- Each algorithm opens on its own screen with a Back to Main Menu button.
- Load Sample Data and Reset/Clear on every module.
- Input validation with clear error messages. The program does not crash on empty, negative, non-numeric, or out-of-range input.

| Module | Inputs | Outputs |
|---|---|---|
| SRTF | PID, arrival time, and burst time per process (up to 20 processes) | Gantt chart with idle time; CT, TAT, WT, and RT per process; averages; CPU utilization; throughput; number of preemptions; step log of every decision |
| Priority (Non-Preemptive) | PID, arrival time, burst time, and priority per process (up to 20 processes) | Gantt chart with idle time; CT, TAT, WT, and RT per process; averages; CPU utilization; throughput |
| C-SCAN | Disk size (default 200), initial head position, request sequence (separated by commas or spaces) | Graph of the head's path; seek sequence; head movement per step; total head movement; average seek distance |

## Scheduling conventions

The program follows the standard conventions in section 4.3 of the project brief.

### CPU scheduling (SRTF and Priority)

- Time starts at 0 in whole time units. The program labels time values as ms; the values entered are not converted.
- If no process is ready, the CPU is idle, and the idle interval is shown in the Gantt chart. Context-switch time is 0.
- Ties (arrival time, burst/remaining time, or priority) go to the earlier arrival, then to the lower PID.
- A lower priority number means a higher priority. There is no aging.
- SRTF preempts only if the new process's remaining time is strictly less than the running process's.
- Priority (Non-Preemptive): once a process starts, it runs until it completes.
- Formulas:
  - Turnaround = Completion − Arrival
  - Waiting = Turnaround − Burst
  - Response = First CPU time − Arrival
  - CPU utilization = Busy time ÷ Total time × 100
  - Throughput = Number of processes ÷ Total time

### Disk scheduling (C-SCAN)

- Cylinders run from 0 to N − 1 (default N = 200).
- The head moves toward the higher cylinders first, serving requests on the way, travels to the last cylinder (N − 1), jumps back to cylinder 0 without serving requests, and continues toward the higher cylinders.
- The return jump is counted in the total head movement and shown in the path.
- Head movement is the sum of the absolute differences between successive head positions, starting from the initial head position.
- Duplicate requests are served consecutively (zero movement).
- Average seek distance = Total head movement ÷ Number of requests.

## Sample data

| Module | Sample loaded by Load Sample Data |
|---|---|
| SRTF | P1 (0, 8), P2 (1, 4), P3 (2, 9), P4 (3, 5), P5 (30, 3), P6 (31, 1) as (arrival, burst) |
| Priority (Non-Preemptive) | P1–P10 with idle gaps and priority ties |
| C-SCAN | Disk size 200, head at 50, requests 170, 80, 35, 60, 90, 10, 40, 115 (total head movement 388) |

## Documentation

See `Documentation.pdf` in the submission folder for the discussion of each algorithm, the worked examples, the user manual, and the appendices.
