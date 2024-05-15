package terrablender.worldgen.surface;

import net.minecraft.server.level.WorldGenRegion;

// keeps track of the currently generating WorldGenRegion during surface generation
public class SurfaceRegion {
	private static final ThreadLocal<WorldGenRegion> SURFACE_REGION = new ThreadLocal<>();

	public static void set(WorldGenRegion region) {
		SURFACE_REGION.set(region);
	}
	
	public static WorldGenRegion get() {
		return SURFACE_REGION.get();
	}
}
