import java.io.*;
import java.util.*;

public class Frame{

    //One frame of size 256 bytes that can hold one page
    private int frameNumber;
    private int jobID;
    private int pageNumber;

    //Frame constructor
    public Frame(int frameNumber, int jobID, int pageNumber){
        this.frameNumber = frameNumber;
        this.jobID = jobID;
        this.pageNumber = pageNumber;
    }

    //Accessors
    public int getFrameNum(){return frameNumber; }
    public void setJobID(int id){jobID = id;}
    public void setPageNumber(int pn){pageNumber = pn;}

}
