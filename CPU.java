import java.nio.file.*;
import java.nio.charset.*;

public class CPU{

    //The following variables are return statuses for the possible options of jobs after execution
    //ERROR CODE
    private final int ERROR = -1;
    //POSSIBLE STATUS CODES
    private final int REQUEST_IN_QUANTUM = 1;
    private final int REQUEST_AT_END_OF_QUANTUM = 2;
    private final int QUANTUM_EXPIRED_DEMOTE = 3;
    private final int QUANTUM_EXPIRED_NO_DEMOTE = 4;
    private final int PAGE_FAULT = 5;
    private final int TERMINATED = 6;
    private final int ABNORMAL_TERMINATION = 7;

    //Method that simulates one CPU cycle
    public int run(SYSTEM system, int timeQuantum, boolean demotion){
        //Increment times in subqueue
        system.scheduler.currentJob.setTimesInSubqueue(system.scheduler.currentJob.getTimesInSubqueue() + 1);

        //Boolean values to determine status codes
        boolean endOfFile = false;
        boolean abnormalTermination = false;
        boolean ioRequestAtEndQuantum = false;
        boolean ioRequestInQuantum = false;
        boolean pageFault = false;
        //quantum counter
        int counter = 0;
        //Iterate up to the current allowed time quantum
        while(counter < timeQuantum){
            String line;
            int programCounter = system.scheduler.currentJob.getPC();

            //Open up the reference string and get the entry at line programCounter
            try{
                line = Files.readAllLines(Paths.get(system.loader.filePathway + "/" + system.scheduler.currentJob.getRefString())).get(programCounter);
            }catch(Exception ex){endOfFile = true; break;}

            //split the return string to get the 'p|w|r' and page number
            int length = line.length();
            char reference_code = line.charAt(0);
            int page_number = Integer.parseInt(line.substring(1));
            //If the page referenced is invalid: ABNORMAL TERMINATION
            if(system.loader.instances.get(system.scheduler.currentJob.getPTBA()).getIndex(0, page_number) == 0)
                abnormalTermination = true;

            //If reference code is P
            if(reference_code == 'p'){
                //See if the page is resident, if it is then increment the clock by 2 VTUs and update the reference bit
                if(system.loader.instances.get(system.scheduler.currentJob.getPTBA()).getIndex(1, page_number) == 1){
                    system.clock.doubleIncrementClock();
                    system.loader.instances.get(system.scheduler.currentJob.getPTBA()).setIndexAt(2, page_number, 1);
                    //Increment the line pointer to the next instruction
                    system.scheduler.currentJob.incrementPC();
                }
                //If the page is not resident the fault and let the replacer handle it
                else if(system.loader.instances.get(system.scheduler.currentJob.getPTBA()).getIndex(1, page_number) == 0){
                    system.scheduler.currentJob.incrementNumPageFaults();
                    pageFault = true;
                    system.setPageNumberAtFault(page_number);
                    break;
                }
                //Backup method to fault regardless
                else{
                    system.scheduler.currentJob.incrementNumPageFaults();
                    pageFault = true;
                    system.setPageNumberAtFault(page_number);
                    break;
                }
            }

            //If reference code is R
            else if(reference_code == 'r'){
                //Increment clock and check when I/O request is made
                system.clock.doubleIncrementClock();
                if(counter + 2 >= timeQuantum)
                    ioRequestAtEndQuantum = true;
                else
                    ioRequestInQuantum = true;
                //Update referenced bit
                system.loader.instances.get(system.scheduler.currentJob.getPTBA()).setIndexAt(2, page_number, 1);
                system.scheduler.currentJob.incrementPC();
                break;
            }

            //If reference code is W
            else if(reference_code == 'w'){
                //Increment clock and check when I/O request is made
                system.clock.doubleIncrementClock();
                if(counter + 2 >= timeQuantum)
                    ioRequestAtEndQuantum = true;
                else
                    ioRequestInQuantum = true;
                //Update modified bit
                system.loader.instances.get(system.scheduler.currentJob.getPTBA()).setIndexAt(3, page_number, 1);
                system.scheduler.currentJob.incrementPC();
                break;
            }

            counter+=2;
        }

        //Return a status for the job back to the system to take the appropriate scheduling action

        if(abnormalTermination)
            return ABNORMAL_TERMINATION;
        else if(pageFault)
            return PAGE_FAULT;
        else if(endOfFile)
            return TERMINATED;
        else if(!ioRequestAtEndQuantum && !ioRequestInQuantum && !demotion)
            return QUANTUM_EXPIRED_NO_DEMOTE;
        else if(!ioRequestAtEndQuantum && !ioRequestInQuantum && demotion)
            return QUANTUM_EXPIRED_DEMOTE;
        else if(ioRequestInQuantum)
            return REQUEST_IN_QUANTUM;
        else if(ioRequestAtEndQuantum)
            return REQUEST_AT_END_OF_QUANTUM;
        //THIS SHOULD NEVER BE RETURNED, INDICATED AN ERROR
        else
            return ERROR;
    }

    //This is called by SYSTEM if the CPU is idle
    public void idle(SYSTEM system){
        system.clock.incrementClock();
    }
}
