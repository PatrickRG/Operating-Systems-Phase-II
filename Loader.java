import java.util.*;
import java.io.*;

public class Loader{

    //File Reader to parse the arrivals file

    private BufferedReader fileReader;
    LinkedList<String[]> jobQueue = new LinkedList<String[]>();
    LinkedList<PageTable> instances = new LinkedList<PageTable>();
    String filePathway;

    //Create a loader around an arrivals that is inputted into the Loader constructor

    public Loader(String fileName){
        try{
            filePathway = fileName;
            this.fileReader = new BufferedReader(new FileReader(fileName + "/arrivals"));
        } catch(Exception ex) {System.out.println("Could not find data file.");}
    }

    /*
        This method is called when jobs need to be loaded from the inbound jobs (arrivals file)
        i.e:
            1. The system was just started and initial jobs need to be loaded
            2. The JobQueue is empty and more need to be loaded into memory or the job queue
    */
    public void load(SYSTEM system){
        boolean finished = false;
        boolean lookAhead = false;
        //After not admitting a job, the counter will allow the loader to look for another job up to 5 iterations (breaking if 0 is encountered)
        int counter = 5;
        while(counter > 0){
            try{
                String[] split = {};
                String newS = "";
                String s = "";
                //Only pull jobs from disk if job queue is empty or if we are looking ahead after not admitting
                if(jobQueue.peek() == null || lookAhead){
                        s = fileReader.readLine();
                        newS = s.trim();
                        split = newS.split("\\s+");
                        if(Integer.parseInt(split[0]) == 0)
                            break;
                }

                //Pull from the Job Queue since it is not empty
                else{
                    split = jobQueue.remove();
                    if(Integer.parseInt(split[0]) == 0)
                        break;
                }
                int jobID = Integer.parseInt(split[0]);
                int sizeInBytes = Integer.parseInt(split[1]);
                //pull the reference string jb**
                String refString = split[2];

                //Since each page is 256 we can get the size in pages as follows:
                int sizeInPages = (int)Math.ceil((double)sizeInBytes/256.0);
                //Calculate internal fragmentation
                int internalFragmentation = 256 - (sizeInBytes % 256);
                //Number of frames a job gets is (number of pages/4)
                int numFrames = (int)Math.ceil((double)sizeInPages/4.0);

                //check if there are enough frames to allocate the space for this job and also a free PCB slot
                if((system.mem_manager.admit(numFrames) == true) && system.scheduler.numPCBs() < 15){
                    //System.out.println("We are admitting job " + jobID + "\n\n\n\n");
                    PageTable pt = new PageTable(jobID, sizeInPages);
                    instances.add(pt);
                    int pageTableBaseAddress = instances.indexOf(pt);
                    system.scheduler.setup(system, jobID, sizeInBytes, sizeInPages, refString, internalFragmentation, numFrames, pageTableBaseAddress);
                }
                else{
                    jobQueue.addFirst(split);
                    //We failed to admit due to size, so consider other jobs
                    if(!lookAhead)
                        lookAhead = true;
                    counter--;
                }
            }catch(Exception ex){break;}
        }
    }

    //Replacing a victim page after the replacer found the victim
    public void loadPageReplacer(SYSTEM system, PCB job, int pageTableBaseAddress, int pn, int pageNumber){
        //Retrieve replacement frame
        int replacementFrame = instances.get(system.scheduler.currentJob.getPTBA()).getIndex(4, pn);
        //remove resident bits and modified bits of victim page (writing back)
        instances.get(pageTableBaseAddress).setIndexAt(0, pn, 1);
        instances.get(pageTableBaseAddress).setIndexAt(1, pn, 0);
        instances.get(pageTableBaseAddress).setIndexAt(2, pn, 0);
        instances.get(pageTableBaseAddress).setIndexAt(3, pn, 0);
        instances.get(pageTableBaseAddress).setIndexAt(4, pn, -1);
        //update page that is placed (valid and resident now)
        instances.get(pageTableBaseAddress).setIndexAt(0, pageNumber, 1);
        instances.get(pageTableBaseAddress).setIndexAt(1, pageNumber, 1);
        instances.get(pageTableBaseAddress).setIndexAt(2, pageNumber, 0);
        instances.get(pageTableBaseAddress).setIndexAt(3, pageNumber, 0);
        instances.get(pageTableBaseAddress).setIndexAt(4, pageNumber, replacementFrame);
        //Update reference bits
        for(int i = 0; i < 128; i ++)
            instances.get(pageTableBaseAddress).setIndexAt(2, i, 0);
    }

    //load a page into a frame that is not currently occupied
    public void loadNewPage(SYSTEM system, PCB job, int pageTableBaseAddress, int pageNumber){
        //Get a new frame
        Frame toBeAllocated = system.mem_manager.allocate();
        system.scheduler.currentJob.incrementAllocatedNumOfFrames();
        //update page that is placed (valid and resident now)
        instances.get(pageTableBaseAddress).setIndexAt(0, pageNumber, 1);
        instances.get(pageTableBaseAddress).setIndexAt(1, pageNumber, 1);
        instances.get(pageTableBaseAddress).setIndexAt(2, pageNumber, 0);
        instances.get(pageTableBaseAddress).setIndexAt(3, pageNumber, 0);
        instances.get(pageTableBaseAddress).setIndexAt(4, pageNumber, toBeAllocated.getFrameNum());
        //Update reference bits
        for(int i = 0; i < 128; i ++)
            instances.get(pageTableBaseAddress).setIndexAt(2, i, 0);

    }
}
