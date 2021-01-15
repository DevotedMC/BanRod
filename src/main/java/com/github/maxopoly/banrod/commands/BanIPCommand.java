package com.github.maxopoly.banrod.commands;

import com.github.maxopoly.banrod.BanRodPlugin;
import com.github.maxopoly.banrod.model.BanEffect;
import com.github.maxopoly.zeus.commands.ZCommand;
import com.github.maxopoly.zeus.commands.ZeusCommand;
import com.github.maxopoly.zeus.commands.sender.CommandSender;
import com.github.maxopoly.zeus.util.ParsingUtils;
import inet.ipaddr.IPAddress;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;

@ZCommand(description = "Bans a player IP", altIds = "bip", id = "banIP", minArgs = 1, maxArgs = 3)
public class BanIPCommand extends ZeusCommand {
    @Override
    public String handle(CommandSender sender, String command) {
        String[] args = command.split(" ");
        InetAddress inet = null;
        try {
            inet = InetAddress.getByName(args[0]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (inet == null) {
            return "Could not find the IP " + args[0];
        }
        IPAddress ip = IPAddress.from(inet);
        String durationString;
        if (args.length == 1) {
            durationString = "perma";
        }
        else {
            durationString = args [1];
        }
        long howLong = ParsingUtils.parseTime(durationString);
        if (howLong <= 0) {
            return "Could not parse ban duration " + durationString;
        }
        String comment;
        if (args.length == 3) {
            comment = args [2];
        }
        else {
            comment = null;
        }
        Instant now = Instant.now();
        BanRodPlugin.getInstance().getIPBanManager().banIP(ip, now, now.plusMillis(howLong), comment, "ZConsole", BanEffect.ACCOUNT_BAN);
        return "Successfully banned IP of: " + ip.toString();
    }
}
