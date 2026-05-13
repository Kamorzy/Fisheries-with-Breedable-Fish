package kamorzy.fisheries_wbf.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import kamorzy.fisheries_wbf.fishery.FisheryFishSpawner.FishCooldownAccess;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements FishCooldownAccess {
    @Unique
    private static final String FISHERIES_WBF_COOLDOWN_TAG = "fisheries_wbf:BaitCooldownUntil";

    @Unique
    private long fisheries_wbf$baitCooldownUntilGameTime;

    @Override
    public long fisheries_wbf$getBaitCooldownUntilGameTime() {
        return fisheries_wbf$baitCooldownUntilGameTime;
    }

    @Override
    public void fisheries_wbf$setBaitCooldownUntilGameTime(long gameTime) {
        fisheries_wbf$baitCooldownUntilGameTime = gameTime;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void fisheries_wbf$saveCooldown(ValueOutput output, CallbackInfo ci) {
        if (fisheries_wbf$baitCooldownUntilGameTime > 0L) {
            output.putLong(FISHERIES_WBF_COOLDOWN_TAG, fisheries_wbf$baitCooldownUntilGameTime);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void fisheries_wbf$loadCooldown(ValueInput input, CallbackInfo ci) {
        fisheries_wbf$baitCooldownUntilGameTime = input.getLongOr(FISHERIES_WBF_COOLDOWN_TAG, 0L);
    }
}
