package terrablender.mixin;

import java.util.Set;
import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.SurfaceRules;
import terrablender.worldgen.IExtendedSurfaceContext;
import terrablender.worldgen.surface.StaticCondition;

@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceRules$BiomeConditionSource")
public class MixinBiomeConditionSource {
	@Shadow
	@Final
    Predicate<ResourceKey<Biome>> biomeNameTest;

    @Inject(
    	at = @At("HEAD"), 
    	method = "apply", 
    	cancellable = true
    )
    public void apply(SurfaceRules.Context ctx, CallbackInfoReturnable<SurfaceRules.Condition> callback) {
    	Set<ResourceKey<Biome>> surroundingBiomes;
    	if((Object) ctx instanceof IExtendedSurfaceContext surfaceContext && (surroundingBiomes = surfaceContext.getSurroundingBiomes()) != null) {
    		boolean isBiomeNearby = surroundingBiomes.stream().filter(this.biomeNameTest).findAny().isPresent();
    		if(!isBiomeNearby) {
    			// if the biome isn't in any nearby chunks we can always return false
    			callback.setReturnValue(StaticCondition.FALSE);
    		} else if(surroundingBiomes.size() == 1) {
    			// the biome is the only one in any nearby chunks so we can always return true
    			callback.setReturnValue(StaticCondition.TRUE);
    		}
    	}
    }
}