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

    public static class WerewolfGamePreGenerate{
        TextChannel mainChannel;
        TextChannel werewolfChannel;
        ArrayList<Member> memberList = new ArrayList<>();

        public WerewolfGamePreGenerate(){
            Main.commands.put("setThisAsMainChannel", event -> event.getMessage().getChannel().ofType(TextChannel.class).doOnNext(this::setMainChannel).then());
            Main.commands.put("setThisAsWerewolfChannel", event -> event.getMessage().getChannel().ofType(TextChannel.class).doOnNext(this::setWerewolfChannel).then());
            Main.commands.put("addMe", event -> event.getMessage().getAuthorAsMember().doOnNext(this::addToPlayerList).then());
            Main.commands.put("deAddMe", event -> event.getMessage().getAuthorAsMember().doOnNext(this::deAddFromPlayerList).then());
            Main.commands.put("init", event -> {Main.werewolfGame =initialyseWerewolfGame(); return Mono.empty();
            });
        }

        public void setMainChannel(TextChannel mainChannel) {
            this.mainChannel = mainChannel;
        }

        public void setWerewolfChannel(TextChannel werewolfChannel) {
            this.werewolfChannel = werewolfChannel;

        }

        public void addToPlayerList(Member member) {
            memberList.add(member);

        }
        public Mono<Void> deAddFromPlayerList(Member member){
            memberList.remove(member);
            return Mono.empty();
        }

        public WerewolfGame initialyseWerewolfGame() {
            Exception exception = new Exception();
            if(mainChannel == null){
               // throw exception;
            }
            if (werewolfChannel == null){
              //  throw exception;
            }
            if (memberList.size() <= 3){
                // throw exception;
            }
            else {
                Main.commands.remove("setThisAsMainChannel");
                Main.commands.remove("setThisAsWerewolfChannel");
                Main.commands.remove("addMe");
                Main.commands.remove("deAddMe");
                Main.commands.remove("init");
                return new WerewolfGame(memberList, mainChannel, werewolfChannel);
            }
            return null;
        }

    }

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
       HashMap<WerewolfGameRole,Boolean> listOfVotingPlayers = new HashMap<>(); //this list should contain all players that a
       HashMap<WerewolfGameRole, Integer> listOfVotedPlayers = new HashMap<>();

       playerList.stream().filter(player -> player.isAlive = true).forEach(x -> listOfVotingPlayers.put(x,false));
       Main.commands.put("vote", event -> Mono.fromRunnable(internVillageEvent(event,listOfVotingPlayers,listOfVotedPlayers)));


    }
    private Runnable internVillageEvent(MessageCreateEvent event, HashMap<WerewolfGameRole, Boolean> listOfVoters, HashMap<WerewolfGameRole,Integer> listOfVotedPlayers) {
        return () -> {
            if (event.getMessage()
                    .getChannel()
                    .block()
                    .getId()
                    .equals(mainChannel)) { //checks if the player writes in the right channel :D
                if (listOfVoters.entrySet()
                        .stream()
                        .filter(player -> player.getKey()
                                .player
                                .equals(event.getMessage()
                                        .getAuthorAsMember()
                                        .block()))
                        .filter(player -> player.getValue() == true)
                        .findFirst()
                        .isPresent()) { //tests if the player does actually take part of the game and has still a vote


                    event.getMessage()
                            .getMemberMentions()
                            .stream()
                            .findFirst()
                            .ifPresentOrElse(mentionedPlayer -> {
                                        WerewolfGameRole targetPlayer = playerList
                                                .stream()
                                                .filter(thatPlayer -> thatPlayer.player.equals(mentionedPlayer))
                                                .findFirst()
                                                .get();
                                        listOfVotedPlayers.putIfAbsent(targetPlayer,0);
                                        listOfVotedPlayers.replace(targetPlayer,listOfVotedPlayers.get(targetPlayer) + 1);

                                        WerewolfGameRole finalVictim = null;
                                        String deathMessage = null;
                                        long remainingVotes = listOfVoters.entrySet().stream().filter(x -> x.getValue() == false).count();
                                        long highestVoting = listOfVotedPlayers.values().stream().mapToInt(x -> x).filter(x -> x >= 0).max().orElse(0); //finds the highest value.
                                        long secondHighestVoting = listOfVotedPlayers.values().stream().mapToInt(x -> x).filter(x -> x < highestVoting).filter(x -> x >= 0).max().orElse(0); //finds the second highest value.
                                        if (highestVoting - secondHighestVoting > remainingVotes && listOfVotedPlayers.values().stream().filter(x -> x == highestVoting).count() == 1) { //used to find out if the remaining votes would be able to change the result. if not, there will be the kill.
                                            finalVictim = listOfVotedPlayers.entrySet().stream().filter(x -> x.getValue() == highestVoting).findFirst().get().getKey();
                                            deathMessage =finalVictim.player.getTag() + " is the Victim :D";
                                        } else if (remainingVotes == 0) { //finds out if there are no remaining votes. this should be only reached if there is more then one with the most votes.
                                            finalVictim = listOfVotedPlayers.entrySet().stream().filter(x -> x.getValue() == highestVoting).findAny().get().getKey();
                                            deathMessage = "Well, seems like there is a draw. We took a random of the mostvoted guys and kill him... " + finalVictim.player.getTag() + " is our Victim.";
                                        } else { //forgot what here should happen :,D
                                            werewolfChannel.createMessage().withContent("forgot what here should happen lol").subscribe();
                                        }

                                        if (finalVictim != null){
                                            werewolfChannel.createMessage(deathMessage).subscribe();
                                            finalVictim.isAlive = false;
                                            Main.commands.remove("vote");
                                            playersWhoGotKilledOverNight.add(finalVictim);
                                            gameContinue();

                                        }





                                    }


                                    , () -> event.getMessage().getChannel().block().createMessage("please add a Mention of a player that is actually playing :D").subscribe());
                }
            }
            else {

                event.getMessage().getChannel().block().createMessage("I think thats the wrong Channel :D").subscribe();


            }


        };
    }

    public void werewolfEvent(){
        HashMap<Member,Boolean> votingWerewolfes = new HashMap<>();
        alivePlayers().stream()
                .filter(x -> x.getClass()
                        .isAssignableFrom(Werewolf.class))
                .forEach(y -> votingWerewolfes.put(y.player,false));

        HashMap<WerewolfGameRole, Integer> votedVictims = new HashMap<>();




        werewolfChannel.createMessage(MessageCreateSpec.create().nonceOrElse("Good Night, my fellow Werewolfes! Which one do we want to hunt?")).subscribe();
        Main.commands.put("kill", event -> Mono.fromRunnable(internWerewolfEvent(votingWerewolfes,votedVictims,event)) );

    }

    private Runnable internWerewolfEvent(HashMap<Member, Boolean> votingWerewolfes, HashMap<WerewolfGameRole, Integer> votedVictims, MessageCreateEvent event) {

        return () -> {
            if (event.getMessage()
                    .getChannel()
                    .block()
                    .getId()
                    .equals(werewolfChannel.getId())) { //fragt ab, ob der spieler in den richtigen channel schreibt

                Member votingPlayer = event.getMember().get();

                if (votingWerewolfes.get(votingPlayer) == false) { //Fragt ab, ob der Spieler bereits abgestimmt hat :D


                    Mono<Member> targetPlayer = event.getMessage()
                            .getMemberMentions()
                            .get(0)
                            .asFullMember();
                    if (targetPlayer.hasElement().block()) { //yeah i know that doesnt work :,D

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

        };





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
