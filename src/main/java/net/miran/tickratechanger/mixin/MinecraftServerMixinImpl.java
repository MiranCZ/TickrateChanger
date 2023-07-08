package net.miran.tickratechanger.mixin;

import net.minecraft.server.MinecraftServer;
import net.miran.tickratechanger.MinecraftServerMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixinImpl implements MinecraftServerMixin {


    @Mutable
    @Shadow
    @Final
    public static int MS_PER_TICK;


    @Shadow protected abstract void startMetricsRecordingTick();

    @Shadow private long nextTickTime;

    private static long TICK_TIME = 50L;

    public void setMsPerTick(int ms) {
        TICK_TIME = ms;
        MS_PER_TICK = ms;
    }

    @Redirect(method = "runServer", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(JJ)J",ordinal = 0))
    public long modifyDelayedTasksMaxNextTickTime(long a, long b) {
        a = a - 50L + TICK_TIME;

        return Math.max(a, b);
    }

    @Redirect(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;startMetricsRecordingTick()V",ordinal = 0))
    public void modifyNextTickTime(MinecraftServer instance){
        this.nextTickTime = nextTickTime -50L + TICK_TIME;
        startMetricsRecordingTick();
    }


}
