package ma.shaur.bettercoppergolem.mixin;

import java.util.function.BiConsumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import ma.shaur.bettercoppergolem.custom.entity.LastItemDataHolder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.brain.task.MoveItemsTask;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(MoveItemsTask.class)
public abstract class MoveItemsTaskMixin 
{
	
	@Shadow
	private static boolean canPickUpItem(PathAwareEntity entity) { return false; }
	
	@Shadow
	private static boolean canInsert(PathAwareEntity entity, Inventory inventory) { return false; }
	
	@Shadow
	private static boolean hasItem(Inventory inventory) { return false; }
	
	@Shadow
	protected abstract void resetVisitedPositions(PathAwareEntity pathAwareEntity);
	
	@Shadow
	protected abstract void invalidateTargetStorage(PathAwareEntity pathAwareEntity);

	@Shadow
	protected abstract void markVisited(PathAwareEntity entity, World world, BlockPos pos);
	
	@Shadow 
	protected abstract void method_74021(PathAwareEntity pathAwareEntity, World world, BlockPos blockPos); //me when I eat the mapping
	
	@ModifyArg(method = "tickInteracting", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/brain/task/MoveItemsTask;selectInteractionState(Lnet/minecraft/entity/mob/PathAwareEntity;Lnet/minecraft/inventory/Inventory;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;)V"), index = 2)
	private BiConsumer<PathAwareEntity, Inventory> pickupItemCallback(BiConsumer<PathAwareEntity, Inventory> pickupItemCallback)
	{
		return this::betterTakeStack;
	}

	private void betterTakeStack(PathAwareEntity entity, Inventory inventory) 
	{
		ItemStack itemStack = betterExtractStack(entity, inventory);
		entity.equipStack(EquipmentSlot.MAINHAND, itemStack);
		entity.setDropGuaranteed(EquipmentSlot.MAINHAND);
		if(!(entity instanceof LastItemDataHolder lastStackHolder && !lastStackHolder.getLastItemStack().isEmpty() && lastStackHolder.getLastItemStack().getItem().equals(itemStack.getItem()))) this.resetVisitedPositions(entity);
	}
	
	private static ItemStack betterExtractStack(PathAwareEntity entity, Inventory inventory) 
	{
		int i = 0, firstMatch = -1, matchAmmount = 0;
		for (ItemStack itemStack : inventory)
		{
			if (!itemStack.isEmpty() && firstMatch < 0) 
			{
				matchAmmount = Math.min(itemStack.getCount(), 16);
				firstMatch = i;
				if(!(entity instanceof LastItemDataHolder)) break;
			}
			if(entity instanceof LastItemDataHolder lastStackHolder && !lastStackHolder.getLastItemStack().isEmpty() && lastStackHolder.getLastItemStack().getItem().equals(itemStack.getItem()))
			{
				return inventory.removeStack(i, Math.min(itemStack.getCount(), 16));
			}
			i++;
		}

		return firstMatch < 0 ? ItemStack.EMPTY : inventory.removeStack(firstMatch, matchAmmount);
	}
	
	@ModifyArg(method = "tickInteracting", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/brain/task/MoveItemsTask;selectInteractionState(Lnet/minecraft/entity/mob/PathAwareEntity;Lnet/minecraft/inventory/Inventory;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;)V"), index = 4)
	private BiConsumer<PathAwareEntity, Inventory> placeItemCallback(BiConsumer<PathAwareEntity, Inventory> pickupItemCallback)
	{
		return this::betterPlaceStack;
	}

	private void betterPlaceStack(PathAwareEntity entity, Inventory inventory) 
	{
		ItemStack itemStack = betterInsertStack(entity, inventory);
		entity.equipStack(EquipmentSlot.MAINHAND, itemStack);
		if (itemStack.isEmpty()) 
		{
			if(!(entity instanceof LastItemDataHolder)) this.resetVisitedPositions(entity);
		} 
		else 
		{
			this.invalidateTargetStorage(entity);
		}
	}

	private static ItemStack betterInsertStack(PathAwareEntity entity, Inventory inventory) {
		int i = 0;
		ItemStack hand = entity.getMainHandStack();
		ItemStack handCopy = entity.getMainHandStack().copy();

		for(ItemStack itemStack : inventory)
		{
			if (itemStack.isEmpty()) 
			{
				inventory.setStack(i, hand);
				if(entity instanceof LastItemDataHolder lastStackHolder) lastStackHolder.setLastItemStack(handCopy);
				return ItemStack.EMPTY;
			}

			if (ItemStack.areItemsAndComponentsEqual(itemStack, hand) && itemStack.getCount() < itemStack.getMaxCount()) 
			{
				int j = itemStack.getMaxCount() - itemStack.getCount();
				int k = Math.min(j, hand.getCount());
				itemStack.setCount(itemStack.getCount() + k);
				hand.setCount(hand.getCount() - j);
				inventory.setStack(i, itemStack);
				if (hand.isEmpty()) 
				{
					if(entity instanceof LastItemDataHolder lastStackHolder) lastStackHolder.setLastItemStack(handCopy);
					return ItemStack.EMPTY;
				}
			}
			i++;
		}

		return hand;
	}
	
	//Kinda silly, cab't think of a better way for now
	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/brain/task/MoveItemsTask;markVisited(Lnet/minecraft/entity/mob/PathAwareEntity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"))
	private void markVisitedRedirect(MoveItemsTask moveItemsTask, PathAwareEntity entity, World world, BlockPos pos, ServerWorld paramWorld, PathAwareEntity paramEntity)
	{
		if(!(entity instanceof LastItemDataHolder)) 
		{
			markVisited(entity, world, pos);
			return;
		}
		
		Inventory inventory = null;

		BlockEntity blockEntity = world.getBlockEntity(pos);
		BlockState blockState = world.getBlockState(pos);
		Block block = blockState.getBlock();
        if (block instanceof ChestBlock chestBlock) inventory = ChestBlock.getInventory(chestBlock, blockState, world, pos, true);
        else if (blockEntity instanceof Inventory) inventory = (Inventory) blockEntity;

		if(inventory != null)
		{
			if(canPickUpItem(entity)) 
			{
				if (!hasItem(inventory)) markVisited(entity, world, pos);
			}
			else if(!canInsert(entity, inventory))
			{
				markVisited(entity, world, pos);
			}
		}
	}
}
