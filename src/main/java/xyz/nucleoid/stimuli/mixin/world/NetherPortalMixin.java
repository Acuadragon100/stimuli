package xyz.nucleoid.stimuli.mixin.world;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.stimuli.Stimuli;
import xyz.nucleoid.stimuli.event.EventResult;
import xyz.nucleoid.stimuli.event.world.NetherPortalOpenEvent;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.portal.PortalShape;

@Mixin(PortalShape.class)
public class NetherPortalMixin {
    @Shadow private BlockPos bottomLeft;

    @ModifyReturnValue(method = "findEmptyPortalShape", at = @At("RETURN"))
    private static Optional<PortalShape> filterNewPortal(Optional<PortalShape> original, LevelAccessor worldAccess, BlockPos pos, Direction.Axis firstCheckedAxis) {
        return original.filter(portal -> {
            if (!(worldAccess instanceof ServerLevel || worldAccess instanceof WorldGenRegion)) {
                return true;
            }

            var world = ((ServerLevelAccessor) worldAccess).getLevel();
            var lowerCorner = ((NetherPortalMixin) (Object) portal).bottomLeft;

            try (var invokers = Stimuli.select().at(world, lowerCorner)) {
                var result = invokers.get(NetherPortalOpenEvent.EVENT).onOpenNetherPortal(world, lowerCorner);

                if (result == EventResult.DENY) {
                    return false;
                }
            }

            return true;
        });
    }
}
