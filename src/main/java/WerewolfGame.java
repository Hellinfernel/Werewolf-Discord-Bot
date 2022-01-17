import discord4j.core.event.domain.message.MessageCreateEvent;
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
    ArrayList<WerewolfGameRole> playersWhoGotKilledOverNight = new ArrayList<>();




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

                gameContinue();



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

       mainChannel.createMessage("Good Morning, my fellow Villagers!").subscribe();
       if (playersWhoGotKilledOverNight.stream().findFirst().isPresent()){
           playersWhoGotKilledOverNight.stream().findFirst().ifPresent(x -> {
               mainChannel.createMessage("Oh no! Seems like " + x.player.getTag() + " got killed!").subscribe();
               playersWhoGotKilledOverNight.remove(x);
               if (playersWhoGotKilledOverNight.stream().findFirst().isPresent()){
                   playersWhoGotKilledOverNight.stream().forEach(y -> mainChannel.createMessage("And " + y.player.getTag() + " too!").subscribe());
               }
           });
           mainChannel.createMessage("THAT CRIES FO JUSTICE! GET THE GUILLOTINE!");
       }
       mainChannel.createMessage("Ok, who do we want to kill?").subscribe();
       HashMap<WerewolfGameRole,Boolean> listOfVotingPlayers = new HashMap<>();

       playerList.stream().filter(player -> player.isAlive = true).forEach(x -> listOfVotingPlayers.put(x,false));
       Main.commands.put("vote", event -> {internVillageEvent(event,listOfVotingPlayers );return Mono.empty();});


    }
    private void internVillageEvent(MessageCreateEvent event, HashMap<WerewolfGameRole, Boolean> listOfVoters){


    }

    public void werewolfEvent(){
        HashMap<Member,Boolean> votingWerewolfes = new HashMap<>();
        alivePlayers().stream()
                .filter(x -> x.getClass()
                        .isAssignableFrom(Werewolf.class))
                .forEach(y -> votingWerewolfes.put(y.player,false));

        HashMap<WerewolfGameRole, Integer> votedVictims = new HashMap<>();




        werewolfChannel.createMessage(MessageCreateSpec.create().nonceOrElse("Good Night, my fellow Werewolfes! Which one do we want to hunt?")).subscribe();
        Main.commands.put("kill", event ->{ internWerewolfEvent(votingWerewolfes,votedVictims,event); return Mono.empty(); });

    }

    private void internWerewolfEvent(HashMap<Member, Boolean> votingWerewolfes, HashMap<WerewolfGameRole, Integer> votedVictims, MessageCreateEvent event) {

        if (event.getMessage()
                .getChannel()
                .equals(werewolfChannel)) { //fragt ab, ob der spieler in den richtigen channel schreibt

            Member votingPlayer = event.getMember().get();

            if (votingWerewolfes.get(votingPlayer) == false) { //Fragt ab, ob der Spieler bereits abgestimmt hat :D


                Mono<Member> targetPlayer = event.getMessage()
                        .getMemberMentions()
                        .get(0)
                        .asFullMember();
                if (/*targetPlayer.hasElement(). == true*/true) { //yeah i know that doesnt work :,D

                    alivePlayers().stream()
                            .filter(y -> y.getClass()
                                    .isAssignableFrom(Villager.class))
                            .filter(z -> z.player.equals(targetPlayer))
                            .findFirst()
                            .ifPresent(victim -> {
                                votingWerewolfes.replace(votingPlayer, false, true);
                                if (votedVictims.containsKey(victim.player)) {
                                    int voteCounter = votedVictims.get(victim) + 1;
                                    votedVictims.replace(victim, voteCounter);
                                    werewolfChannel.createMessage().withContent(victim.player.getTag() + " got a vote. He/She/It has now " + voteCounter + " votes.").subscribe();

                                } else {
                                    votedVictims.put(victim, 1);
                                    werewolfChannel.createMessage().withContent(victim.player.getTag() + " got a vote. He/She/It has now 1 vote.").subscribe();
                                }

                                WerewolfGameRole finalVictim = null;
                                String deathMessage = null;
                                long remainingWerewolfVotes = votingWerewolfes.entrySet().stream().filter(x -> x.getValue() == false).count();
                                long highestVoting = votedVictims.values().stream().mapToInt(x -> x).filter(x -> x >= 0).max().orElse(0); //finds the highest value.
                                long secondHighestVoting = votedVictims.values().stream().mapToInt(x -> x).filter(x -> x < highestVoting).filter(x -> x >= 0).max().orElse(0); //finds the second highest value.
                                if (highestVoting - secondHighestVoting > remainingWerewolfVotes && votedVictims.values().stream().filter(x -> x == highestVoting).count() == 1) { //used to find out if the remaining votes would be able to change the result. if not, there will be the kill.
                                    finalVictim = votedVictims.entrySet().stream().filter(x -> x.getValue() == highestVoting).findFirst().get().getKey();
                                    deathMessage =finalVictim.player.getTag() + " is the Victim :D";
                                } else if (remainingWerewolfVotes == 0) { //finds out if there are no remaining votes. this should be only reached if there is more then one with the most votes.
                                    finalVictim = votedVictims.entrySet().stream().filter(x -> x.getValue() == highestVoting).findAny().get().getKey();
                                    deathMessage = "Well, seems like there is a draw. We took a random of the mostvoted guys and kill him... " + finalVictim.player.getTag() + " is our Victim.";
                                } else { //forgot what here should happen :,D
                                    werewolfChannel.createMessage().withContent("forgot what here should happen lol").subscribe();
                                }

                                if (finalVictim != null){
                                    werewolfChannel.createMessage(deathMessage).subscribe();
                                    finalVictim.isAlive = false;
                                    Main.commands.remove("kill");
                                    playersWhoGotKilledOverNight.add(finalVictim);
                                    gameContinue();
                                    
                                }



                            });


                } else {
                    werewolfChannel.createMessage().withContent("No valid target. the syntax is: !kill @HereThenameoftheplayer#9367").subscribe();

                }

            } else {
                werewolfChannel.createMessage().withContent("You have already voted ;D").subscribe();
            }



        }


    }


    ArrayList<WerewolfGameRole> alivePlayers(){
        ArrayList <WerewolfGameRole> alivePlayers = new ArrayList<WerewolfGameRole>();
        playerList.stream()
                .filter(player -> player.isAlive)
                .forEach(x -> alivePlayers.add(x));
        return alivePlayers;
    }

    void gameContinue(){ //method who should be called if the game should continue.
        if (someoneWhoWon() == null){
                 if (isDay==true) {
            isDay = false;
            werewolfEvent();
                 }

                 else {
            isDay = true;
            villageEvent();
                 }
        }
        else {
            someoneWhoWon().winningSequence();
            gameEnd = true;
        }


    }



    void gameRun() {


        while (gameEnd == false) {


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
