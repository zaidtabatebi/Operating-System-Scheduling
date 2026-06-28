import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    static Map<String, double[]> results = new LinkedHashMap<>();

    public static void main(String[] args) throws CloneNotSupportedException {
        Scanner scan = new Scanner(System.in);

        System.out.println("Enter the context switch");
        int context = scan.nextInt();

        System.out.println("enter the # of processes: ");
        int numberOfTotalProcesses = scan.nextInt();

        System.out.println("enter the degree of MP: ");
        int degreeOfMP = scan.nextInt();

        System.out.println("enter the number of bursts in each process: ");
        int numberOfBursts = scan.nextInt();

        System.out.println("enter the range of CPU bursts\nMin: ");
        int minCPU = scan.nextInt();
        System.out.println("Max: ");
        int maxCPU = scan.nextInt();

        System.out.println("enter the range of IO bursts\nMin: ");
        int minIO = scan.nextInt();
        System.out.println("Max: ");
        int maxIO = scan.nextInt();

        System.out.println("enter the range of priority\nMin: ");
        int minPriority = scan.nextInt();
        System.out.println("Max: ");
        int maxPriority = scan.nextInt();

        System.out.println("enter the range of initial arrival times\nMin: ");
        int minArrival = scan.nextInt();
        System.out.println("Max: ");
        int maxArrival = scan.nextInt();

        System.out.println("enter initial Tau: ");
        int initialTau = scan.nextInt();

        System.out.println("enter alpha: ");
        double alpha = scan.nextDouble();

        System.out.println("enter your type of generating random number of CPU/IO bursts\n(0) for gaussian / (other) for binomial: ");
        int type = scan.nextInt();
        boolean randomType = type == 0;

        Queue<Process> jobQueue = new PriorityQueue<>(numberOfTotalProcesses, new SortByArraivalTime());

        generateJobs(jobQueue, numberOfTotalProcesses, randomType, numberOfBursts, minCPU, maxCPU, minIO, maxIO,
                minPriority, maxPriority, minArrival, maxArrival, initialTau);

        Queue<Process> jobQueueInst1 = cloneQueue(jobQueue);
        Queue<Process> jobQueueInst2 = cloneQueue(jobQueue);
        Queue<Process> jobQueueInst3 = cloneQueue(jobQueue);
        Queue<Process> jobQueueInst4 = cloneQueue(jobQueue);
        Queue<Process> jobQueueInst5 = cloneQueue(jobQueue);
        Queue<Process> jobQueueInst6 = cloneQueue(jobQueue);
        Queue<Process> jobQueueInst7 = cloneQueue(jobQueue);
        Queue<Process> jobQueueInst8 = cloneQueue(jobQueue);

        FirstComeFirstServed FCFSObject = new FirstComeFirstServed(jobQueueInst1, new LinkedList<>(), new LinkedList<>(), degreeOfMP, context);
        ShortestJobFirst SJFObject = new ShortestJobFirst(jobQueueInst2, new PriorityQueue<>(numberOfTotalProcesses, new SortByShortestCPUBurst()), new LinkedList<>(), degreeOfMP);
        ShortestRemainingTimeFirst SRJFObject = new ShortestRemainingTimeFirst(jobQueueInst3, new PriorityQueue<>(numberOfTotalProcesses, new SortByShortestCPUBurst()), new LinkedList<>(), degreeOfMP);
        Priority priority = new Priority(jobQueueInst4, new PriorityQueue<>(numberOfTotalProcesses, new SortByPriority()), new LinkedList<>(), degreeOfMP);
        RoundRobin RR = new RoundRobin(jobQueueInst5, new PriorityQueue<>(numberOfTotalProcesses, new SortByArraivalTime()), new LinkedList<>(), degreeOfMP);
        ExponentialAveraging expAveraging = new ExponentialAveraging(jobQueueInst6, new PriorityQueue<>(numberOfTotalProcesses, new SortByExpAveraging()), new LinkedList<>(), degreeOfMP, alpha, context);

        RandomScheduler randomScheduler = new RandomScheduler(jobQueueInst7, new LinkedList<>(), new LinkedList<>(), degreeOfMP, context);
        PriorityScheduler priorityScheduler = new PriorityScheduler(jobQueueInst8, new LinkedList<>(), degreeOfMP, context);

        System.out.println("Your processes status are like this before performing any algorithm : ");
        System.out.println("________________");
        while (!jobQueue.isEmpty()) {
            Process p = jobQueue.poll();
            System.out.println("pID : " + p.pID + "\t\tArrival Time : " + p.arrivalTime + "\tPriority : " + p.priority + "\tBursts : " + returnBursts(p));
        }

        scan.nextLine(); // flush
        System.out.println("\nRunning FCFS...");
        FCFSObject.applyFCFS();

        System.out.println("\nRunning SJF...");
        SJFObject.applySJF();

        System.out.println("\nRunning SRJF...");
        SRJFObject.applySRJF();

        System.out.println("\nRunning Round Robin...");
        RR.applyRR(4); // example quantum

        System.out.println("\nRunning Exponential Averaging...");
        expAveraging.applyExpAverage();

        System.out.println("\nRunning Random Scheduler...");
        double[] randomStats = randomScheduler.applyRandom();
        results.put("RandomScheduler", randomStats);

        System.out.println("\nRunning Priority Scheduler...");
        double[] priorityStats = priorityScheduler.applyPriority();
        results.put("PriorityScheduler", priorityStats);

        System.out.println("\n\n===== Algorithm Comparison =====");
        System.out.println("Algorithm\t\t\tAvg Waiting Time\tTurnaround Time");
        for (String algo : results.keySet()) {
            double[] val = results.get(algo);
            System.out.printf("%-20s %.2f\t\t\t%.2f\n", algo, val[0], val[1]);
        }
    }

    public static Queue<Process> cloneQueue(Queue<Process> original) throws CloneNotSupportedException {
        Queue<Process> cloned = new PriorityQueue<>(original.size(), new SortByArraivalTime());
        for (Process p : original) {
            cloned.add((Process) p.clone());
        }
        return cloned;
    }

    public static String returnBursts(Process p) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < p.content.length; i++) {
            if (p.content[i].type) {
                str.append("CPU(").append(p.content[i].timeNeeded).append(")");
            } else {
                str.append("IO(").append(p.content[i].timeNeeded).append(")");
            }
            if (i != p.content.length - 1) {
                str.append(", ");
            }
        }
        return str.toString();
    }

    public static void generateJobs(Queue<Process> jobQueue, int numberOfTotalProcesses, boolean typeOfRandomGenerator,
                                    int numberOfBursts, int minCPU, int maxCPU, int minIO, int maxIO, int minPriority, int maxPriority,
                                    int minArrival, int maxArrival, int initialTau) {
        for (int i = 0; i < numberOfTotalProcesses; i++) {
            int numberOfCpuBursts;
            if (typeOfRandomGenerator)
                numberOfCpuBursts = randomGaussianInt(0.75 * numberOfBursts, numberOfBursts / 2, numberOfBursts);
            else
                numberOfCpuBursts = randomBinomialInt(0.75 * numberOfBursts, numberOfBursts / 2, numberOfBursts);

            int numberOfIoBursts = numberOfBursts - numberOfCpuBursts;
            Burst[] content = new Burst[numberOfBursts];

            content[0] = new Burst(true, ThreadLocalRandom.current().nextInt(minCPU, maxCPU + 1), 0);
            content[numberOfBursts - 1] = new Burst(true, ThreadLocalRandom.current().nextInt(minCPU, maxCPU + 1), 0);
            numberOfCpuBursts -= 2;

            for (int j = 1; j < numberOfBursts - 1; j++) {
                if (content[j - 1].type) {
                    if (numberOfCpuBursts + 1 > numberOfIoBursts) {
                        if (numberOfCpuBursts != 0 && (ThreadLocalRandom.current().nextInt(0, 2) == 0 || numberOfIoBursts == 0)) {
                            content[j] = new Burst(true, ThreadLocalRandom.current().nextInt(minCPU, maxCPU + 1), 0);
                            numberOfCpuBursts--;
                        } else {
                            content[j] = new Burst(false, ThreadLocalRandom.current().nextInt(minIO, maxIO + 1), 0);
                            numberOfIoBursts--;
                        }
                    } else {
                        content[j] = new Burst(false, ThreadLocalRandom.current().nextInt(minIO, maxIO + 1), 0);
                        numberOfIoBursts--;
                    }
                } else {
                    content[j] = new Burst(true, ThreadLocalRandom.current().nextInt(minCPU, maxCPU + 1), 0);
                    numberOfCpuBursts--;
                }
            }

            jobQueue.add(new Process(ThreadLocalRandom.current().nextInt(minPriority, maxPriority + 1),
                    ThreadLocalRandom.current().nextInt(minArrival, maxArrival + 1), content, initialTau));
        }
    }

    public static int randomBinomialInt(double mean, int min, int max) {
        if (max < min || mean < min || mean > max) {
            throw new IllegalArgumentException();
        }
        int n = max - min;
        double p = ((double) (mean - min)) / n;
        int val = min;
        for (int i = 0; i < n; i++) {
            if (Math.random() <= p) {
                val++;
            }
        }
        return val;
    }

    public static int randomGaussianInt(double mean, int min, int max) {
        if (max < min || mean < min || mean > max) {
            throw new IllegalArgumentException();
        }
        int n = max - min;
        double p = ((double) (mean - min)) / n;
        double sd = n * p * (1 - p);
        int val = (int) (new Random().nextGaussian() * sd + mean + .5);
        if (val < min) val = min;
        else if (val > max) val = max;
        return val;
    }
}