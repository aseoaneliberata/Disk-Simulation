import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

public class prog3 {

	private static boolean debug = false;

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
		t = req[0].submit;

		while (t % ns != 0) {
			t++;
		}

		if (debug) {
			System.out.printf("Debug output for FCFS algorithm\n");
			System.out.printf("=============================\n");
		}

		for (int j = 0; j < nr; j++) {
			if (debug) {
				System.out.println("Request " + (j + 1) + " (" + req[j].nsec + " sec @ lba " + req[j].addr
						+ ") start at t = " + t + " (disk @ cyl " + ccyl + ")");
			}

			// System.out.println(" chs = " + req[j].acyl + " / " + req[j].atrk
			// + " / " + req[j].asec);

			// if (t == 0 && req[0].acyl == 0) {

			// } else {
			int temp = seek(req[j].acyl);
			t += temp;
			// System.out.println(" seek to cylinder " + req[j].acyl + "
			// completed at t = " + t);
			ccyl = req[j].acyl;
			// }

			int latency = (req[j].asec + ns - csec) % ns;
			t += latency;
			csec = req[j].asec;

			System.out.println("	rotational latency to sector " + req[j].asec + " completed at t = " + t);

			t += req[j].nsec;
			csec += req[j].nsec;

			// System.out.println(" request completed at t = " + t);
			req[j].t_stop = t;
		}

		display("FCFS");
	}

	public static void sstf() {
		ccyl = 0;
		csec = 0;
		t = req[0].submit;

		while (t % ns != 0) {
			t++;
		}

		if (debug) {
			System.out.printf("Debug output for SSTF algorithm\n");
			System.out.printf("=============================\n");
		}

		// Keep track of the processed requests with an array of ints that store
		// the index of each process, and then -1 after the request being
		// processed
		for (int i = 0; i < nr; i++) {
			req[i].done = false;
		}

		// minIndex doesn't have to be initialized every loop
		int minIndex = 0;

		// We want to process all the requests, so loop "nr" times
		for (int i = 0; i < nr; i++) {

			// Loop through the array looking for the shortest time
			// Initialize the minimum distance to the max value of integer
			int minDistance = Integer.MAX_VALUE;

			// double loop... avoid
			for (int j = 0; j < req.length; j++) {

				// if the request was already processed, jump to the next one
				if (req[j].done) {
					continue;
				}

				// calculates and stores the seek time
				int distance = seek(req[j].acyl);

				// in case that the distance is the same, the first one goes
				// first since it is the one waiting the longest.s
				if (minDistance == distance) {
					// if (req[minIndex].submit > req[j].submit) {
					// minDistance = distance;
					// minIndex = j;
					// }
				}

				// if the current request has a seek time smaller than what the
				// program thought that it was the minimum, save it
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

			if (debug) {
				System.out.println("	request completed at t = " + t);
			}

			// update stop time on original one
			req[minIndex].t_stop = t;

			// Once the request is processed, nullify it
			req[minIndex].done = true;
		}
		display("SSTF");
	}

	public static void look() {
		ccyl = 0;
		csec = 0;
		t = req[0].submit;

		/*
		 * while(t % ns != 0){ t++; }
		 */

		boolean upsweep = true;

		ArrayList<ioreq> Sweep = new ArrayList<ioreq>();

		for (int i = 0; i < nr; i++) {
			Sweep.add(req[i]);
		}

		while (!Sweep.isEmpty()) {

			for (int i = 0; i < Sweep.size(); i++) {
				ioreq current = Sweep.get(i);
				if (upsweep && current.acyl > ccyl || !upsweep && current.acyl < ccyl) {
					System.out.printf("The value of the cylinder is %d\n", current.acyl);
					System.out.printf("The size of the array before sweep removal is %d\n", Sweep.size());
					Sweep.remove(i);
					System.out.printf("The size of the array after sweep removal is %d\n", Sweep.size());

					if (t < req[i].submit) {
						t = req[i].submit;
					}

					int temp = seek(current.acyl);
					System.out.printf("The value of t is %d\n", t);
					ccyl = current.acyl;
					csec = t % ns;

					int latency = (current.asec + ns - csec) % ns;
					System.out.printf("latency is %d\n", latency);
					System.out.printf("The value of t is %d\n", t);

					t += current.nsec + temp + latency;

					// if ((i < nr - 1) && (t < req[i + 1].submit)) {
					// t = req[i].submit;
					// }

					for (int j = 0; j < req.length; j++) {
						if (current.submit == req[j].submit) {
							req[j].t_stop = t;
							break;
						}
					}
				} else {
					upsweep = !upsweep;
				}
			}
		}

		display("LOOK");
	}

	public static void clook() {

		ccyl = 0;
		csec = 0;
		t = req[0].submit;

		// initialize all to done = false;
		for (int i = 0; i < req.length; i++) {
			req[i].done = false;
			req[i].now = false;
		}

		// bubble sort (1st - cylinders, 2nd - submit)
		boolean swapped = true;
		int j = 0;
		ioreq tmp;
		while (swapped) {
			swapped = false;
			j++;
			for (int i = 0; i < req.length - j; i++) {
				if (req[i].acyl > req[i + 1].acyl) {
					tmp = req[i];
					req[i] = req[i + 1];
					req[i + 1] = tmp;
					swapped = true;
				} else if (req[i].acyl == req[i + 1].acyl) {
					if (req[i].submit > req[i + 1].submit) {
						tmp = req[i];
						req[i] = req[i + 1];
						req[i + 1] = tmp;
						swapped = true;
					}
				}
			}
		}

		int completed = 0;

		req[0].now = true;

		while (completed < nr) {
			boolean skip = false;
			for (int i = 0; i < req.length; i++) {
				// deals with the first case and a possible queue (those
				// assigned with now)
				if (req[i].done == false && req[i].now == true && req[i].acyl >= ccyl) {

					if (t < req[i].submit) {
						t = req[i].submit;
					}

					int temp = seek(req[i].acyl);
					t += temp;
					ccyl = req[i].acyl;

					int latency = (req[i].asec + ns - csec) % ns;
					t += latency;
					csec = req[i].asec;

					t += req[i].nsec;
					csec += req[i].nsec;

					req[i].t_stop = t;
					req[i].done = true;
					req[i].now = false;

					skip = true;
					completed++;
				}

				// if there is no "now" queue or nothing gets selected, the next
				// request that arrives
				// is processed

				if (skip) {
					continue;
				}

				for (int k = 0; k < req.length; k++) {
					if (!req[k].done && req[k].submit < t) {
						req[k].now = true;
						break;
					}
				}
				
				
			}
		}

		display("CLOOK");
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
		// look();
		System.out.println();
		clook();
	}
}
