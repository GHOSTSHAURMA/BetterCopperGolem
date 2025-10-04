package ma.shaur.bettercoppergolem.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

@Mixin(ContainerComponent.class)
public interface ContainerComponentAccessor 
{
	@Accessor
	DefaultedList<ItemStack> getStacks();

	@Mutable
	@Final
	@Accessor("stacks")
	void setStacks(DefaultedList<ItemStack> stacks);
}
