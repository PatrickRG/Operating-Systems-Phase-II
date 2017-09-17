public class SystemClock{

    //holds the current time of the system

    private long currentTime;
    public long timeSpentIdle;

    public SystemClock(long currentTime){
        this.currentTime = currentTime;
    }

    //Increment clock method that the CPU will wall during each iteration
    public void incrementClock(){ currentTime += 1; timeSpentIdle++;}
    public void doubleIncrementClock(){ currentTime = currentTime + 2; }

    //Return the current system time
    public long getTime(){ return currentTime; }
}
