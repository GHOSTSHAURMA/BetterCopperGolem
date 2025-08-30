package ma.shaur.bettercoppergolem.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ma.shaur.bettercoppergolem.custom.entity.LastItemDataHolder;
import net.minecraft.entity.passive.CopperGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

@Mixin(CopperGolemEntity.class)
public class CopperGolemEntityMixin implements LastItemDataHolder
{
	//An item for optimizing selection(select same type of items first for faster sorting and item batching
	//Maybe move to CopperGolemBrain an store as a memory? 
	private ItemStack lastItemStack = ItemStack.EMPTY;

	@Inject(method = "writeCustomData(Lnet/minecraft/storage/WriteView;)V", at = @At("TAIL"))
	public void writeCustomData(WriteView view, CallbackInfo info) 
	{
		view.put("last_item_stack", ItemStack.OPTIONAL_CODEC, lastItemStack);
	}

	@Inject(method = "readCustomData(Lnet/minecraft/storage/ReadView;)V", at = @At("TAIL"))
	public void readCustomData(ReadView view, CallbackInfo info)
	{
		view.read("last_item_stack", ItemStack.OPTIONAL_CODEC);
	}
	
	@Inject(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/CopperGolemEntity;setStackInHand(Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER))
	public void interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> info)
	{
		//No need to optimize for the same item since a player took it out of golem's hands
		lastItemStack = ItemStack.EMPTY;
	}
	
	@Override
	public ItemStack getLastItemStack() 
	{
		return lastItemStack;
	}

	@Override
	public void setLastItemStack(ItemStack lastItemStack) 
	{
		this.lastItemStack = lastItemStack;
	}
}
