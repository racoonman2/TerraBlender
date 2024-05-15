package terrablender.mixin;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStatusTasks;
import net.minecraft.world.level.chunk.status.ToFullChunk;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import terrablender.worldgen.surface.SurfaceRegion;

@Mixin(ChunkStatusTasks.class)
public class MixinChunkStatusTasks {

	@Inject(
		method = "generateSurface", 
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;buildSurface(Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;)V"
		),
		locals = LocalCapture.CAPTURE_FAILHARD
	)
    private static void generateSurface$HEAD(WorldGenContext genCtx, ChunkStatus status, Executor executor, ToFullChunk toFullChunk, List<ChunkAccess> neighborChunks, ChunkAccess centerChunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> callback, ServerLevel level, WorldGenRegion region) {
		// set the currently generating chunk region
		SurfaceRegion.set(region);
    }
	
	@Inject(method = "generateSurface", at = @At("TAIL"))
	private static void generateSurface$TAIL(WorldGenContext genCtx, ChunkStatus status, Executor executor, ToFullChunk toFullChunk, List<ChunkAccess> neighborChunks, ChunkAccess centerChunk, CallbackInfoReturnable<CompletableFuture<ChunkAccess>> callback) {
		// clear the currently generating chunk region
		SurfaceRegion.set(null);
    }
}
