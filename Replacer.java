import java.util.*;
import java.io.*;

public class Replacer{
    private File TRACE_FILE = new File("trace_file.txt");
    private FileWriter writer;

    //start the writer for trace_file
    public void startWriter(){
        try{
            writer = new FileWriter(TRACE_FILE);
            writer.write(String.format("%-15s %-15s %-15s %-15s %-15s \n", "JOB ID","REPLACED PAGE","PLACED PAGE", "FRAME", "STATUS"));
            writer.flush();
        }catch(Exception ex){}
    }

    //This method finds a replacement page if a victim needs to be selected
    public int findPage(SYSTEM system,  int pageTableBaseAddress, int placedPage){

        int[] pagePreference = new int[128];
        Boolean found = false;

        //build an array of replacement preference for all of the resident pages within a job
        for(int i = 0; i < 128; i++){
            // Not referenced and Not Modified
            if(system.loader.instances.get(pageTableBaseAddress).getIndex(0, i) == 1 && system.loader.instances.get(pageTableBaseAddress).getIndex(1, i) == 1 && system.loader.instances.get(pageTableBaseAddress).getIndex(2, i) == 0 && system.loader.instances.get(pageTableBaseAddress).getIndex(3, i) == 0)
                pagePreference[i] = 1;
            //Not Referenced and Modified
            else if(system.loader.instances.get(pageTableBaseAddress).getIndex(0, i) == 1 && system.loader.instances.get(pageTableBaseAddress).getIndex(1, i) == 1 && system.loader.instances.get(pageTableBaseAddress).getIndex(2, i) == 0 && system.loader.instances.get(pageTableBaseAddress).getIndex(3, i)  == 1)
                pagePreference[i] = 2;
            //Referenced and Not Modified
            else if(system.loader.instances.get(pageTableBaseAddress).getIndex(0, i) == 1 && system.loader.instances.get(pageTableBaseAddress).getIndex(1, i) == 1 && system.loader.instances.get(pageTableBaseAddress).getIndex(2, i) == 1 && system.loader.instances.get(pageTableBaseAddress).getIndex(3, i)  == 0)
                pagePreference[i] = 3;
            //Referenced and Modified
            else if(system.loader.instances.get(pageTableBaseAddress).getIndex(0, i) == 1 && system.loader.instances.get(pageTableBaseAddress).getIndex(1, i) == 1 && system.loader.instances.get(pageTableBaseAddress).getIndex(2, i) == 1 && system.loader.instances.get(pageTableBaseAddress).getIndex(3, i)  == 1)
                pagePreference[i] = 4;
        }

        String job_id = "" + system.scheduler.currentJob.getID();

        //If we find a Not Referenced, Not Modified page: choose as victim
        for(int i = 0; i < 128; i++){
            if(pagePreference[i] == 1){
                system.scheduler.currentJob.incrementCleanPagesReplaced();
                try{
                    writer.write(String.format("%-15s %-15s %-15s %-15s %-15s \n", job_id, i, placedPage, system.loader.instances.get(system.scheduler.currentJob.getPTBA()).getIndex(4, i), "NR:NM"));
                    writer.flush();
                }catch(Exception ex){}
                return i;
            }
        }

        //If we find a Not Referenced, Modified page: choose as victim
        for(int i = 0; i < 128; i++){
            if(pagePreference[i] == 2){
                system.scheduler.currentJob.incrementDirtyPagesReplaced();
                try{
                    writer.write(String.format("%-15s %-15s %-15s %-15s %-15s \n", job_id, i, placedPage, system.loader.instances.get(system.scheduler.currentJob.getPTBA()).getIndex(4, i), "NR:M"));
                    writer.flush();
                }catch(Exception ex){}
                return i;
            }
        }

        //If we find a Referenced, Not Modified page: choose as victim
        for(int i = 0; i < 128; i++){
            if(pagePreference[i] == 3){
                system.scheduler.currentJob.incrementCleanPagesReplaced();
                try{
                    writer.write(String.format("%-15s %-15s %-15s %-15s %-15s \n", job_id, i, placedPage, system.loader.instances.get(system.scheduler.currentJob.getPTBA()).getIndex(4, i), "R:NM"));
                    writer.flush();
                }catch(Exception ex){}
                return i;
            }
        }

        //If we find a Referenced, Modified page: choose as victim
        for(int i = 0; i < 128; i++){
            if(pagePreference[i] == 4){
                system.scheduler.currentJob.incrementDirtyPagesReplaced();
                try{
                    writer.write(String.format("%-15s %-15s %-15s %-15s %-15s \n", job_id, i, placedPage, system.loader.instances.get(system.scheduler.currentJob.getPTBA()).getIndex(4, i), "R:M"));
                    writer.flush();
                }catch(Exception ex){}
                return i;
            }
        }

        //error statement
        return -1;
    }

}
