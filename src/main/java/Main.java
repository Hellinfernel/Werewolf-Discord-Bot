import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.TextChannelEditSpec;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {
    static Map<String, Command> commands = new HashMap<>();
    private static GatewayDiscordClient client;
    private static WerewolfGame werewolfGame;
    private static WerewolfGamePreGenerate preGenerate;



    static {



        client = DiscordClientBuilder.create(System.getenv("DISCORD_BOT_API_TOKEN"))
                .build()
                .login()
                .block();


        //Dies ist ein static initialisation block. Dieser wird einmalig aufgerufen, wenn die Klasse geladen wird.
        System.out.println("Der Static wurde aufgerufen :,D");
        commands.put("ping", event -> event.getMessage().getChannel()
                .flatMap(channel -> channel.createMessage("Pong!"))
                .then());

        commands.put("newGame", event ->
            Mono.just(new WerewolfGamePreGenerate())
                    .doOnNext(werewolfGamePreGenerate -> preGenerate = werewolfGamePreGenerate)
                    .then());

        commands.put("returnMyName", event -> {
           Member member =event.getMember().get();
           Mono<MessageChannel> channel = event.getMessage().getChannel();
           return channel.flatMap(x -> x.createMessage(member.getTag())).then();

                }
                );

        commands.put("allCommands", event -> event
                .getMessage()
                .getChannel()
                .flatMap(messageChannel -> { StringBuilder stringBuilder = new StringBuilder();
                commands.keySet().forEach(s -> stringBuilder.append(s + "\n"));
                return messageChannel.createMessage(stringBuilder.toString());})
                .then()
             /*   .doOnNext(channel -> commands
                        .entrySet()
                        .stream()
                        .forEach(stringCommandEntry -> channel
                                .createMessage(stringCommandEntry
                                        .getKey())
                                .subscribe())) */
                );



      /*  commands.put("setPermission", event ->
        {

            Mono<Void> ret = client
                    .getChannelById(targetChannelSnowflake) // Gets the channel by id, Mono<Channel>
                    .ofType(TextChannel.class) // Casts the Mono<Channel> to Mono<TextChannel>
                    .flatMap(channel -> { // flatMap allows for Mono<A> -> Mono<B> transformations, expecting a mono to be returned internally
                        // Channel edit returns a Mono<TextChannel>
                        return channel.edit(spec -> {
                            // Sets up a permission override for just one user, the target channel is private by default
                            PermissionOverwrite userPerms = PermissionOverwrite.forMember(
                                    event.getMember().get().getId(),
                                    PermissionSet.of(Permission.READ_MESSAGE_HISTORY, Permission.SEND_MESSAGES, Permission.VIEW_CHANNEL),
                                    PermissionSet.none()
                            );

                            // Grabs the current overrides and downcasts it to add the existing one, this is an open issue re usability
                            // https://github.com/Discord4J/Discord4J/issues/1009
                            Set<PermissionOverwrite> currentOverrides = channel.getPermissionOverwrites().stream()
                                    .map(epo -> ((PermissionOverwrite) epo)).collect(Collectors.toSet());

                            // Adds the new user to the existing perm list
                            currentOverrides.add(userPerms);

                            // Modifies the channels permission overrides, this is a consumer so no need to return here
                            spec.setPermissionOverwrites(currentOverrides);
                        });
                    })
                    // Due to the flatMap over a Mono<TextChannel>, the type of this object is now Mono<TextChannel>
                    // Using .then to throw that type away and turn it into a Void
                    .then().doOnError(Throwable::printStackTrace);



            return ret;


        }); */
        


    }

    public static void setPreGenerate(WerewolfGamePreGenerate preGenerate) {
        Main.preGenerate = preGenerate;
    }

    public static WerewolfGamePreGenerate getPreGenerate() {
        return preGenerate;
    }

    public static WerewolfGame getWerewolfGame() {
        return werewolfGame;
    }

    public static void setWerewolfGame(WerewolfGame werewolfGame) {
        Main.werewolfGame = werewolfGame;
    }

    Mono<TextChannel> editPermissionsOfTextChannel(TextChannel channel, PermissionSet permissionSetsAllowed, PermissionSet permissionSetsDenied, Member member){
        ArrayList<PermissionOverwrite> permissionOverwrite = new ArrayList<>();
        permissionOverwrite.add(PermissionOverwrite.forMember(member.getId(),
                permissionSetsAllowed,
                permissionSetsDenied));
        TextChannelEditSpec.Builder builder = TextChannelEditSpec.builder();
                return channel.edit(builder.addAllPermissionOverwrites(permissionOverwrite)
                                    .build());


    }






        //  .addMemberOverwrite(member.getId(),
        //        PermissionOverwrite.forMember(member.getId(), PermissionSet.of("SEND_MESSAGES"),null)
        //      , "BECAUSE") /*



    public static void main(String args[]){



        client.getEventDispatcher().on(ReadyEvent.class).subscribe(event -> {
            User self = event.getSelf();
            System.out.println(String.format("Logged in as %s#%s", self.getUsername(), self.getDiscriminator()));
        });


        client.getEventDispatcher().on(MessageCreateEvent.class)
                .flatMap(event -> Mono.just(event.getMessage().getContent())
                .flatMap(content -> Flux.fromIterable(commands.entrySet())
                .filter(entry -> content.startsWith('!' + entry.getKey()))
                        .flatMap(entry -> entry.getValue().execute(event))
                        .next()))
                .subscribe();




        client.onDisconnect().block();

        }
   }






