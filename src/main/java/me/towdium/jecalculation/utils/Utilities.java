package me.towdium.jecalculation.utils;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameData;
import me.towdium.jecalculation.JustEnoughCalculation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Author: Towdium
 * Date:   2016/6/25.
 */
@SuppressWarnings("unused")
public class Utilities {
    final static int[] scaleTable = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};
    static Map<String, String> dictionary = new HashMap<>();

    static {
        Map<String, ModContainer> modMap = Loader.instance().getIndexedModList();
        for (Map.Entry<String, ModContainer> modEntry : modMap.entrySet()) {
            String lowercaseId = modEntry.getKey().toLowerCase(Locale.ENGLISH);
            String modName = modEntry.getValue().getName();
            dictionary.put(lowercaseId, modName);
        }
    }

    // FLOAT FORMATTING
    public static String cutFloat(float f, int size) {
        BiFunction<Float, Integer, String> form = (fl, len) -> {
            // here 2 means floating point and unit character
            // represents for maximum acceptable decimal digits
            int scale = len - 2 - String.valueOf(fl.intValue()).length();
            // maximum decimal limits to 2
            int cut = Math.min(scale, 2);
            return String.format("%." + (Math.max(cut, 0)) + 'f', fl);
        };
        int scale = (int) Math.log10(f) / 3;
        switch (scale) {
            case 0:
                return form.apply(f, size);
            case 1:
                return form.apply(f / 1000.0f, size) + 'K';
            case 2:
                return form.apply(f / 1000000.0f, size) + 'M';
            case 3:
                return form.apply(f / 1000000000.0f, size) + 'B';
            case 4:
                return form.apply(f / 1000000000000.0f, size) + 'G';
            default:
                return form.apply(f / 1000000000000000.0f, size) + 'T';
        }
    }

    public static String cutLong(long i, int size) {
        if (i < 1000) {
            return String.valueOf(i);
        } else {
            return cutFloat(i, size);
        }
    }

    // CIRCULATE STRUCTURE
    public static int circulate(int current, int total, boolean forward) {
        if (forward) {
            if (current == total - 1) return 0;
            else return current + 1;
        } else {
            if (current == 0) return total - 1;
            else return current - 1;
        }
    }

    // MOD NAME
    public static String getModName(ItemStack stack) {
        String name = GameData.getItemRegistry().getNameForObject(stack.getItem());
        JustEnoughCalculation.logger.warn(String.format("item name: %s", name));
        // TODO check
        return name;
    }

    public static String getModName(FluidStack stack) {
        String name = GameData.getItemRegistry().getNameForObject(stack.getFluid());
        JustEnoughCalculation.logger.warn(String.format("fluid name: %s", stack.getFluid()));
        // TODO check
        return name;
    }

    public static class Timer {
        long time = System.currentTimeMillis();
        boolean running = false;

        public void setState(boolean b) {
            if (!b && running)
                running = false;
            if (b && !running) {
                running = true;
                time = System.currentTimeMillis();
            }
        }

        public long getTime() {
            return running ? System.currentTimeMillis() - time : 0;
        }

    }

    public static class Circulator {
        int total, current;

        public Circulator(int total) {
            this(total, 0);
        }

        public Circulator(int total, int current) {
            this.total = total;
            this.current = current;
        }

        public int next() {
            current = current + 1 == total ? 0 : current + 1;
            return current;
        }

        public int prev() {
            current = current == 0 ? total - 1 : current - 1;
            return current;
        }

        public int index() {
            return current;
        }
    }

    public static class ReversedIterator<T> implements Iterator<T> {
        ListIterator<T> i;

        public ReversedIterator(List<T> l) {
            i = l.listIterator(l.size());
        }

        public ReversedIterator(ListIterator<T> i) {
            while (i.hasNext()) i.next();
            this.i = i;
        }

        @Override
        public boolean hasNext() {
            return i.hasPrevious();
        }

        @Override
        public T next() {
            return i.previous();
        }

        public Stream<T> stream() {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED), false);
        }
    }
}
