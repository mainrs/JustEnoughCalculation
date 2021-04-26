package me.towdium.jecalculation.client.widget.widgets;

import me.towdium.jecalculation.client.gui.JecGui;
import me.towdium.jecalculation.client.widget.Widget;
import net.minecraft.client.gui.GuiTextField;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Author: towdium
 * Date:   17-8-18.
 */
@ParametersAreNonnullByDefault
public class WTextField extends Widget.Advanced {
    protected int xPos, yPos, xSize;
    GuiTextField textField;

    public WTextField(int xPos, int yPos, int xSize) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.xSize = xSize;
    }

    @Override
    public void onGuiInit(JecGui gui) {
        textField = new GuiTextField(gui.getFontRenderer(), xPos + gui.getGuiLeft(), yPos + gui.getGuiTop(), xSize, 20);
    }

    @Override
    public boolean onClicked(JecGui gui, int xMouse, int yMouse, int button) {
        boolean flag = xMouse >= textField.xPosition && xMouse < textField.xPosition + textField.width && yMouse >= textField.yPosition && yMouse < textField.yPosition + textField.height;
        textField.mouseClicked(xMouse, yMouse, button);
        return textField.isFocused() && flag && button == 0;
    }

    @Override
    public void onDraw(JecGui gui, int xMouse, int yMouse) {
        textField.drawTextBox();
    }

    @Override
    public boolean onKey(JecGui gui, char ch, int code) {
        return textField.textboxKeyTyped(ch, code);
    }
}
