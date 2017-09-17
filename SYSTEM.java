import java.util.*;
import java.io.*;

public class SYSTEM{
    /*
        PATRICK GODDARD
        CS 4323
        Operating Systems Program Phase II
        April 26th, 2017

        Global Variables:
            Within Class SYSTEM:
                system: The current specific instance of SYSTEM in which all class instances communicate through
                loader: The current instance of loader, which loads file from the arrivals file and stores them on disk and calls the scheduler
                mem_manager: The current instance of memory manager, which handles frame allocation and deallocation and whether jobs can be loaded into memory
                scheduler: The current instance of Scheduler, which handles the MLFQ, Blocked Queue, and handling process movement through the four subqueues
                processor: The current instance of CPU which handles running the current process, and returning status codes which tell the System what method of the Scheduler to call
                clock: The current instance of Clock, which handles the global system time and is incremented only by CPU run time or idle time
                replacer: The current instance of the page Replacer, which handles finding a victim page
            Within Class Scheduler:
                currentJob: Holds the current PCB of the job that is running
                quantum: Hols the current allowed time slice for the job that is currently running
                pageNumberAtFault: Holds the current page number that caused a fault in the CPU
                subqueue1: SubQueue1 of the MLFQ
                subqueue2: SubQueue2 of the MLFQ
                subqueue3: SubQueue3 of the MLFQ
                subqueue4: SubQueue4 of the MLFQ
                blockedQueue: The blockedQueue which holds jobs that are waiting in I/O
            Within Class Loader:
                jobQueue: Serves as 'Disk' for jobs that are unable to be currently loaded into memory
            Within Class MemoryManager:
                jobsDelivered: Total number of jobs terminated wither normally or abnormally
                free_frames: Number of free frames available
                allocated_frame_list: frames which are currently allocated_frame_list
                free_frame_table: frames that are not currently allocated and that are available

    */
    private static int pageNumberAtFault;
    private int quantum;
    private boolean finalBurst;
    private boolean demotionAfterQuantum;
    Loader loader;
    MemoryManager mem_manager;
    Scheduler scheduler;
    CPU processor;
    SystemClock clock;
    Replacer replacer;
    PageTable page_table;

    //Constructor for a new instance of SYSTEM, which has all of the necessary components
    public SYSTEM(Loader loader, MemoryManager mem_manager, Scheduler scheduler, CPU processor, SystemClock clock, Replacer replacer){
        this.loader = loader;
        this.mem_manager = mem_manager;
        this.scheduler = scheduler;
        this.processor = processor;
        this.clock = clock;
        this.replacer = replacer;
    }

    public static void main(String args[]){
        //Create instances of each of the System parts
        Replacer replacer = new Replacer();
        SystemClock clock = new SystemClock(0);
        Loader loader = new Loader(args[0]);
        MemoryManager mem_manager = new MemoryManager();
        Scheduler scheduler = new Scheduler();
        CPU processor = new CPU();

        //Put all of the instances into a constructor to build an abstract 'System'
        SYSTEM system = new SYSTEM(loader, mem_manager, scheduler, processor, clock, replacer);

        //Pass in 128 for 128 total frames in the system
        mem_manager.init(128);

        //Instantiate the FileWrite for the TRACE_FILE file
        replacer.startWriter();

        //Instantiate the FileWrite for the MEM_STAT file
        mem_manager.startWriter();

        //Initially load as many jobs as possible into memory
        loader.load(system);

        /*
            Use an inifite loop that will only terminate when all jobs are complete
            i.e: No jobs left in the arrivals file, no jobs in the subqueues, no jobs in the JobQueue
        */
        while(true){

            //Call the scheduler to decide which job will be ran next
            scheduler.dispatch(system);

            //Prepare to receive a return status after executing a job
            int returnStatus = 0;

            //Load the job onto the processor if there is a job ready, expecting a return status value for when control returns to the system
            //If there is not a job ready (i.e all jobs in blockedQueue), then have the processor remain idle
            if(scheduler.currentJob != null)
                returnStatus = processor.run(system, scheduler.quantum, scheduler.currentJob.getDemotion());
            else
                processor.idle(system);

            //Have scheduler check the Blocked Queue to see if any jobs have completed their respective I/O before we handle the return
            scheduler.checkBlockedQueue(system);

            //If the status of the job upon completion is a request for I/O then have the scheduler add the process to the Blocked Queue
            //Reset number of turns if the I/O request was in the time slice
            if(returnStatus == 1){
                scheduler.blocked(system, true);
                scheduler.currentJob = null;
            }

            //If the status of the job upon completion is a request for I/O then have the scheduler add the process to the Blocked Queue
            //Do not reset number of turns since the I/O request was at the end of the time slice
            else if(returnStatus == 2){
                scheduler.blocked(system, false);
                scheduler.currentJob = null;
            }

            //No I/O, but the job used all of its turns
            else if(returnStatus == 3){
                scheduler.demoteAndReadyUp();
            }

            //No I/O, job has not used all of its turns
            else if(returnStatus == 4){
                scheduler.readyUp();
            }
            //Job has encountered a Page Fault
            else if(returnStatus == 5){
                int pageTableBaseAddress = scheduler.currentJob.getPTBA();
                //Make sure the page is valid (we check for invalidity in the CPU class)
                if(loader.instances.get(pageTableBaseAddress).getIndex(0, pageNumberAtFault) == 1){
                    //If the job has filled all of its frames then replace a page, otherwise just load one into a free frame
                    if(scheduler.currentJob.getMaxNumFrames() == scheduler.currentJob.getAllocatedNumFrames()){
                        int pn = replacer.findPage(system, pageTableBaseAddress, pageNumberAtFault);
                        loader.loadPageReplacer(system, scheduler.currentJob, pageTableBaseAddress, pn, pageNumberAtFault);
                    }
                    else
                        loader.loadNewPage(system, scheduler.currentJob, pageTableBaseAddress, pageNumberAtFault);
                }
                //block the page to finish I/O
                scheduler.blocked(system, false);
                scheduler.currentJob = null;
            }

            //If the status of the job upon completion is NORMAL TERMINATION then print statistics and release memory. Call the loader to load the next job to use the now free memory. Also decrement the active PCB number, increase number of jobs delivered
            else if(returnStatus == 6){
                mem_manager.incrementJobsDelivered();
                long exitTime = system.clock.getTime();
                scheduler.decrementPCBNum();
                mem_manager.release(system, scheduler.currentJob.getPTBA(), scheduler.currentJob, false);
                if(mem_manager.jobsDelivered % 4 == 0)
                    mem_manager.stats(scheduler.currentJob);
                scheduler.currentJob = null;
                loader.load(system);
            }

            //If the status of the job upon completion is ABNORMAL TERMINATION then print statistics and release memory. Call the loader to load the next job to use the now free memory. Also decrement the active PCB number, increase number of jobs delivered
            else if(returnStatus == 7){
                mem_manager.incrementJobsDelivered();
                long exitTime = system.clock.getTime();
                scheduler.decrementPCBNum();
                mem_manager.release(system, scheduler.currentJob.getPTBA(), scheduler.currentJob, true);
                if(mem_manager.jobsDelivered % 4 == 0)
                    mem_manager.stats(scheduler.currentJob);
                scheduler.currentJob = null;
                loader.load(system);
            }

            //Call the scheduler to see if all subqueues, the job queue, and the blocked queue are empty
            scheduler.checkForSystemCompletion(system);
        }
    }

    //CPU uses this to pass back which page caused the page fault
    public void setPageNumberAtFault(int page_number){
        pageNumberAtFault = page_number;
    }
}
