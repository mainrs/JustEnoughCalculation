package me.towdium.jecalculation.item.items;

import me.towdium.jecalculation.JustEnoughCalculation;
import me.towdium.jecalculation.client.gui.JecGui;
import me.towdium.jecalculation.client.gui.guis.GuiCalculator;
import me.towdium.jecalculation.item.JecaItem;
import me.towdium.jecalculation.utils.IllegalPositionException;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class ItemCalculator extends JecaItem {
    public ItemCalculator() {
        setHasSubtypes(true);
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.tabTools);
        setUnlocalizedName("item_calculator");
        setTextureName(JustEnoughCalculation.Reference.MODID + ":" + CRAFTING_NAME);
    }

    @Override
    public String getUnlocalizedName(int meta) {
        switch (meta) {
            case 0:
                return "item.item_calculator_crafting";
            case 1:
                return "item.item_calculator_math";
            default:
                throw new IllegalPositionException();
        }
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List items) {
        if (tab == CreativeTabs.tabAllSearch || tab == CreativeTabs.tabTools) {
            items.add(new ItemStack(this, 1, 0));
            items.add(new ItemStack(this, 1, 1));
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack p_77659_1_, World p_77659_2_, EntityPlayer p_77659_3_) {
        if (JustEnoughCalculation.side != JustEnoughCalculation.enumSide.SERVER) {
            JecGui.displayGui(new GuiCalculator());
        }
        return super.onItemRightClick(p_77659_1_, p_77659_2_, p_77659_3_);
    }
}
