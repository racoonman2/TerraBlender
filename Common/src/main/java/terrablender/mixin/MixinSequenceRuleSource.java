package terrablender.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.level.levelgen.SurfaceRules;
import terrablender.worldgen.surface.StaticCondition;

@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceRules$SequenceRuleSource")
public class MixinSequenceRuleSource {
	@Shadow
	@Final
	private List<SurfaceRules.RuleSource> sequence;

//	FIXME this conflicts with this mixin from c2me 
//  https://github.com/RelativityMC/C2ME-fabric/blob/368487ccb5a4a5224a6305c020e0e7f1df104136/c2me-opts-allocs/src/main/java/com/ishland/c2me/opts/allocs/mixin/surfacebuilder/MixinMaterialRulesSequenceMaterialRule.java

//  TODO this could be further modified to prevent invalid rule sources from calling apply()
    @Redirect(
    	at = @At(
    		value = "INVOKE",
    		target = "Lcom/google/common/collect/ImmutableList$Builder;add(Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList$Builder;",
    		remap = false
    	), 
    	method = "apply"
    )
    public ImmutableList.Builder<Object> add(ImmutableList.Builder<Object> builder, Object value) {
    	// no point in keeping biome specific surface rules in the sequence if they're not going to be used, so remove them
    	// this helps when theres a lot of modded surface rules, since majority of those aren't going to be used but would still be evaluated otherwise
    	if(value instanceof SurfaceRules.TestRule test && test.condition().equals(StaticCondition.FALSE)) {
    		return builder;
    	}
    	return builder.add(value);
    }
}
