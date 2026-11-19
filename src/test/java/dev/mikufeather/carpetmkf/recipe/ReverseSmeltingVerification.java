package dev.mikufeather.carpetmkf.recipe;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.mikufeather.carpetmkf.MkfSettings;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Runs against the actual mapped 1.21.1 recipe implementation, without a world. */
public final class ReverseSmeltingVerification {
    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var ops = RegistryOps.create(JsonOps.INSTANCE, registries);
        var manager = new RecipeManager(registries);
        var metals = List.of("gold", "iron", "copper");
        int assertions = 0;
        try {
            for (String metal : metals) {
                var id = ResourceLocation.fromNamespaceAndPath("carpet-mkf-addition", "raw_" + metal + "_from_ingot");
                var stream = ReverseSmeltingVerification.class.getResourceAsStream(
                        "/data/carpet-mkf-addition/recipe/" + id.getPath() + ".json");
                require(stream != null, "Missing recipe " + id);
                ReverseSmeltingRecipe recipe;
                try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                    recipe = ReverseSmeltingRecipe.SERIALIZER.codec().codec()
                            .parse(ops, JsonParser.parseReader(reader)).getOrThrow();
                }
                var input = new SingleRecipeInput(new ItemStack(BuiltInRegistries.ITEM.get(
                        ResourceLocation.withDefaultNamespace(metal + "_ingot"))));
                var raw = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace("raw_" + metal));
                var holder = new RecipeHolder<>(id, recipe);
                manager.replaceRecipes(List.of(holder));
                MkfSettings.reversibleFurnace = false;
                require(manager.getRecipeFor(RecipeType.SMELTING, input, null, id).isEmpty(), "Disabled ID matched");
                // The cached-check helper needs a world to obtain its manager. Test the
                // actual manager's ID-based overload (the same lookup it delegates to).
                MkfSettings.reversibleFurnace = true;
                require(recipe.matches(input, null), "Enabled recipe did not match " + metal);
                require(manager.getRecipeFor(RecipeType.SMELTING, input, null, id).isPresent(), "ID lookup missed");
                require(recipe.getType() == RecipeType.SMELTING, "Wrong recipe type");
                require(recipe.getCookingTime() == 200 && recipe.getExperience() == 0, "Wrong time/XP");
                var result = recipe.assemble(input, registries);
                require(result.is(raw) && result.getCount() == 1, "Wrong result");
                require(!recipe.matches(new SingleRecipeInput(new ItemStack(Items.STONE)), null), "Matched stone");
                require(manager.getRecipeFor(RecipeType.BLASTING, input, null).isEmpty(), "Matched blast furnace");
                require(manager.getRecipeFor(RecipeType.SMOKING, input, null).isEmpty(), "Matched smoker");
                MkfSettings.reversibleFurnace = false;
                require(!recipe.matches(input, null), "Disabled recipe matched");
                require(manager.getRecipeFor(RecipeType.SMELTING, input, null, id).isEmpty(), "Cached ID ignored disable");
                MkfSettings.reversibleFurnace = true;
                require(manager.getRecipeFor(RecipeType.SMELTING, input, null, id).isPresent(), "Reenable failed");
                assertions += 12;
            }
        } finally {
            MkfSettings.reversibleFurnace = false;
        }
        System.out.println("Reverse smelting verification passed for all three metals (" + assertions + " checks).");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
