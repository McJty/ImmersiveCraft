package mcjty.immcraft.books;

import mcjty.immcraft.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class RenderElementText implements RenderElement {

    private final String text;
    private final int x;
    private final int y;
    private final FontRenderer fontRenderer;

    public RenderElementText(String text, int x, int y) {
        this.text = text;
        this.x = x;
        this.y = y;
        fontRenderer = Minecraft.getMinecraft().fontRendererObj;
    }

    @Override
    public void render(int dy) {
        ClientProxy.font.drawString(x, y + dy, text, 1.0f, 1.0f);
//        fontRenderer.drawString(text, x, y + dy, 0xffffffff);
    }
}