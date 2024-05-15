/**
 * Copyright (C) Glitchfiend
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package terrablender.api;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import net.minecraft.world.level.levelgen.SurfaceRules;
import terrablender.worldgen.TBSurfaceRuleData;

public class SurfaceRuleManager
{
    private static Map<RuleCategory, Map<String, SurfaceRules.RuleSource>> surfaceRules = Maps.newLinkedHashMap(); // surface rule order actually matters now so use a map type that preserves that
    private static Map<RuleCategory, SurfaceRules.RuleSource> defaultSurfaceRules = Maps.newLinkedHashMap();
    private static Map<RuleCategory, Map<RuleStage, List<Pair<Integer, SurfaceRules.RuleSource>>>> defaultSurfaceRuleInjections = Maps.newLinkedHashMap();

    /**
     * Add surface rules for biomes belonging to a modded namespace.
     * @param category the category to add surface rules for.
     * @param namespace the namespace to use these rules for.
     * @param rules the rules to add.
     */
    public static void addSurfaceRules(RuleCategory category, String namespace, SurfaceRules.RuleSource rules)
    {
        surfaceRules.get(category).put(namespace, rules);
    }

    /**
     * Add surface rules to be inserted into the default surface rules at a given stage.
     * @param category the category of the surface rules.
     * @param ruleStage the stage to add the surface rules to.
     * @param priority the priority of the surface rules.
     * @param rules the rules to add.
     */
    public static void addToDefaultSurfaceRulesAtStage(RuleCategory category, RuleStage ruleStage, int priority, SurfaceRules.RuleSource rules)
    {
        defaultSurfaceRuleInjections.get(category).get(ruleStage).add(Pair.of(priority, rules));
    }

    /**
     * Set the default surface rules for a category.
     * @param category the category of the surface rules.
     * @param rules the new default surface rules.
     */
    public static void setDefaultSurfaceRules(RuleCategory category, SurfaceRules.RuleSource rules)
    {
        defaultSurfaceRules.put(category, rules);
    }

    /**
     * Remove surface rules for biomes belonging to a modded namespace.
     * @param category the category to add surface rules for.
     * @param namespace the namespace to use these rules for.
     */
    public static void removeSurfaceRules(RuleCategory category, String namespace)
    {
        surfaceRules.get(category).remove(namespace);
    }

    /**
     * Gets the surface rules for a given category.
     * @param category the category to get the surface rules for.
     * @param datapackRules the surface rules to fallback on.
     * @return the surface rules.
     */
    public static SurfaceRules.RuleSource getSurfaceRules(RuleCategory category, SurfaceRules.RuleSource datapackRules) {
    	// store surface rules in a list instead of by namespace. This removes the need to do a biome lookup to get the namespace, which is the main source of the lag.
        ImmutableList.Builder<SurfaceRules.RuleSource> builder = ImmutableList.builder();
        builder.addAll(surfaceRules.get(category).values());
        builder.add(datapackRules);

//		These are essentially a copy of the vanilla surface rules, and I can't find any way to have them be in the same sequence as the datapack rules without one overriding the other.
//      builder.add(getDefaultSurfaceRules(category));
        return SurfaceRules.sequence(builder.build().toArray(SurfaceRules.RuleSource[]::new));
    }

    /**
     * Get the surface rules to be added to a given stage.
     * @param category the category of the surface rules.
     * @param ruleStage the stage of the surface rules.
     * @return list of the surface rules to be added.
     */
    public static List<SurfaceRules.RuleSource> getDefaultSurfaceRuleAdditionsForStage(RuleCategory category, RuleStage ruleStage)
    {
        return defaultSurfaceRuleInjections.get(category).get(ruleStage).stream().sorted(Comparator.comparing(Pair::getFirst, Comparator.reverseOrder())).map(Pair::getSecond).collect(ImmutableList.toImmutableList());
    }

    /**
     * Get the default surface rules for a category.
     * @param category the category to get the surface rules for.
     * @return the default surface rules.
     */
    public static SurfaceRules.RuleSource getDefaultSurfaceRules(RuleCategory category)
    {
        if (defaultSurfaceRules.containsKey(category))
            return defaultSurfaceRules.get(category);

        if (category == RuleCategory.NETHER)
            return TBSurfaceRuleData.nether();
        else if (category == RuleCategory.END) {
            return TBSurfaceRuleData.end();
        }

        return TBSurfaceRuleData.overworld();
    }

    /**
     * Categories for surface rules to be classed under.
     */
    public enum RuleCategory
    {
        OVERWORLD, NETHER, END
    }

    /**
     * The stage for surface rules to be added to.
     */
    public enum RuleStage
    {
        BEFORE_BEDROCK, AFTER_BEDROCK
    }

    static
    {
        for (RuleCategory category : RuleCategory.values())
        {
            // Initialize the surface rules map
            surfaceRules.put(category, Maps.newHashMap());

            // Initialize the default surface rule injections map
            Map<RuleStage, List<Pair<Integer, SurfaceRules.RuleSource>>> ruleStages = Maps.newHashMap();
            for (RuleStage stage : RuleStage.values())
                ruleStages.put(stage, Lists.newArrayList());
            defaultSurfaceRuleInjections.put(category, ruleStages);
        }
    }
}
