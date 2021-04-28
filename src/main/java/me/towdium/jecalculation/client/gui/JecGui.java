package me.towdium.jecalculation.client.gui;

import cpw.mods.fml.client.config.GuiUtils;
import me.towdium.jecalculation.JustEnoughCalculation;
import me.towdium.jecalculation.client.gui.resource.Resource;
import me.towdium.jecalculation.client.gui.widget.IDrawable;
import me.towdium.jecalculation.client.gui.widget.Widget;
import me.towdium.jecalculation.client.gui.widget.widgets.WEntry;
import me.towdium.jecalculation.client.gui.widget.widgets.WEntryGroup;
import me.towdium.jecalculation.core.entry.Entry;
import me.towdium.jecalculation.nei.NEIPlugin;
import me.towdium.jecalculation.polyfill.mc.client.renderer.GlStateManager;
import me.towdium.jecalculation.utils.IllegalPositionException;
import me.towdium.jecalculation.utils.helpers.LocalizationHelper;
import me.towdium.jecalculation.utils.wrappers.Single;
import me.towdium.jecalculation.utils.wrappers.Triple;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Author: towdium
 * Date:   8/12/17.
 */
@ParametersAreNonnullByDefault
public class JecGui extends GuiContainer {
    public static final int COLOR_GREY = 0xFFA1A1A1;
    public static final int COLOR_BLUE = 0xFFb0b9e6;

    public List<GuiButton> buttonList = super.buttonList;
    public WidgetManager wgtMgr = new WidgetManager();
    public Entry hand = Entry.EMPTY;
    protected GuiScreen parent;
    protected List<Triple<Integer, Integer, List<String>>> tooltipBuffer = new ArrayList<>();

    public JecGui(@Nullable GuiScreen parent) {
        this(parent, false);
    }

    public JecGui(@Nullable GuiScreen parent, boolean acceptsTransfer) {
        super(acceptsTransfer ? new ContainerTransfer() : new ContainerNonTransfer());
        this.parent = parent;
    }

    @Override
    public void initGui() {
        super.initGui();
        wgtMgr.onInit();
    }

    public static boolean mouseIn(int xPos, int yPos, int xSize, int ySize, int xMouse, int yMouse) {
        return xMouse > xPos && yMouse > yPos && xMouse <= xPos + xSize && yMouse <= yPos + ySize;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        wgtMgr.onDraw(mouseX, mouseY);
        drawExtra();
        GlStateManager.pushMatrix();
        GlStateManager.translate(mouseX - 8, mouseY - 8, 0);
        hand.drawEntry(this);
        GlStateManager.popMatrix();
        drawBufferedTooltip();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawResourceContinuous(Resource.WGT_PANEL, guiLeft, guiTop, xSize, ySize, 5, 5, 5, 5);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        boolean InGui = wgtMgr.onClick(mouseX, mouseY, mouseButton);
        if(!InGui) {
            this.handleMouseEvent();
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (!wgtMgr.onKey(typedChar, keyCode)) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                if (hand != Entry.EMPTY)
                    hand = Entry.EMPTY;
                else if (parent != null)
                    Minecraft.getMinecraft().displayGuiScreen(parent);
                else
                    super.keyTyped(typedChar, keyCode);
            }
        }
    }

    /**
     * @return if the event is canceled
     * This function handles click outside the normal region,
     * especially the overlap with JEI overlay. It handles
     * mouse event before JEI.
     */
    public boolean handleMouseEvent() {
        JustEnoughCalculation.logger.info("handle mouse clicked");
        int xMouse = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int yMouse = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        if (Mouse.getEventButtonState()) {
            if (Mouse.getEventButton() == 0) {
                if (hand == Entry.EMPTY) {
                    // TODO
                    Entry e = NEIPlugin.getEntryUnderMouse();
                    if (e != Entry.EMPTY) {
                        hand = e;
                        return true;
                    }
                } else {
                    if (!mouseIn(guiLeft, guiTop, width, height, xMouse, yMouse)) {
                        hand = Entry.EMPTY;
                        return true;
                    }
                }
            } else if (Mouse.getEventButton() == 1) {
                if (hand != Entry.EMPTY) {
                    hand = Entry.EMPTY;
                    return true;
                }
            }
        }
        return false;
    }

    public FontRenderer getFontRenderer() {
        return fontRendererObj;
    }

    public void drawResource(Resource r, int xPos, int yPos) {
        drawTexture(r.getResourceLocation(), xPos, yPos, r.getXPos(), r.getYPos(), r.getXSize(), r.getYSize());
    }

    public void drawResourceContinuous(Resource r,
                                       int xPos,
                                       int yPos,
                                       int xSize,
                                       int ySize,
                                       int borderTop,
                                       int borderBottom,
                                       int borderLeft,
                                       int borderRight) {
        drawTextureContinuous(r.getResourceLocation(), xPos, yPos, xSize, ySize, r.getXPos(), r.getYPos(), r.getXSize(),
                              r.getYSize(), borderTop, borderBottom, borderLeft, borderRight);
    }

    public void drawTexture(ResourceLocation l,
                            int destXPos,
                            int destYPos,
                            int sourceXPos,
                            int sourceYPos,
                            int sourceXSize,
                            int sourceYSize) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(l);
        drawTexturedModalRect(destXPos, destYPos, sourceXPos, sourceYPos, sourceXSize, sourceYSize);
    }

    public void drawFluid(Fluid f, int xPos, int yPos, int xSize, int ySize) {
        IIcon fluidStillIcon = f.getStillIcon();
        mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        if (fluidStillIcon != null) drawTexturedModelRectFromIcon(xPos, yPos, fluidStillIcon, xSize, ySize);
    }

    public void drawTextureContinuous(ResourceLocation l,
                                      int destXPos,
                                      int destYPos,
                                      int destXSize,
                                      int destYSize,
                                      int sourceXPos,
                                      int sourceYPos,
                                      int sourceXSize,
                                      int sourceYSize,
                                      int borderTop,
                                      int borderBottom,
                                      int borderLeft,
                                      int borderRight) {
        GuiUtils.drawContinuousTexturedBox(l, destXPos, destYPos, sourceXPos, sourceYPos, destXSize, destYSize,
                                           sourceXSize, sourceYSize, borderTop, borderBottom, borderLeft, borderRight,
                                           zLevel);
    }

    public void drawRectangle(int xPos, int yPos, int xSize, int ySize, int color) {
        drawRect(xPos, yPos, xPos + xSize, yPos + ySize, color);
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        int right = xPos + xSize;
        int bottom = yPos + ySize;
        Tessellator tessellator = Tessellator.instance;
        GlStateManager.disableTexture2D();
        GlStateManager.color(f, f1, f2, f3);
        tessellator.startDrawingQuads();
        tessellator.addVertex((double) xPos, (double) bottom, 0.0D);
        tessellator.addVertex((double) right, (double) bottom, 0.0D);
        tessellator.addVertex((double) right, (double) yPos, 0.0D);
        tessellator.addVertex((double) xPos, (double) yPos, 0.0D);
        tessellator.draw();
        GlStateManager.enableTexture2D();
    }


    public void drawText(float xPos, float yPos, Font f, String... text) {
        Function<String, Integer> indenter;
        switch (f.align) {
            case AUTO:
            case LEFT:
                indenter = s -> 0;
                break;
            case CENTRE:
                indenter = s -> fontRendererObj.getStringWidth(s) / 2;
                break;
            case RIGHT:
                indenter = s -> -fontRendererObj.getStringWidth(s);
                break;
            default:
                throw new IllegalPositionException();
        }
        drawText(xPos, yPos, f, indenter, text);
    }

    private void drawText(float xPos, float yPos, Font f, Function<String, Integer> indenter, String... text) {
        Single<Integer> y = new Single<>(0);
        boolean unicode = fontRendererObj.getUnicodeFlag();
        if (!f.unicode)
            fontRendererObj.setUnicodeFlag(false);
        GlStateManager.pushMatrix();
        GlStateManager.translate(xPos, yPos, 0);
        GlStateManager.scale(f.size, f.size, 1);
        Arrays.stream(text).forEachOrdered(s -> {
            fontRendererObj.drawString(s, indenter.apply(s), y.value, f.color, f.shadow);
            y.value += fontRendererObj.FONT_HEIGHT;
        });
        GlStateManager.popMatrix();
        fontRendererObj.setUnicodeFlag(unicode);
    }

    public void drawText(float xPos, float yPos, float xSize, Font f, String... text) {
        float sizeScaled = xSize / f.size;
        int l = fontRendererObj.getStringWidth("...");
        String[] ss = !f.cut ? text : Arrays.stream(text).map(s -> {
            int w = fontRendererObj.getStringWidth(s);
            if (w <= sizeScaled) return s;
            else if (l >= w) return "...";
            else return fontRendererObj.trimStringToWidth(s, (int) (sizeScaled - l)) + "...";
        }).toArray(String[]::new);
        switch (f.align) {
            case LEFT:
                drawText(xPos, yPos, f, s -> 0, ss);
                break;
            case AUTO:
            case CENTRE:
                drawText(xPos, yPos, f, s -> ((int) sizeScaled - fontRendererObj.getStringWidth(s)) / 2, ss);
                break;
            case RIGHT:
                drawText(xPos, yPos, f, s -> (int) sizeScaled - fontRendererObj.getStringWidth(s), ss);
                break;
            default:
                throw new IllegalPositionException();
        }
    }

    public void drawText(float xPos, float yPos, int xSize, int ySize, Font f, String... text) {
        int yOffset = (ySize - (int) (text.length * fontRendererObj.FONT_HEIGHT * f.size)) / 2;
        drawText(xPos, yPos + yOffset, xSize, f, text);
    }

    public void drawTooltip(int xPos, int yPos, String... text) {
        drawTooltip(xPos, yPos, Arrays.asList(text));
    }

    public void drawTooltip(int xPos, int yPos, List<String> text) {
        tooltipBuffer.add(new Triple<>(xPos, yPos, text));
    }

    protected void drawBufferedTooltip() {
        tooltipBuffer.forEach(i -> {
            drawHoveringText(i.three, i.one, i.two);
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
        });
        tooltipBuffer.clear();
    }

    public void drawItemStack(int xPos, int yPos, ItemStack is, boolean centred) {
        if (is.getItem() == null) {
            return;
        }
        if (centred) {
            xPos -= 8;
            yPos -= 8;
        }
        RenderHelper.enableGUIStandardItemLighting();
        itemRender.renderItemIntoGUI(fontRendererObj, mc.getTextureManager(), is, xPos, yPos);
        RenderHelper.disableStandardItemLighting();
    }

    public String localize(String key, Object... os) {
        return LocalizationHelper.format(this.getClass(), "gui", key, os);
    }

    // function to override

    protected void drawExtra() {
    }

    public static class Font {
        public static final Font DEFAULT_SHADOW = new Font(0xFFFFFF, true, true, false, 1, enumAlign.AUTO);
        public static final Font DEFAULT_NO_SHADOW = new Font(0x404040, false, true, false, 1, enumAlign.AUTO);
        public static final Font DEFAULT_HALF = new Font(0xFFFFFF, true, true, true, 0.5f, enumAlign.AUTO);

        public int color;
        public boolean shadow, cut, unicode;
        public float size;
        public enumAlign align;

        /**
         * @param color   foreground color
         * @param shadow  whether to draw shadow
         * @param cut     whether to cut string when exceed xSize
         * @param unicode if false, FORCE NOT UNICODE
         * @param size    font size, 1 for default font size
         */
        public Font(int color, boolean shadow, boolean cut, boolean unicode, float size, enumAlign align) {
            this.color = color;
            this.shadow = shadow;
            this.cut = cut;
            this.size = size;
            this.align = align;
            this.unicode = unicode;
        }

        public Font copy() {
            return new Font(color, shadow, cut, unicode, size, align);
        }

        public enum enumAlign {LEFT, CENTRE, RIGHT, AUTO}
    }

    public static boolean isShiftDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    public static class ContainerTransfer extends Container {
        @Override
        public boolean canInteractWith(EntityPlayer playerIn) {
            return true;
        }
    }

    public static class ContainerNonTransfer extends Container {
        @Override
        public boolean canInteractWith(EntityPlayer playerIn) {
            return true;
        }
    }


    public class WidgetManager {
        protected List<Widget> widgets = new ArrayList<>();
        protected boolean initialized = false;

        public void add(Widget w) {
            widgets.add(w);
            if (initialized && w instanceof IDrawable)
                ((IDrawable) w).onGuiInit(JecGui.this);
        }

        public void addAll(Widget... w) {
            for (Widget aw : w)
                add(aw);
        }

        public void remove(Widget w) {
            if (widgets.remove(w) && w instanceof IDrawable) ((IDrawable) w).onRemoved(JecGui.this);
        }

        public void removeAll(Widget... w) {
            for (Widget aw : w)
                remove(aw);
        }

        public void onInit() {
            widgets.stream().filter(w -> w instanceof IDrawable)
                   .forEach(w -> ((IDrawable) w).onGuiInit(JecGui.this));
            initialized = true;
        }

        public void onDraw(int mouseX, int mouseY) {
            widgets.forEach(widget -> widget.onDraw(JecGui.this, mouseX, mouseY));
        }

        @SuppressWarnings("UnusedReturnValue")
        public boolean onClick(int xMouse, int yMouse, int button) {
            for (Widget w : widgets) {
                if (w instanceof IDrawable &&
                    ((IDrawable) w).onClicked(JecGui.this, xMouse, yMouse, button))
                    return true;
            }
            return false;
        }

        @SuppressWarnings("UnusedReturnValue")
        public boolean onKey(char ch, int code) {
            return widgets.stream().filter(w -> w instanceof IDrawable)
                          .anyMatch(w -> ((IDrawable) w).onKey(JecGui.this, ch, code));
        }

        public Optional<WEntry> getEntryAt(int xMouse, int yMouse) {
            return widgets.stream().map(w -> {
                if (w instanceof WEntryGroup)
                    return ((WEntryGroup) w).getEntryAt(JecGui.this, xMouse, yMouse);
                else if (w instanceof WEntry)
                    return Optional.ofNullable(((WEntry) w).mouseIn(JecGui.this, xMouse, yMouse) ? ((WEntry) w) : null);
                else
                    return Optional.<WEntry>empty();
            }).filter(Optional::isPresent).findFirst().orElse(Optional.empty());
        }
    }

    public int getGuiLeft() {
        return guiLeft;
    }

    public int getGuiTop() {
        return guiTop;
    }

    public void drawHoveringText(String text, int x, int y) {
        super.drawHoveringText(Arrays.asList(text), x, y, fontRendererObj);
    }

    public void drawHoveringText(List<String> textLines, int x, int y) {
        super.drawHoveringText(textLines, x, y, fontRendererObj);
    }
}
