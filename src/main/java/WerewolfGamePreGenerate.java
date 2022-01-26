import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

public class WerewolfGamePreGenerate {
    TextChannel mainChannel;
    TextChannel werewolfChannel;
    ArrayList<Member> memberList = new ArrayList<>();

    public WerewolfGamePreGenerate() {
        Main.commands.put("setThisAsMainChannel", event -> event.getMessage().getChannel().ofType(TextChannel.class).doOnNext(this::setMainChannel).then());
        Main.commands.put("setThisAsWerewolfChannel", event -> event.getMessage().getChannel().ofType(TextChannel.class).doOnNext(this::setWerewolfChannel).then());
        Main.commands.put("addMe", event -> event.getMessage().getAuthorAsMember().doOnNext(this::addToPlayerList).then());
        Main.commands.put("deAddMe", event -> event.getMessage().getAuthorAsMember().doOnNext(this::deAddFromPlayerList).then());
        Main.commands.put("init", event -> Mono.just(this)
                        .map(werewolfGamePreGenerate -> werewolfGamePreGenerate.initialiseWerewolfGame())
                        .doOnNext(game -> Main.setWerewolfGame(game)).doOnError(error -> event.getMessage()
                        .getChannel()
                        .map(channel -> channel.createMessage("seems like something was wrong")))
                        .then());

       /* {
           try {
                Main.werewolfGame = initialyseWerewolfGame();
          }
            catch (Exception exception){
                event.getMessage()
                        .getChannel()
                        .doOnNext(channel -> channel.createMessage("seems like something got wrong. Sure that all parameters are ok?"))
                        .then();

            }


             return Mono.empty();
        }); */
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

    public Mono<Void> deAddFromPlayerList(Member member) {
        memberList.remove(member);
        return Mono.empty();
    }

    public WerewolfGame initialiseWerewolfGame() {

        if (mainChannel == null) {
            return null;

        }
        if (werewolfChannel == null) {
            return null;

        }
        if (memberList.size() <= 3) {
            return null;

        } else {
            Main.commands.remove("setThisAsMainChannel");
            Main.commands.remove("setThisAsWerewolfChannel");
            Main.commands.remove("addMe");
            Main.commands.remove("deAddMe");
            Main.commands.remove("init");
            return new WerewolfGame(memberList, mainChannel, werewolfChannel);
        }

    }

}
