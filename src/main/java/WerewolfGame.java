import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.MessageCreateSpec;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class WerewolfGame {
    TextChannel mainChannel;
    TextChannel werewolfChannel;
    int playerNumber;



    ArrayList<WerewolfGameRole> playerList;
   // ArrayList<WerewolfEvent> DayEvent;
    // ArrayList<WerewolfEvent> NightEvent;
    boolean isDay = false;
    private boolean gameEnd = false;

    WerewolfGame(ArrayList<Member> memberList, TextChannel mainChannel, TextChannel werewolfChannel){
        playerNumber = memberList.size();
        playerList = giveRoles(memberList);
        this.mainChannel = mainChannel;
        this.werewolfChannel = werewolfChannel;

      /*  else {

                playerList.stream()
                        .forEach(player -> rolesList
                                .stream()
                                .findAny()
                                .ifPresent(role -> { role.initialize(player);
                                role.dayEvent.forEach(event -> DayEvent.add(event));
                                role.nightEvent.forEach(event -> NightEvent.add(event));

                                })); */

                gameRun();



        }

    private ArrayList<WerewolfGameRole> giveRoles(ArrayList<Member> player) {
        Random r = new Random();
        ArrayList<WerewolfGameRole> rolesList = new ArrayList<WerewolfGameRole>();
        int numberOfWerewolfes = playerNumber / 4;
        int numberOfVillagers = playerNumber - numberOfWerewolfes;
        for (int i = 0;  i < numberOfWerewolfes; i++ ){
            Werewolf werewolf = new Werewolf(player.get(r.nextInt(player.size())));
            rolesList.add(werewolf);
            player.remove(werewolf.player);
        }
        for (int i = 0; i < numberOfVillagers; i++){
            Villager villager = new Villager(player.get(r.nextInt(player.size())));
            rolesList.add(villager);
            player.remove(villager.player);

        }
        return rolesList;

    }

    public void villageEvent(){
        StringBuilder stringBuilder = new StringBuilder();
        MessageCreateSpec spec = MessageCreateSpec.create();
        stringBuilder.append("Good Morning!");

        mainChannel.createMessage();


    }

    public void werewolfEvent(){
        HashMap<Member,Boolean> votingWerewolfes = new HashMap<>();
        alivePlayers().stream()
                .filter(x -> x.getClass()
                        .isAssignableFrom(Werewolf.class))
                .forEach(y -> votingWerewolfes.put(y.player,false));

        HashMap<WerewolfGameRole, Integer> votedVictims = new HashMap<>();



        werewolfChannel.createMessage(MessageCreateSpec.create().nonceOrElse("Good Night, my fellow Werewolfes! Which one do we want to hunt?"));
        Main.commands.put("kill", event -> {

            if( event.getMessage()
                    .getChannel()
                    .equals(werewolfChannel)) { //fragt ab, ob der spieler in den richtigen channel schreibt

                Member votingPlayer = event.getMember().get();

                if (votingWerewolfes.get(votingPlayer) == false) { //Fragt ab, ob der Spieler bereits abgestimmt hat :D


                    Mono<Member> targetPlayer = event.getMessage()
                            .getMemberMentions()
                            .get(0)
                            .asFullMember();
                      if (/*targetPlayer.hasElement(). == true*/true){ //yeah i know that doesnt work :,D

                        alivePlayers().stream()
                                .filter(y -> y.getClass()
                                        .isAssignableFrom(Villager.class))
                                .filter(z -> z.player.equals(targetPlayer))
                                .findFirst()
                                .ifPresent(victim -> {
                                    votingWerewolfes.replace(votingPlayer,false,true);
                                    if (votedVictims.containsKey(victim.player)){
                                        int voteCounter = votedVictims.get(victim) + 1;
                                        votedVictims.replace(victim, voteCounter);
                                        werewolfChannel.createMessage().withContent(victim.player.getTag()+ " got a vote. He/She/It has now "+ voteCounter + " votes.");

                                    }
                                    else{
                                        votedVictims.put(victim,1);
                                        werewolfChannel.createMessage().withContent(victim.player.getTag() + " got a vote. He/She/It has now 1 vote.");
                                    }

                                    long remainingWerewolfVotes = votingWerewolfes.entrySet().stream().filter(x -> x.getValue() == false).count();
                                    long highestVoting = votedVictims.values().stream().mapToInt(x -> x).filter(x -> x >= 0).max().orElse(0); //finds the highest value.
                                    long secondHighestVoting = votedVictims.values().stream().mapToInt(x -> x).filter(x -> x < highestVoting).filter(x -> x >= 0).max().orElse(0); //finds the second highest value.
                                    if (highestVoting - secondHighestVoting > remainingWerewolfVotes && votedVictims.values().stream().filter(x -> x == highestVoting).count() == 1){ //used to find out if the remaining votes would be able to change the result. if not, there will be the kill.
                                        WerewolfGameRole finalVictim = votedVictims.entrySet().stream().filter(x -> x.getValue() == highestVoting).findFirst().get().getKey();
                                        finalVictim.isAlive = false;
                                        werewolfChannel.createMessage().withContent(finalVictim.player.getTag() + " is the Victim :D");
                                        Main.commands.remove("kill");
                                    }
                                    else if (remainingWerewolfVotes == 0){ //finds out if there are no remaining votes. this should be only reached if there is more then one with the most votes.
                                        WerewolfGameRole finalVictim = votedVictims.entrySet().stream().filter(x -> x.getValue() == highestVoting).findAny().get().getKey();
                                        finalVictim.isAlive = false;
                                        werewolfChannel.createMessage().withContent("Well, seems like there is a draw. We took a random of the mostvoted guys and kill him... " + finalVictim.player.getTag() + " is our Victim.");
                                        Main.commands.remove("kill");
                                    }
                                    else { //forgot what here should happen :,D
                                        werewolfChannel.createMessage().withContent("forgot what here should happen lol");


                                    }




                                });







                      }
                           else {
                             werewolfChannel.createMessage().withContent("No valid target. the syntax is: !kill @HereThenameoftheplayer#9367");

                           }

                }
                else {
                    werewolfChannel.createMessage().withContent("You have already voted ;D");
                }


            }



            return null;
        });

    }


    ArrayList<WerewolfGameRole> alivePlayers(){
        ArrayList <WerewolfGameRole> alivePlayers = new ArrayList<WerewolfGameRole>();
        playerList.stream()
                .filter(player -> player.isAlive)
                .forEach(x -> alivePlayers.add(x));
        return alivePlayers;
    }



    void gameRun() {


        while (gameEnd = false) {


            if (someoneWhoWon() == null) {
                if (isDay == true) {
                    villageEvent();


                    isDay = false;
                } else {
                   werewolfEvent();
                    isDay = true;
                }
            } else {
                someoneWhoWon().winningSequence();
                gameEnd = true;

            }
        }


    }

    WerewolfGameRole someoneWhoWon(){


        AtomicReference<WerewolfGameRole> someoneWhoWon = null;


        playerList.stream()
                .filter(player -> player.winningCondition(this) == true)
                .findFirst().ifPresent(player -> someoneWhoWon.set(player));
        return someoneWhoWon.get();

    }



}
