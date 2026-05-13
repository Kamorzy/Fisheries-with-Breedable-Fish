package kamorzy.fisheries_wbf.mixin;

import kamorzy.fisheries_wbf.FisheriesWbfModEntry;
import kamorzy.fisheries_wbf.fishery.FisheryBaitHandler;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Unique
    private boolean fisheries_wbf$loggedValidBaitTick;

    @Inject(method = "tick", at = @At("TAIL"))
    private void fisheries_wbf$tickBait(CallbackInfo ci) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;

        if (!fisheries_wbf$loggedValidBaitTick
                && (itemEntity.getItem().is(Items.ROTTEN_FLESH)
                || itemEntity.getItem().is(Items.BEETROOT)
                || itemEntity.getItem().is(Items.SPIDER_EYE))) {
            fisheries_wbf$loggedValidBaitTick = true;
        }

        FisheryBaitHandler.tickItemEntity(itemEntity);
    }
}