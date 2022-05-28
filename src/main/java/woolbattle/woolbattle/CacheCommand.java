package woolbattle.woolbattle;

import org.apache.commons.lang.ObjectUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import woolbattle.woolbattle.perks.AllPassivePerks;
import woolbattle.woolbattle.perks.PassivePerk;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;

public class CacheCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        ArrayList<PassivePerk<? extends Event, ?>> passivePerks = AllPassivePerks.passivePerks;
        HashMap<String, ArrayList<String>> playersOnPerks = new HashMap<>();
        AtomicReference<String> result = new AtomicReference<>(format("%s: ", args[1]));
        AtomicInteger i = new AtomicInteger(0);

        try{

            switch(args[0]){
                case "player":

                        passivePerks.forEach(perk -> perk.getPlayers().forEach(p ->{
                            try{
                                if(p.getName().equals(args[1])){
                                    if(i.get() != 0){
                                        result.set(result.get() + ",\n" + perk.getName());
                                    }else{
                                        result.set("\n" + result.get() + perk.getName());
                                    }
                                }
                                i.addAndGet(1);
                            }catch(NullPointerException ignored){

                            }


                        }));

                        System.out.println(result);



                    break;
                case "perk":
                    passivePerks.forEach(perk ->{if(perk.getName().equals(args[1])){
                        perk.getPlayers().forEach(p -> {
                            if(i.get() != 0){
                                result.set(format("%s,\n%s", result.get(), p.getName()));
                            }else{
                                result.set(format("%s\n%s", result.get(), p.getName()));
                            }
                            i.getAndIncrement();
                        });
                    }
                    });
                    System.out.println(result);
                    break;
                default:
                    System.out.printf("%s%s is not a valid first argument, in regard of this command.", ChatColor.RED, args[0]);
                    break;
            }
        }catch(ArrayIndexOutOfBoundsException e){

            passivePerks.forEach(perk -> {
                AtomicReference<String> res = new AtomicReference<>("");
                AtomicInteger j = new AtomicInteger(0);
                res.set(format("%s\n%s%s%s:\n", res.get(), ChatColor.GREEN ,perk.getName(), ChatColor.RESET));
                perk.getPlayers().forEach(p -> {

                    if(j.get() != 0){
                        res.set(format("%s\t%s,\n", res.get(),p.getName()));
                    }else{
                        res.set(format("%s\t%s", res.get(), p.getName()));
                    }
                    i.getAndIncrement();

                }
            );});
        }




        passivePerks.forEach(perk -> perk.getPlayers().forEach(p -> {

            if (!playersOnPerks.containsKey(p.getName())) {
                playersOnPerks.put(p.getName(), new ArrayList<String>(){
                    {
                        add(perk.getName());
                    }
                });
            }else{
                playersOnPerks.get(p.getName()).add(perk.getName());
            }
        }));

        return false;
    }
}
