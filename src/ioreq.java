
public class ioreq {
	/*------------*/
	/* Input data */
	/*------------*/
	public int submit; /* time when request was submittted */
	public int addr; /* logical disk address */
	public int nsec; /* # of sectors to process */

	/*--------------*/
	/* Derived data */
	/*--------------*/
	public int acyl, atrk, asec; /* physical address of first sector */

	/*--------------------*/
	/* Simulation results */
	/*--------------------*/
	public int t_stop; /* time when request was completed */

	/*-------*/
	/* Flags */
	/*-------*/
	public int done; /* for all but FCFS, 0 if request is active */
	public int now; /* for LOOK and C-LOOK */
	
	public ioreq (int submit, int addr, int nsec) {
		this.submit = submit;
		this.addr = addr;
		this.nsec = nsec;
	}
}
