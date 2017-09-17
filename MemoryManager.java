import java.io.*;
import java.util.*;

public class MemoryManager{

    private File MEM_STAT = new File("mem_stat.txt");
    private FileWriter writer;

    int free_frames = 128;
    int jobsDelivered = 0;

    //Frames that are allocated and frames that are free
    LinkedList<Frame> allocated_frame_list = new LinkedList<Frame>();
    LinkedList<Frame> free_frame_table = new LinkedList<Frame>();

    //initialize the FFT
    public void init(int size){
        for(int i = 0; i < size; i++){
            Frame temp = new Frame(i, -1, -1);
            free_frame_table.add(temp);
        }
    }

    //method that returns a free frame when called
    public Frame allocate(){
        return free_frame_table.remove();
    }

    //Method that checks if there is room in memory
    public Boolean admit(int size){
        if((free_frames - size) >= 0){
            free_frames -= size;
            return true;
        }
        else
            return false;
    }

    //release will release the frames that are allocated to a certain job and will also output to the mem_stat file
    public void release(SYSTEM system, int pointer, PCB job, Boolean abnormalTermination){
        //MEM_STAT output:
        if(abnormalTermination){
            try{
                writer.write(String.format("%-50s\n", "ABNORMAL_TERMINATION"));
                writer.flush();
            }catch(Exception ex){}
        }
        else{
            try{
                writer.write(String.format("%-50s\n", "NORMAL_TERMINATION"));
                writer.flush();
            }catch(Exception ex){}
        }
        try{
            writer.write(String.format("%-8s %-8s %-8s %-12s %-12s %-12s %-8s %-8s \n", job.getID(),job.getSizeInBytes(), job.getMaxNumFrames(), job.getInternalFragmentation(), job.getPC(), job.getNumPageFaults(), job.getCleanPagesReplaced(), job.getDirtyPagesReplaced()));
            writer.flush();
        }catch(Exception ex){}

        //free the frames:
        int counter = 0;
        for(int i = 0; i < 128; i++){
            int frame = system.loader.instances.get(pointer).getIndex(4, i);
            if(frame != -1){
                counter++;
                Frame temp = new Frame(frame, -1, -1);
                free_frame_table.add(temp);
            }
        }
        free_frames += counter;
    }

    //called every 4 jobs delivered
    public void stats(PCB job){
        try{
            double fftf = ((double)free_frames)/128.0;
            double aftf = (128.0 - (double)free_frames)/128;
            String a = "" + fftf;
            String b = "" + aftf;
            writer.write(String.format("%-8s %-8s %-8s %-12s %-12s %-12s %-8s %-8s %-8s %-8s \n", "        ","        ","        ", "            ", "            ", "            ", "        ", "        ", a, b));
            writer.flush();
        }catch(Exception ex){}
    }

    //Increment the total number of jobs delivered (For mem_stat statistics)
    public void incrementJobsDelivered(){ jobsDelivered++; }

    //Start the writer for the mem_stat file
    public void startWriter(){
        try{
            writer = new FileWriter(MEM_STAT);
            writer.write(String.format("%-8s %-8s %-8s %-12s %-12s %-12s %-8s %-8s %-8s %-8s \n", "JOB ID","SIZE","FRAMES", "INT. FRAG.", "REF. LENGTH", "PAGE FAULTS", "CLEAN", "DIRTY", "FF/TF", "AF/TF"));
            writer.flush();
        }catch(Exception ex){}
    }
}
