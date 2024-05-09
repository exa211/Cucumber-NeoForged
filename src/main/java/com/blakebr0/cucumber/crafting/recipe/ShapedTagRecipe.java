package com.blakebr0.cucumber.crafting.recipe;

import com.blakebr0.cucumber.crafting.OutputResolver;
import com.blakebr0.cucumber.init.ModRecipeSerializers;
import com.exa.recipe.RecipeJsonUtil;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class ShapedTagRecipe extends ShapedNoMirrorRecipe {
    private final OutputResolver outputResolver;
    private ItemStack output;

    public ShapedTagRecipe(ResourceLocation id, String group, CraftingBookCategory category, int width, int height, NonNullList<Ingredient> inputs, OutputResolver.Item outputResolver, boolean showNotification) {
        super(id, group, category, width, height, inputs, ItemStack.EMPTY, showNotification);
        this.outputResolver = outputResolver;
    }

    public ShapedTagRecipe(ResourceLocation id, String group, CraftingBookCategory category, int width, int height, NonNullList<Ingredient> inputs, String tag, int count, boolean showNotification) {
        super(id, group, category, width, height, inputs, ItemStack.EMPTY, showNotification);
        this.outputResolver = new OutputResolver.Tag(tag, count);
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        if (this.output == null) {
            this.output = this.outputResolver.resolve();
        }

        return this.output;
    }

    @Override
    public boolean isSpecial() {
        if (this.output == null) {
            this.output = this.outputResolver.resolve();
        }

        return this.output.isEmpty();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CRAFTING_SHAPED_TAG.get();
    }

    public static class Serializer implements RecipeSerializer<ShapedTagRecipe> {
        @Override
        public ShapedTagRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            var group = GsonHelper.getAsString(json, "group", "");
            var category = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(json, "category", null), CraftingBookCategory.MISC);
            var key = RecipeJsonUtil.keyFromJson(GsonHelper.getAsJsonObject(json, "key"));
            var pattern = RecipeJsonUtil.patternFromJson(GsonHelper.getAsJsonArray(json, "pattern"));
            var width = pattern[0].length();
            var height = pattern.length;
            var ingredients = RecipeJsonUtil.dissolvePattern(pattern, key, width, height);
            var result = GsonHelper.getAsJsonObject(json, "result");
            var tag = GsonHelper.getAsString(result, "tag");
            var count = GsonHelper.getAsInt(result, "count", 1);
            var showNotification = GsonHelper.getAsBoolean(json, "show_notification", true);

            return new ShapedTagRecipe(recipeId, group, category, width, height, ingredients, tag, count, showNotification);
        }

        @Override
        public ShapedTagRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            var group = buffer.readUtf(32767);
            var category = buffer.readEnum(CraftingBookCategory.class);
            var width = buffer.readVarInt();
            var height = buffer.readVarInt();
            var ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);

            for (var k = 0; k < ingredients.size(); k++) {
                ingredients.set(k, Ingredient.fromNetwork(buffer));
            }

            var output = OutputResolver.create(buffer);
            var showNotification = buffer.readBoolean();

            return new ShapedTagRecipe(recipeId, group, category, width, height, ingredients, output, showNotification);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ShapedTagRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            buffer.writeVarInt(recipe.getWidth());
            buffer.writeVarInt(recipe.getHeight());

            for (var ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(buffer);
            }

            buffer.writeItemStack(recipe.outputResolver.resolve(), false);
            buffer.writeBoolean(recipe.showNotification());
        }
    }
}
