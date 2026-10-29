package com.zidle.item;

import com.zidle.core.PlayerState;
import com.zidle.core.ZIdleSaveData;
import com.zidle.managers.EconomyManager;
import com.zidle.utils.Formatters;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Botón principal "MORDER / INFECTAR": clic derecho = clic manual. */
public class InfectedFangItem extends Item {
    public InfectedFangItem(Settings settings) { super(settings); }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && user instanceof ServerPlayerEntity sp && sp.getServer() != null) {
            PlayerState s = ZIdleSaveData.get(sp.getServer()).of(sp.getUuid());
            double gained = EconomyManager.click(s);
            sp.sendMessage(Text.literal("+" + Formatters.format(gained) + " virus").formatted(Formatting.GREEN), true);
            user.getItemCooldownManager().set(this, 2);
        }
        return TypedActionResult.success(stack, world.isClient());
    }
}
