import java.util.*;

public class PriorityScheduler {

    Queue<Process> jobQueue;
    PriorityQueue<Process> readyQueue;
    LinkedList<Process> deviceList;
    int degreeOfMP;
    int contix;
    Random random = new Random();

    public PriorityScheduler(Queue<Process> jobQueue, LinkedList<Process> deviceList, int degreeOfMP, int contix) {
        this.jobQueue = jobQueue;
        this.deviceList = deviceList;
        this.degreeOfMP = degreeOfMP;
        this.contix = contix;
        this.readyQueue = new PriorityQueue<>(Comparator.comparingInt(p -> p.priority));
    }

    public double[] applyPriority() {
        int currentTime = 0;
        int capacity = degreeOfMP;
        Process runningState = null;
        int totalWaitingTime = 0;
        int totalTurnaroundTime = 0;
        int completedProcesses = 0;
        List<Process> finished = new ArrayList<>();

        while (true) {
            while (!jobQueue.isEmpty() && jobQueue.peek().arrivalTime <= currentTime) {
                readyQueue.add(jobQueue.poll());
            }

            for (Process p : readyQueue) {
                p.timeInTheSystem++;
                p.timeInTheReadyQueue++;
            }
            for (Process p : deviceList) {
                p.timeInTheSystem++;
            }
            if (runningState != null) {
                runningState.timeInTheSystem++;
            }

            if (runningState == null && !readyQueue.isEmpty()) {
                runningState = readyQueue.poll();
            }

            if (runningState != null) {
                runningState.decrementFCFS();
                if (runningState.getCurrentBurst().timeNeeded == 0) {
                    runningState.currentBurst++;
                    if (runningState.currentBurst >= runningState.content.length) {
                        finished.add(runningState);
                        totalWaitingTime += runningState.timeInTheReadyQueue;
                        totalTurnaroundTime += runningState.timeInTheSystem;
                        completedProcesses++;
                        runningState = null;
                        capacity++;
                    } else if (!runningState.isCPU()) {
                        deviceList.add(runningState);
                        runningState = null;
                    }
                }
            }

            for (int i = 0; i < deviceList.size(); i++) {
                deviceList.get(i).decrementFCFS();
                if (deviceList.get(i).getCurrentBurst().timeNeeded == 0) {
                    Process p = deviceList.remove(i);
                    p.currentBurst++;
                    p.arrivalTime = currentTime;
                    readyQueue.add(p);
                    i--;
                }
            }

            currentTime++;

            if (runningState == null && deviceList.isEmpty() && readyQueue.isEmpty() && jobQueue.isEmpty()) {
                break;
            }
        }

        double avgWaiting = (double) totalWaitingTime / completedProcesses;
        double avgTurnaround = (double) totalTurnaroundTime / completedProcesses;

        return new double[]{avgWaiting, avgTurnaround};
    }
}
