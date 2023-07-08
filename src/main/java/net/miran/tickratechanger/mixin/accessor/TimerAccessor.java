package net.miran.tickratechanger.mixin.accessor;

import net.minecraft.client.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Timer.class)
public interface TimerAccessor {

	@Accessor @Mutable
	public void setMsPerTick(float T);
	
}
