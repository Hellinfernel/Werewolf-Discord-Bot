

public class Werewolf extends WerewolfGameRole {

    public Werewolf(){

    }

    @Override
    public boolean winningCondition(WerewolfGame game) {
        int werewolfNumber = (int) game.playerList.stream()
                .filter(player -> player
                        .getClass()
                        .isAssignableFrom(Werewolf.class))
                .count();
        if (werewolfNumber > game.alivePlayers().size() - werewolfNumber){
            return true;

        }
        return false;
    }

    @Override
    public void winningSequence() {

    }
}
