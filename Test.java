import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.*;
public class Test{

    public static void main(String[] args){

        for(int i = 0; i < 2000; i++){
            try{
                String line = Files.readAllLines(Paths.get("/home/opsys/OS-I/SP17/jb01")).get(i);
                System.out.println(line);
            }catch(Exception ex){}
        }
    }
}
