package com.exa.recipe;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Map;
import java.util.Set;

public final class RecipeJsonUtil {
  private static final int MAX_HEIGHT = 3, MAX_WIDTH = 3;


  public static Map<String, Ingredient> keyFromJson(JsonObject object) {
    java.util.Map<java.lang.String, net.minecraft.world.item.crafting.Ingredient> map = Maps.newHashMap();

    for(Map.Entry<java.lang.String, JsonElement> entry : object.entrySet()) {
      if (entry.getKey().length() != 1) {
        throw new JsonSyntaxException("Invalid key entry: '" + (java.lang.String)entry.getKey() + "' is an invalid symbol (must be 1 character only).");
      }

      if (" ".equals(entry.getKey())) {
        throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
      }

      map.put(entry.getKey(), net.minecraft.world.item.crafting.Ingredient.fromJson(entry.getValue(), false));
    }

    map.put(" ", net.minecraft.world.item.crafting.Ingredient.EMPTY);
    return map;
  }

  public static String[] patternFromJson(JsonArray p_44197_) {
    String[] astring = new String[p_44197_.size()];
    if (astring.length > MAX_HEIGHT) {
      throw new JsonSyntaxException("Invalid pattern: too many rows, " + MAX_HEIGHT + " is maximum");
    } else if (astring.length == 0) {
      throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
    } else {
      for(int i = 0; i < astring.length; ++i) {
        String s = GsonHelper.convertToString(p_44197_.get(i), "pattern[" + i + "]");
        if (s.length() > MAX_WIDTH) {
          throw new JsonSyntaxException("Invalid pattern: too many columns, " + MAX_WIDTH + " is maximum");
        }

        if (i > 0 && astring[0].length() != s.length()) {
          throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
        }

        astring[i] = s;
      }

      return astring;
    }
  }

  public static NonNullList<Ingredient> dissolvePattern(String[] p_44203_, Map<String, Ingredient> p_44204_, int p_44205_, int p_44206_) {
    NonNullList<Ingredient> nonnulllist = NonNullList.withSize(p_44205_ * p_44206_, Ingredient.EMPTY);
    Set<String> set = Sets.newHashSet(p_44204_.keySet());
    set.remove(" ");

    for(int i = 0; i < p_44203_.length; ++i) {
      for(int j = 0; j < p_44203_[i].length(); ++j) {
        String s = p_44203_[i].substring(j, j + 1);
        Ingredient ingredient = p_44204_.get(s);
        if (ingredient == null) {
          throw new JsonSyntaxException("Pattern references symbol '" + s + "' but it's not defined in the key");
        }

        set.remove(s);
        nonnulllist.set(j + p_44205_ * i, ingredient);
      }
    }

    if (!set.isEmpty()) {
      throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + set);
    } else {
      return nonnulllist;
    }
  }

  private static int firstNonSpace(String p_44185_) {
    int i;
    for(i = 0; i < p_44185_.length() && p_44185_.charAt(i) == ' '; ++i) {
    }

    return i;
  }

  private static int lastNonSpace(String p_44201_) {
    int i;
    for(i = p_44201_.length() - 1; i >= 0 && p_44201_.charAt(i) == ' '; --i) {
    }

    return i;
  }

  public static String[] shrink(String... p_44187_) {
    int i = Integer.MAX_VALUE;
    int j = 0;
    int k = 0;
    int l = 0;

    for(int i1 = 0; i1 < p_44187_.length; ++i1) {
      String s = p_44187_[i1];
      i = Math.min(i, firstNonSpace(s));
      int j1 = lastNonSpace(s);
      j = Math.max(j, j1);
      if (j1 < 0) {
        if (k == i1) {
          ++k;
        }

        ++l;
      } else {
        l = 0;
      }
    }

    if (p_44187_.length == l) {
      return new String[0];
    } else {
      String[] astring = new String[p_44187_.length - l - k];

      for(int k1 = 0; k1 < astring.length; ++k1) {
        astring[k1] = p_44187_[k1 + k].substring(i, j + 1);
      }

      return astring;
    }
  }

}
