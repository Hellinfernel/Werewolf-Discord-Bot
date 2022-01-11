import discord4j.core.object.entity.Member;

public abstract class WerewolfGameRole {

    public Member player;
    boolean isAlive = true;
    public abstract boolean winningCondition(WerewolfGame game);


    public abstract void winningSequence();
  //  public ArrayList<WerewolfEvent> dayEvent;
   // public ArrayList<WerewolfEvent> nightEvent;


}
