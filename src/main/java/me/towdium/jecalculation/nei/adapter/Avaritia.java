package me.towdium.jecalculation.nei.adapter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import codechicken.nei.recipe.IRecipeHandler;

@ParametersAreNonnullByDefault
public class Avaritia implements IAdapter {

    @Override
    public Set<String> getAllOverlayIdentifier() {
        return new HashSet<>(Arrays.asList("extreme_compression", "extreme"));
    }

    @Override
    public void handleRecipe(IRecipeHandler recipe, int index, List<Object[]> inputs, List<Object[]> outputs) {}
}
