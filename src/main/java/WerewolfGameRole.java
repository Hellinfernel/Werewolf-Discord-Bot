

public abstract class WerewolfGameRole {



    public String name;
    boolean isAlive = true;
    public abstract boolean winningCondition(WerewolfGame game);


    public abstract void winningSequence();
  //  public ArrayList<WerewolfEvent> dayEvent;
   // public ArrayList<WerewolfEvent> nightEvent;


}
