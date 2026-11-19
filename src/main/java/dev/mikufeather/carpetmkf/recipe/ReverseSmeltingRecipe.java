package dev.mikufeather.carpetmkf.recipe;

import dev.mikufeather.carpetmkf.MkfAddition;
import dev.mikufeather.carpetmkf.MkfSettings;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;

/** A normal smelting recipe whose match is gated by the current Carpet rule. */
public final class ReverseSmeltingRecipe extends SmeltingRecipe {
    public static final RecipeSerializer<ReverseSmeltingRecipe> SERIALIZER =
            new SimpleCookingSerializer<>(ReverseSmeltingRecipe::new, 200);

    public ReverseSmeltingRecipe(String group, CookingBookCategory category, Ingredient ingredient,
            ItemStack result, float experience, int cookingTime) {
        super(group, category, ingredient, result, experience, cookingTime);
    }

    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                ResourceLocation.fromNamespaceAndPath(MkfAddition.ID, "reverse_smelting"), SERIALIZER);
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        // RecipeManager also calls this for its previously cached recipe ID.
        return MkfSettings.reversibleFurnace && super.matches(input, level);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
