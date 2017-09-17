public class PCB{

    private int jobID;
    //Holds what line we are at in the reference string
    private int programCounter;
    //Holds the job** reference file path
    private String refString;
    //job size in bytes
    private int sizeInBytes;
    //job size in pages
    private int sizeInPages;
    //starting address of the page table
    private int pageTableBaseAddress;
    //total number of page faults encountered
    private int numPageFaults;
    //number of pages we replace
    private int numOfPageReplacements;
    //amount of internal fragmentation
    private int internalFragmentation;
    //Maximum number of frames the job can holes (sizeInPages/4)
    private int maxNumOfFrames;
    //Current number of frames the job holds
    private int allocatedNumOfFrames;
    //Whether the job has used all of its turns
    private Boolean demotion;
    private int currentSubqueue;
    private int timesInSubqueue;
    private int cleanPagesReplaced;
    private int dirtyPagesReplaced;
    //Used to calculate when to take the job out of the blocked queue
    private long completionTimeOfIO;

    //Contrustor for a PCB holding all of the variables listed above
    public PCB(int jobID, String refString, int sizeInBytes, int sizeInPages, int pageTableBaseAddress, int numPageFaults, int numOfPageReplacements,
               int internalFragmentation, int maxNumOfFrames, int allocatedNumOfFrames, Boolean demotion, int programCounter, int currentSubqueue,
               int timesInSubqueue, int cleanPagesReplaced, int dirtyPagesReplaced, long completionTimeOfIO){
        this.jobID = jobID;
        this.refString = refString;
        this.sizeInBytes = sizeInBytes;
        this.sizeInPages = sizeInPages;
        this.pageTableBaseAddress = pageTableBaseAddress;
        this.numPageFaults = numPageFaults;
        this.numOfPageReplacements = numOfPageReplacements;
        this.internalFragmentation = internalFragmentation;
        this.maxNumOfFrames = maxNumOfFrames;
        this.allocatedNumOfFrames = allocatedNumOfFrames;
        this.demotion = demotion;
        this.programCounter = programCounter;
        this.currentSubqueue = currentSubqueue;
        this.timesInSubqueue = timesInSubqueue;
        this.cleanPagesReplaced = cleanPagesReplaced;
        this.dirtyPagesReplaced = dirtyPagesReplaced;
        this.completionTimeOfIO = completionTimeOfIO;
    }

    /*
        The following methods are accessors and setters/incrementors for the variables that are listed above.
        Since some variables are only set once (in the constructor), some variables only have a 'get' method. (i.e: the job ID never changes)
    */
    
    public int getID(){return jobID;}

    public Boolean getDemotion(){ return demotion; }
    public void setDemotion(Boolean b){ demotion = b; }

    public void incrementPC(){ programCounter++; }
    public int getPC(){ return programCounter;}

    public String getRefString(){return refString;}

    public int getPTBA(){return pageTableBaseAddress;}

    public int getMaxNumFrames(){ return maxNumOfFrames;}
    public int getAllocatedNumFrames(){ return allocatedNumOfFrames;}

    public int getCurrentSubqueue() {return currentSubqueue;}
    public void setCurrentSubqueue(int i){ currentSubqueue = i;}

    public int getTimesInSubqueue(){ return timesInSubqueue; }
    public void setTimesInSubqueue(int i){ timesInSubqueue = i; }

    public void incrementAllocatedNumOfFrames(){ allocatedNumOfFrames++; }

    public long getCompletionTimeOfIO(){ return completionTimeOfIO;}
    public void setCompletionTimeOfIO(long i){ completionTimeOfIO = i; }

    public int getSizeInBytes(){return sizeInBytes; }
    public int getInternalFragmentation(){return internalFragmentation; }

    public void incrementNumPageFaults(){ numPageFaults++; }
    public int getNumPageFaults(){ return numPageFaults; }

    public void incrementCleanPagesReplaced(){ cleanPagesReplaced++; }
    public int getCleanPagesReplaced(){ return cleanPagesReplaced; }

    public void incrementDirtyPagesReplaced(){ dirtyPagesReplaced++; }
    public int getDirtyPagesReplaced(){ return dirtyPagesReplaced; }
}
