package com.github.maxopoly.banrod.commands;

import com.github.maxopoly.banrod.BanRodPlugin;
import com.github.maxopoly.banrod.model.BanEffect;
import com.github.maxopoly.zeus.ZeusMain;
import com.github.maxopoly.zeus.commands.ZCommand;
import com.github.maxopoly.zeus.commands.ZeusCommand;
import com.github.maxopoly.zeus.commands.sender.CommandSender;
import com.github.maxopoly.zeus.util.ParsingUtils;
import java.time.Instant;
import java.util.UUID;

@ZCommand(description = "Bans a player", altIds = "bp", id = "banPlayer", minArgs = 1, maxArgs = 3)
public class BanPlayerCommand extends ZeusCommand {
    @Override
    public String handle(CommandSender sender, String command) {
        String[] args = command.split(" ");
        UUID targetPlayer = ZeusMain.getInstance().getPlayerManager().getUUID(args[0]);
        if (targetPlayer == null) {
            return "Could not find the player " + args[0];
        }
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
        BanRodPlugin.getInstance().getAccountBanManager().banAccount(targetPlayer, now, now.plusMillis(howLong), comment, "ZConsole", BanEffect.ACCOUNT_BAN);
        return "Successfully banned: " + targetPlayer;
    }
}
