import java.io.*;
import java.util.*;

public class PageTable{

    int jobID;
    int[][] array;

    //Construct a new page table for a job
    public PageTable(int jobID, int numPages){
        array = new int[5][128];
        for(int i = 0; i < 128; i++){
                array[4][i] = -1;
        }
        for(int i = 0; i < numPages; i++)
            array[0][i] = 1;
    }

    //return value at certain index in the page table
    public int getIndex(int r, int c){
        return array[r][c];
    }

    //update value at certain index in the page table
    public void setIndexAt(int r, int c, int value){
        array[r][c] = value;
    }
}
