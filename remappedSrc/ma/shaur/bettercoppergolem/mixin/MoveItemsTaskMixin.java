package ma.shaur.bettercoppergolem.mixin;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import ma.shaur.bettercoppergolem.config.Config;
import ma.shaur.bettercoppergolem.config.ConfigHandler;
import ma.shaur.bettercoppergolem.custom.entity.LastItemDataHolder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.brain.task.MoveItemsTask;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(MoveItemsTask.class)
public abstract class MoveItemsTaskMixin 
{
	@Mutable
	@Shadow 
	@Final private static int VISITS_UNTIL_COOLDOWN;
	
	@Mutable
	@Shadow 
	@Final private static int MAX_STACK_SIZE_AT_ONCE;

	static 
	{
		Config config = ConfigHandler.getConfig();

		VISITS_UNTIL_COOLDOWN = config.maxChestCheckCount;
		MAX_STACK_SIZE_AT_ONCE = config.maxHeldItemStackSize;
	}

	@Shadow
	private static boolean canPickUpItem(PathAwareEntity entity) { return false; }
	
	@Shadow
	private static boolean canInsert(PathAwareEntity entity, Inventory inventory) { return false; }
	
	@Shadow
	private static boolean hasItem(Inventory inventory) { return false; }
	
	@Shadow
	private static boolean hasExistingStack(PathAwareEntity entity, Inventory inventory) { return false; }
	
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
		if(!(entity instanceof LastItemDataHolder lastStackHolder && !lastStackHolder.getLastItemStack().isEmpty() && ItemStack.areItemsEqual(lastStackHolder.getLastItemStack(), itemStack))) this.resetVisitedPositions(entity);
	}
	
	private static ItemStack betterExtractStack(PathAwareEntity entity, Inventory inventory) 
	{
		int i = 0, firstMatch = -1, matchAmmount = 0;
		for (ItemStack itemStack : inventory)
		{
			if (!itemStack.isEmpty() && firstMatch < 0) 
			{
				matchAmmount = Math.min(itemStack.getCount(), MAX_STACK_SIZE_AT_ONCE);
				firstMatch = i;
				if(!(entity instanceof LastItemDataHolder)) break;
			}
			if(entity instanceof LastItemDataHolder lastStackHolder && !lastStackHolder.getLastItemStack().isEmpty() && ItemStack.areItemsEqual(lastStackHolder.getLastItemStack(), itemStack))
			{
				return inventory.removeStack(i, Math.min(itemStack.getCount(), MAX_STACK_SIZE_AT_ONCE));
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

	private static ItemStack betterInsertStack(PathAwareEntity entity, Inventory inventory)
	{
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
				int tillFullStack = itemStack.getMaxCount() - itemStack.getCount();
				int toInsert = Math.min(tillFullStack, hand.getCount());
				itemStack.setCount(itemStack.getCount() + toInsert);
				hand.setCount(hand.getCount() - tillFullStack);
				inventory.setStack(i, itemStack);
				if (hand.isEmpty()) 
				{
					if(entity instanceof LastItemDataHolder lastStackHolder) lastStackHolder.setLastItemStack(handCopy);
					return ItemStack.EMPTY;
				}
			}
			
			if(ConfigHandler.getConfig().allowInsertingItemsIntoContainers)
			{
				ComponentMap componentMap = itemStack.getComponents();

				if(componentMap.contains(DataComponentTypes.BUNDLE_CONTENTS))
				{
					BundleContentsComponent component = componentMap.get(DataComponentTypes.BUNDLE_CONTENTS);
					BundleContentsComponent.Builder componentBuilder = new BundleContentsComponent.Builder(component);
					if(componentBuilder.add(hand) > 0) itemStack.set(DataComponentTypes.BUNDLE_CONTENTS, componentBuilder.build());
					
					if (hand.isEmpty()) 
					{
						if(entity instanceof LastItemDataHolder lastStackHolder) lastStackHolder.setLastItemStack(handCopy);
						return ItemStack.EMPTY;
					}
				}
				else if(componentMap.contains(DataComponentTypes.CONTAINER))
				{
					ContainerComponent component = componentMap.get(DataComponentTypes.CONTAINER);
					List<ItemStack> stacks = ((ContainerComponentAccessor)(Object) component).getStacks();
					int j = 0;
					for(; j < stacks.size(); j++)
					{
						ItemStack stack = stacks.get(j);
						if(stack.isEmpty())
						{
							stacks.set(j, hand);
							if(entity instanceof LastItemDataHolder lastStackHolder) lastStackHolder.setLastItemStack(handCopy);
							return ItemStack.EMPTY;
						}
						else if(ItemStack.areItemsAndComponentsEqual(stack, hand))
						{
							int tillFullStack = stack.getMaxCount() - stack.getCount();
							int toInsert = Math.min(tillFullStack, hand.getCount());
							stack.setCount(stack.getCount() + toInsert);
							hand.setCount(hand.getCount() - tillFullStack);
							stacks.set(j, stack);
							
							if (hand.isEmpty()) 
							{
								if(entity instanceof LastItemDataHolder lastStackHolder) lastStackHolder.setLastItemStack(handCopy);
								return ItemStack.EMPTY;
							}
						}
					}
					if(j < 27) // I CAN NOT find max stack amount for container component
					{
						DefaultedList<ItemStack> list = DefaultedList.ofSize(j + 2, ItemStack.EMPTY);
						j = 0;
						for(; j + 2 < list.size(); j++)
						{
							list.set(j, stacks.get(j));
						}
						list.set(j, hand);
						((ContainerComponentAccessor)(Object) component).setStacks(list); 
						if(entity instanceof LastItemDataHolder lastStackHolder) lastStackHolder.setLastItemStack(handCopy);
						return ItemStack.EMPTY;
					}
				}
			}
			
			i++;
		}
		return hand;
	}
	
	//Kinda silly, cab't think of a better way for now
	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/brain/task/MoveItemsTask;markVisited(Lnet/minecraft/entity/mob/PathAwareEntity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"))
	private void betterMarkVisited(MoveItemsTask moveItemsTask, PathAwareEntity entity, World world, BlockPos pos, ServerWorld paramWorld, PathAwareEntity paramEntity)
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
	
	//I am become lag, the destroyer of TPS
	@Redirect(method = "canInsert", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/brain/task/MoveItemsTask;hasExistingStack(Lnet/minecraft/entity/mob/PathAwareEntity;Lnet/minecraft/inventory/Inventory;)Z"))
	private static boolean betterHasExistingStack(PathAwareEntity entity, Inventory inventory, PathAwareEntity paramEntity, Inventory paramInventory)
	{
		ItemStack hand = entity.getMainHandStack();
		Config config = ConfigHandler.getConfig();

		boolean emptySpaces = false, shouldPlace = false, shouldInsert = false;
		for(ItemStack stack : inventory)
		{
			if(stack.isEmpty()) 
			{
				emptySpaces = true;
				continue;
			}
			
			if(!hand.getComponents().contains(DataComponentTypes.BUNDLE_CONTENTS) && !hand.getComponents().contains(DataComponentTypes.CONTAINER) && ItemStack.areItemsEqual(stack, hand)) 
			{
				shouldPlace = true;
				if(stack.getCount() < stack.getMaxCount())
				{
					emptySpaces = true;
					break;
				}
			}
			
			if(!config.shulkerAndBundleSorting || shouldPlace || shouldInsert) continue;
			
			ComponentMap componentMap = stack.getComponents();
			if(componentMap.contains(DataComponentTypes.BUNDLE_CONTENTS))
			{
				BundleContentsComponent component = componentMap.get(DataComponentTypes.BUNDLE_CONTENTS);

				if(hand.getComponents().contains(DataComponentTypes.BUNDLE_CONTENTS))
				{
					if(!config.ignoreColor && (!(hand.getItem() instanceof BundleItem) || !hand.getItem().equals(Items.BUNDLE))) // afaik there is no way to just get dye color of an item
					{
						if(ItemStack.areItemsAndComponentsEqual(stack, hand)) shouldPlace = true;
						continue;
					}
					else if(hand.getComponents().get(DataComponentTypes.BUNDLE_CONTENTS).stream().parallel().filter(i -> !component.stream().parallel().anyMatch(j -> ItemStack.areItemsAndComponentsEqual(i, j))).findAny().isEmpty())
					{
						shouldPlace = true;
						continue;
					}
				}
				else if(config.allowInsertingItemsIntoContainers || config.allowIndividualItemsMatchContainerContents)
				{
					Stream<ItemStack> stream = component.stream().parallel();
					if(stream.anyMatch(i -> ItemStack.areItemsEqual(hand, i)))
					{
						shouldPlace = config.allowIndividualItemsMatchContainerContents;
						if(config.allowInsertingItemsIntoContainers) shouldInsert = new BundleContentsComponent.Builder(component).add(hand.copy()) > 0;
						continue;
					}
				}
			}
			if(componentMap.contains(DataComponentTypes.CONTAINER))
			{
				ContainerComponent component = componentMap.get(DataComponentTypes.CONTAINER);

				if(hand.getComponents().contains(DataComponentTypes.CONTAINER))
				{
					if(!config.ignoreColor && (!(hand.getItem() instanceof BlockItem blockItem) || !blockItem.getBlock().equals(Blocks.SHULKER_BOX) || !hand.getItem().equals(Items.SHULKER_BOX)))
					{
						if(ItemStack.areItemsAndComponentsEqual(stack, hand)) shouldPlace = true;
						continue;
					}
					else if(hand.getComponents().get(DataComponentTypes.CONTAINER).stream().parallel().filter(i -> !component.stream().parallel().anyMatch(j -> ItemStack.areItemsAndComponentsEqual(i, j))).findAny().isEmpty())
					{
						shouldPlace = true;
						continue;
					}
				}
				else if(config.allowInsertingItemsIntoContainers || config.allowIndividualItemsMatchContainerContents)
				{
					Stream<ItemStack> stream = component.stream().parallel();
					if(stream.anyMatch(i -> ItemStack.areItemsEqual(hand, i)))
					{
						shouldPlace = config.allowIndividualItemsMatchContainerContents;
						if(config.allowInsertingItemsIntoContainers)
						{
							List<ItemStack> stacks = ((ContainerComponentAccessor)(Object) component).getStacks();
							int i = 0;
							for(ItemStack content : stacks)
							{
								if(content.isEmpty() || ItemStack.areItemsAndComponentsEqual(hand, content) && content.getCount() < content.getMaxCount())
								{
									shouldInsert = true;
									break;
								}
								i++;
							}
							if(i < 27)
							{
								shouldInsert = true;
							}
						}
						continue;
					}
				}
			}
		}
		
		return shouldPlace && emptySpaces || shouldInsert;
	}
}
