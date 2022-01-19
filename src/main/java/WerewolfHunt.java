

public class WerewolfHunt {

/*
    @Override
    public void run(HashMap< Member, Boolean> votingWerewolfes, HashMap<WerewolfGameRole, Integer> votedVictims, MessageCreateEvent event) {

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
                    if (/*targetPlayer.hasElement(). == true*/ //yeah i know that doesnt work :,D

               /*         alivePlayers().stream()
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
/*


            }


        }

    }

                */
}
