package com.ikexing.randomtweaker.api.instance.player;

import com.ikexing.randomtweaker.impl.utils.cap.PlayerSanityHelper;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.player.IPlayer;
import stanhebben.zenscript.annotations.ZenExpansion;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenExpansion("crafttweaker.player.IPlayer")
public class IPlayerExpansionSanity {

    @ZenMethod
    public static float getOriginalSanity(IPlayer player) {
        return PlayerSanityHelper.getPlayerSanity(CraftTweakerMC.getPlayer(player))
            .getOriginalSanity();
    }

    @ZenMethod
    public static void setOriginalSanity(IPlayer player, int sanity) {
        PlayerSanityHelper.getPlayerSanity(CraftTweakerMC.getPlayer(player))
            .setOriginalSanity(sanity);
    }

    @ZenMethod
    public static float getSanity(IPlayer player) {
        return PlayerSanityHelper.getPlayerSanity(CraftTweakerMC.getPlayer(player)).getSanity();
    }

    @ZenMethod
    public static void setSanity(IPlayer player, float sanity) {
        PlayerSanityHelper.getPlayerSanity(CraftTweakerMC.getPlayer(player)).setSanity(sanity);
    }

    @ZenMethod
    public static void updateSanity(IPlayer player, float sanity) {
        PlayerSanityHelper.getPlayerSanity(CraftTweakerMC.getPlayer(player)).updateSanity(sanity);
    }

    /**
     * You need to call this method if the value you changed is not stored or processed in the
     * client (world.remote). It is generally not needed.
     */

    @ZenMethod
    public static void sync(IPlayer player) {
        PlayerSanityHelper.sync(CraftTweakerMC.getPlayer(player));
    }
}
