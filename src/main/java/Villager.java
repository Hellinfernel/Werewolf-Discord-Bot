import discord4j.core.object.entity.Member;

public class Villager extends WerewolfGameRole{

    public Villager(){
        this.player = player;
    }

    @Override
    public boolean winningCondition(WerewolfGame game) {
        if (game.alivePlayers().stream()
                .filter(player -> player.getClass()
                        .isAssignableFrom(Werewolf.class))
                .findFirst()
                .isPresent()){
            return false;
        }
        return true;
    }

    @Override
    public void winningSequence() {


    }
}
