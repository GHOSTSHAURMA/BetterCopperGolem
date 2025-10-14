package ma.shaur.bettercoppergolem.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import ma.shaur.bettercoppergolem.config.ConfigHandler;
import net.minecraft.entity.passive.CopperGolemBrain;

@Mixin(CopperGolemBrain.class)
public class CopperGolemBrainMixin 
{
	@ModifyConstant(method ="addIdleActivities", constant = @Constant(intValue = 8))
	private static int verticalHeight(int constant)
	{
		return ConfigHandler.getConfig().verticalRange + 10;
	}
}
