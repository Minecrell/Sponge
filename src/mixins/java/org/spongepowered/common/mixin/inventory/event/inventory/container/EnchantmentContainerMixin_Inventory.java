/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.mixin.inventory.event.inventory.container;

import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.event.item.inventory.EnchantItemEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.util.ContainerUtil;
import org.spongepowered.common.item.util.ItemStackUtil;

import java.util.List;
import java.util.Random;

@Mixin(value = EnchantmentContainer.class)
public abstract class EnchantmentContainerMixin_Inventory {

    @Shadow @Final private Random rand;
    @Shadow @Final private IntReferenceHolder xpSeed;
    @Shadow @Final private IInventory tableInventory;

    private ItemStackSnapshot prevItem;
    private ItemStackSnapshot prevLapis;

    // onCraftMatrixChanged lambda
    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;calcItemStackEnchantability(Ljava/util/Random;IILnet/minecraft/item/ItemStack;)I"), require = 1)
    private int impl$onCalcItemStackEnchantability(Random random, int option, int power, ItemStack itemStack) {
        int levelRequirement = EnchantmentHelper.calcItemStackEnchantability(random, option, power, itemStack);
        levelRequirement = InventoryEventFactory.callEnchantEventLevelRequirement((EnchantmentContainer)(Object) this, this.xpSeed.get(), option, power, itemStack, levelRequirement);
        return levelRequirement;
    }

    @Inject(method = "getEnchantmentList", cancellable = true, at = @At(value = "RETURN"))
    private void impl$onBuildEnchantmentList(ItemStack stack, int enchantSlot, int level, CallbackInfoReturnable<List<EnchantmentData>> cir) {
        List<EnchantmentData> newList = InventoryEventFactory
                .callEnchantEventEnchantmentList((EnchantmentContainer) (Object) this, this.xpSeed.get(), stack, enchantSlot, level, cir.getReturnValue());

        if (cir.getReturnValue() != newList) {
            cir.setReturnValue(newList);
        }
    }

    // enchantItem lambda
    @Inject(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;onEnchant(Lnet/minecraft/item/ItemStack;I)V"), require = 1)
    private void impl$beforeEnchantItem(CallbackInfo ci) {
        this.prevItem = ItemStackUtil.snapshotOf(this.tableInventory.getStackInSlot(0));
        this.prevLapis = ItemStackUtil.snapshotOf(this.tableInventory.getStackInSlot(1));
    }

    // enchantItem lambda
    @Inject(method = "*", cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addStat(Lnet/minecraft/util/ResourceLocation;)V"), require = 1)
    private void impl$afterEnchantItem(ItemStack itemstack, int id, PlayerEntity playerIn, int i, ItemStack itemstack1, World arg5, BlockPos arg6, CallbackInfo ci) {
        ItemStackSnapshot newItem = ItemStackUtil.snapshotOf(this.tableInventory.getStackInSlot(0));
        ItemStackSnapshot newLapis = ItemStackUtil.snapshotOf(this.tableInventory.getStackInSlot(1));

        org.spongepowered.api.item.inventory.Container container = ContainerUtil.fromNative((Container) (Object) this);

        Slot slotItem = ((InventoryAdapter) container).inventoryAdapter$getSlot(0).get();
        Slot slotLapis = ((InventoryAdapter) container).inventoryAdapter$getSlot(1).get();

        EnchantItemEvent.Post event =
                InventoryEventFactory.callEnchantEventEnchantPost(playerIn, (EnchantmentContainer) (Object) this,
                        new SlotTransaction(slotItem, this.prevItem, newItem),
                        new SlotTransaction(slotLapis, this.prevLapis, newLapis),
                        id, this.xpSeed.get());

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

}
