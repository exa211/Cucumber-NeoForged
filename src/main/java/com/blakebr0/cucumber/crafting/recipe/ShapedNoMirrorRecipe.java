package com.blakebr0.cucumber.crafting.recipe;

import com.blakebr0.cucumber.init.ModRecipeSerializers;
import com.exa.recipe.RecipeJsonUtil;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public class ShapedNoMirrorRecipe extends ShapedRecipe {
    private final ItemStack output;

    public ShapedNoMirrorRecipe(ResourceLocation id, String group, CraftingBookCategory category, int width, int height, NonNullList<Ingredient> inputs, ItemStack output, boolean showNotification) {
        super(id, group, category, width, height, inputs, output, showNotification);
        this.output = output;
    }

    @Override
    public boolean matches(CraftingContainer inventory, Level level) {
        for (int i = 0; i <= inventory.getWidth() - this.getRecipeWidth(); i++) {
            for (int j = 0; j <= inventory.getHeight() - this.getRecipeHeight(); j++) {
                if (this.checkMatch(inventory, i, j)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CRAFTING_SHAPED_NO_MIRROR.get();
    }

    private boolean checkMatch(CraftingContainer inventory, int x, int y) {
        for (var i = 0; i < inventory.getWidth(); i++) {
            for (var j = 0; j < inventory.getHeight(); j++) {
                var k = i - x;
                var l = j - y;
                var ingredient = Ingredient.EMPTY;

                if (k >= 0 && l >= 0 && k < this.getRecipeWidth() && l < this.getRecipeHeight()) {
                    ingredient = this.getIngredients().get(k + l * this.getRecipeWidth());
                }

                if (!ingredient.test(inventory.getItem(i + j * inventory.getWidth()))) {
                    return false;
                }
            }
        }

        return true;
    }

    public static class Serializer implements RecipeSerializer<ShapedNoMirrorRecipe> {
        @Override
        public ShapedNoMirrorRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            var group = GsonHelper.getAsString(json, "group", "");
            var category = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(json, "category", null), CraftingBookCategory.MISC);
            var key = RecipeJsonUtil.keyFromJson(GsonHelper.getAsJsonObject(json, "key"));
            var pattern = RecipeJsonUtil.patternFromJson(GsonHelper.getAsJsonArray(json, "pattern"));
            var width = pattern[0].length();
            var height = pattern.length;
            var ingredients = RecipeJsonUtil.dissolvePattern(pattern, key, width, height);
            var output = itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            var showNotification = GsonHelper.getAsBoolean(json, "show_notification", true);

            return new ShapedNoMirrorRecipe(recipeId, group, category, width, height, ingredients, output, showNotification);
        }

        @Override
        public ShapedNoMirrorRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            var group = buffer.readUtf(32767);
            var category = buffer.readEnum(CraftingBookCategory.class);
            var width = buffer.readVarInt();
            var height = buffer.readVarInt();
            var ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);

            for (var k = 0; k < ingredients.size(); ++k) {
                ingredients.set(k, Ingredient.fromNetwork(buffer));
            }

            var output = buffer.readItem();
            var showNotification = buffer.readBoolean();

            return new ShapedNoMirrorRecipe(recipeId, group, category, width, height, ingredients, output, showNotification);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ShapedNoMirrorRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            buffer.writeVarInt(recipe.getRecipeWidth());
            buffer.writeVarInt(recipe.getRecipeHeight());

            for (var ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buffer);
            }

            buffer.writeItem(recipe.output);
            buffer.writeBoolean(recipe.showNotification());
        }
    }
}
