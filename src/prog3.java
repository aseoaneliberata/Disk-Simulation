import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

public class prog3 {

	private static boolean debug = true;

	private static final int MAXCYL = 100; /* maximum number of cylinders */
	private static final int MAXTRK = 100; /*
											 * maximum number of tracks per
											 * cylinder
											 */
	private static final int MAXSEC = 100; /*
											 * maximum number of sectors per
											 * track
											 */
	private static final int MAXNR = 100; /* max # of disk I/O requests */

	private static int nc; /* # of disk cylinders = 1..MAXCYL */
	private static int nt; /* # of tracks per cylinder = 1..MAXTRK */
	private static int ns; /* # of sectors per track = 1..MAXSEC */
	private static int s1; /* see time per cylinder when |A-B| <= d */
	private static int s2; /* see time per cylinder when |A-B| > d */
	private static int d; /* disk seek time selection value */

	/*
	 * This array of structures contains all the input and computed data.
	 */
	public static ioreq[] req;

	private static int nr; /* # of disk I/O requests */

	private static int t; /* simulation time */

	private static int ccyl, csec; /* current disk cylinder & sector */

	public static void display(String alg) {
		DecimalFormat numberFormat = new DecimalFormat("#.00");

		int i;
		double aresp; /* average response time */
		double num; /* numerator for standard deviation */
		double rstdev; /* standard deviation of response times */
		int tresp;

		System.out.printf("Results for %s algorithm\n", alg);
		System.out.printf("======================");
		for (i = 0; i < alg.length(); i++)
			System.out.print("=");
		System.out.println();

		System.out.printf("I/O operations completed at time %d\n", t);

		/*-------------------------------------------------------------*/
		/* Determine average response time and its standard deviation. */
		/*-------------------------------------------------------------*/
		tresp = 0;
		for (i = 0; i < nr; i++)

		{
			tresp += (req[i].t_stop - req[i].submit);
		}
		aresp = (double) tresp / (double) nr;
		System.out.println("Average response time = " + numberFormat.format(aresp));
		if (nr == 1)
			System.out.printf("Standard deviation cannot be calculated.\n");
		else {
			num = 0;
			for (i = 0; i < nr; i++)
				num += Math.pow(req[i].t_stop - req[i].submit - aresp, 2.0);
			rstdev = Math.sqrt(num / (nr - 1));
			System.out.printf("Standard deviation of response times = " + numberFormat.format(rstdev));
		}

		System.out.println();

	}

	/*--------------------------------------------*/
	/* Return the time required to move the heads */
	/* from 'ccyl' to 'dest'. */
	/*--------------------------------------------*/
	public static int seek(int dest) {
		int dist;

		if (ccyl == dest) {
			return 0;
		}

		dist = ccyl - dest;
		if (dist < 0) {
			dist = -dist;
		}

		if (dist <= d) {
			return dist * s1;
		} else {
			return dist * s2;
		}
	}

	public static void fcfs() {
		ccyl = 0;
		csec = 0;
		t = 0;

		if (debug) {
			System.out.printf("Debug output for FCFS algorithm\n");
			System.out.printf("=============================\n");
		}

		for (int j = 0; j < nr; j++) {
			if (debug) {
				System.out.println("Request " + (j + 1) + " (" + req[j].nsec + " sec @ lba " + req[j].addr
						+ ") start at t = " + t + " (disk @ cyl " + ccyl + ")");

				System.out.println("	chs = " + req[j].acyl + " / " + req[j].atrk + " / " + req[j].asec);
			}

			if (t == 0 && req[0].acyl == 0) {

			} else {
				int temp = seek(req[j].acyl);
				t += temp;
				if (debug) {
					System.out.println("	seek to cylinder " + req[j].acyl + " completed at t = " + t);
				}
				ccyl = req[j].acyl;
			}

			int latency = (req[j].asec + ns - csec) % ns;
			t += latency;
			csec = req[j].asec;
			if (debug) {
				System.out.println("	rotational latency to sector " + req[j].asec + " completed at t = " + t);
			}
			t += req[j].nsec;
			
			csec += req[j].nsec;
			
			while (csec > ns) {
				req[j].asec -= ns;
				csec = req[j].asec;
				req[j].atrk++;
			}

			if (debug) {
				System.out.println("	request completed at t = " + t);
			}
			req[j].t_stop = t;
		}

		display("FCFS");
	}

	public static void sstf() {
		ccyl = 0;
		csec = 0;
		t = 0;

		if (debug) {
			System.out.printf("Debug output for FCFS algorithm\n");
			System.out.printf("=============================\n");
		}

		// store requests in a list
		// ioreq[] aux = new ioreq[nr];
		int[] aux = new int[nr];

		for (int i = 0; i < nr; i++) {
			aux[i] = i;
		}

		int minIndex = 0;

		// There will be "nr" requests to be processed
		for (int i = 0; i < nr; i++) {

			// Loop through the array looking for the shortest time
			int minDistance = Integer.MAX_VALUE;
			for (int j = 0; j < aux.length; j++) {

				if (aux[j] == -1) {
					continue;
				}

				int distance = seek(req[j].acyl);

				if (minDistance == distance) {
					if (req[minIndex].submit > req[j].submit) {
						minDistance = distance;
						minIndex = j;
					}
				}

				if (minDistance > distance) {
					minDistance = distance;
					minIndex = j;
				}
			}

			if (debug) {
				System.out.println("Request " + (minIndex + 1) + " (" + req[minIndex].nsec + " sec @ lba "
						+ req[minIndex].addr + ") start at t = " + t + " (disk @ cyl " + ccyl + ")");

				System.out.println(
						"	chs = " + req[minIndex].acyl + " / " + req[minIndex].atrk + " / " + req[minIndex].asec);
			}

			// seek time
			int seek = seek(req[minIndex].acyl);
			t += seek;
			if (debug) {
				System.out.println("	seek to cylinder " + req[minIndex].acyl + " completed at t = " + t);
			}
			ccyl = req[minIndex].acyl;

			// rotational latency
			int latency = (req[minIndex].asec + ns - csec) % ns;
			t += latency;
			csec = req[minIndex].asec;
			if (debug) {
				System.out.println("	rotational latency to sector " + req[minIndex].asec + " completed at t = " + t);
			}

			// add transfer time
			t += req[minIndex].nsec;

			csec += req[minIndex].nsec;
			// Check for overload of sectors, that is, more sectors than "ns"
			while (csec > ns) {
				req[minIndex].asec -= ns;
				csec = req[minIndex].asec;
				req[minIndex].atrk++;
			}

			if (debug) {
				System.out.println("	request completed at t = " + t);
			}

			// update stop time on original one
			req[minIndex].t_stop = t;

			// Once the request is processed, nullify it
			aux[minIndex] = -1;
		}

		display("SSTF");
	}

	public static void look() {
		ccyl = 0;
		csec = 0;
		t = 0;

		boolean up = true;

		ArrayList<ioreq> upSweep = new ArrayList<ioreq>();
		ArrayList<ioreq> downSweep = new ArrayList<ioreq>();

		// Requests are taken from the upSweep queue
		for (int i = 0; i < nr; i++) {
			upSweep.add(req[i]);
		}

		while (!upSweep.isEmpty() && !downSweep.isEmpty()) {
			if (up) {
				for (int i = 0; i < upSweep.size(); i++) {
					// store the current request
					ioreq current = upSweep.get(i);

					// remove it from the queue since it is being processed
					upSweep.remove(i);

					// request arriving late are not processed yet
					if (current.acyl == ccyl) {
						continue;
					}

					// requests for the same cylinder are added to the other
					// queue
					if (current.acyl < ccyl) {
						downSweep.add(current);
						continue;
					}

					// if all checks, process it

					if (t == 0 && req[0].acyl == 0) {

					} else {
						int temp = seek(current.acyl);
						t += temp;
						ccyl = current.acyl;
					}

					int latency = (current.asec + ns - csec) % ns;
					t += latency;
					csec = current.asec;

					t += current.nsec;
					csec += current.nsec;

					// look for this request and update when it ended

					for (int j = 0; j < req.length; j++) {
						if (current.submit == req[j].submit) {
							req[j].t_stop = t;
						}
					}
				}

			} else {
				for (int i = downSweep.size() - 1; i >= 0; i--) {
					// store the current request
					ioreq current = downSweep.get(i);

					// remove it from the queue since it is being processed
					downSweep.remove(i);

					// request arriving late are not processed yet
					if (current.acyl == ccyl) {
						continue;
					}

					// requests for the same cylinder are added to the other
					// queue
					if (current.acyl > ccyl) {
						upSweep.add(current);
						continue;
					}

					// if all checks, process it

					if (t == 0 && req[0].acyl == 0) {

					} else {
						int temp = seek(current.acyl);
						t += temp;
						ccyl = current.acyl;
					}

					int latency = (current.asec + ns - csec) % ns;
					t += latency;
					csec = current.asec;

					t += current.nsec;
					csec += current.nsec;

					// look for this request and update when it ended

					for (int j = 0; j < req.length; j++) {
						if (current.submit == req[j].submit) {
							req[j].t_stop = t;
						}
					}
				}

			}
			up = !up;

		}

		display("LOOK");
	}

	public static void clook() {
		// display("CLOOK");
	}

	/*------------------------------------------------------*/
	/* Return 1 if the logical disk address 'logaddr' is in */
	/* the range 0 .. nc * nt * ns - 1, or 0 otherwise. */
	/*------------------------------------------------------*/
	static boolean addrok(int logaddr) {
		if (logaddr < 0)
			return false;
		if (logaddr >= nc * nt * ns) {
			return false;
		}
		return true;
	}

	public static void main(String[] args) {

		int temp;
		/* Read input data */
		Scanner sc = new Scanner(System.in);
		nc = sc.nextInt();
		nt = sc.nextInt();
		ns = sc.nextInt();
		s1 = sc.nextInt();
		s2 = sc.nextInt();
		d = sc.nextInt();

		/*-------------------------------*/
		/* Validate the disk parameters. */
		/*-------------------------------*/
		if (nc < 1 || nc > MAXCYL) {
			System.out.printf("Bad # of cylinders; must be 1..%d\n", MAXCYL);
			System.exit(0);
		}
		if (nt < 1 || nt > MAXTRK) {
			System.out.printf("Bad # of tracks per cyl; must be 1..%d\n", MAXTRK);
			System.exit(0);
		}
		if (ns < 1 || ns > MAXSEC) {
			System.out.printf("Bad # of sectors per track; must be 1..%d\n", MAXSEC);
			System.exit(0);
		}
		if (s1 < 1) {
			System.out.printf("Bad 'short' seek time parameter (s1); must be > 0\n");
			System.exit(0);
		}
		if (s2 < 1) {
			System.out.printf("Bad 'long' seek time parameter (s2); must be > 0\n");
			System.exit(0);
		}
		if (d < 1) {
			System.out.printf("Bad seek time selection parameter (d); must be > 0\n");
			System.exit(0);
		}

		nr = sc.nextInt();

		if (nr < 1 || nr > MAXNR) {
			System.out.printf("Bad # of I/O requests; must be 1..%d\n", MAXNR);
		}

		req = new ioreq[nr];

		for (int i = 0; i < nr; i++) {
			int submit = sc.nextInt();
			int addr = sc.nextInt();
			int nsec = sc.nextInt();

			req[i] = new ioreq(submit, addr, nsec);

			if (i > 0 && req[i].submit <= req[i - 1].submit) {
				System.out.printf("Bad request time for I/O request %d.\n", i + 1);
				System.exit(0);
			}
			if (!addrok(req[i].addr)) {
				System.out.printf("Bad starting address for I/O request %d.\n", i + 1);
				System.exit(0);
			}

			if (!addrok(req[i].addr + req[i].nsec - 1)) {
				System.out.println(req[i].addr + req[i].nsec - 1);
				System.out.println(req[i].nsec);
				System.out.printf("Bad ending address for I/O request %d.\n", i + 1);
				System.exit(0);
			}
			if (req[i].nsec < 1) {
				System.out.printf("# sectors too small for I/O request %d.\n", i + 1);
				System.exit(0);
			}

			/*------------------------------------------*/
			/* Compute the CHS address for the request. */
			/*------------------------------------------*/

			req[i].acyl = req[i].addr / (nt * ns); /* cylinder # */
			temp = req[i].addr - req[i].acyl * (nt * ns); /* offset on cyl */
			req[i].atrk = temp / ns; /* track # */
			req[i].asec = temp - req[i].atrk * ns; /* sector # */

			/*----------------------------------------------------------*/
			/* Verify the request doesn't extend past the cylinder end. */
			/*----------------------------------------------------------*/
			if (temp + req[i].nsec > nt * ns) {
				System.out.printf("# sectors too large for I/O request %d.\n", i + 1);
				System.exit(0);
			}
		}

		sc.close();

		if (debug) {
			System.out.println("Disk parameters");
			System.out.println("===============");
			System.out.printf("Number of cylinders (nc):\t\t" + nc + "\n");
			System.out.printf("Tracks/heads per cylinder (nt):\t\t" + nc + "\n");
			System.out.printf("Sectors per track (ns):\t\t\t" + ns + "\n");
			System.out.printf("\"Short\" seek time per cylinder (s1):\t" + s1 + "\n");
			System.out.printf("\"Long\" seek time per cylinder (s2):\t" + s2 + "\n");
			System.out.println();
		}

		if (debug) {
			System.out.println("I/O Requests");
			System.out.println("============");
			for (int i = 0; i < nr; i++) {

				System.out.println(
						"  " + (i + 1) + ". Submitted at t = " + req[i].submit + ", " + req[i].nsec + " sectors at lba "
								+ req[i].addr + " (CHS = " + req[i].acyl + " " + req[i].atrk + " " + req[i].asec + ")");
			}
			System.out.println();
		}

		System.out.println();
		fcfs();
		System.out.println();
		sstf();
		System.out.println();
		look();
		System.out.println();
		clook();
	}
}
