package terrablender.mixin;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.SurfaceRules;
import terrablender.worldgen.IExtendedSurfaceContext;
import terrablender.worldgen.surface.SurfaceRegion;

@Mixin(SurfaceRules.Context.class)
public class MixinSurfaceContext implements IExtendedSurfaceContext {
	@Shadow
	@Final
    private ChunkAccess chunk;
	
	private Set<ResourceKey<Biome>> surroundingBiomes;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(CallbackInfo callback) {
		WorldGenRegion region = SurfaceRegion.get();
		if(region != null) {
	    	this.surroundingBiomes = new HashSet<>();

	    	// collect all biomes in a 3x3 chunk area
	    	// LevelChunkSection keeps track of what biomes are inside of it, so we only have to iterate over every chunk section instead of every block
			ChunkPos centerPos = this.chunk.getPos();
			for(int x = -1; x <= 1; x++) {
	    		for(int z = -1; z <= 1; z++) {
	    			ChunkAccess chunk = region.getChunk(centerPos.x + x, centerPos.z + z);
	    			
	    			for(LevelChunkSection section : chunk.getSections()) {
	    				section.getBiomes().getAll((biome) -> {
	    					biome.unwrapKey().ifPresent(this.surroundingBiomes::add);
	    				});
	    			}
	        	}
	    	}
		}
	}
	
	@Override
	public Set<ResourceKey<Biome>> getSurroundingBiomes() {
		return this.surroundingBiomes;
	}
}
