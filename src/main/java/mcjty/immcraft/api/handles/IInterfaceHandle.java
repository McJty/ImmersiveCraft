package mcjty.immcraft.api.handles;

import mcjty.immcraft.api.input.KeyType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public interface IInterfaceHandle {
    float getMinX();
    float getMaxX();
    float getMinY();
    float getMaxY();
    EnumFacing getSide();
    float getScale();

    Vec3d getRenderOffset();

    ItemStack getCurrentStack(TileEntity inventoryTE);

    boolean acceptAsInput(ItemStack stack);

    // Insert a stack and return the number of items that could not be inserted
    int insertInput(TileEntity te, ItemStack stack);

    // True if this handle is meant for output.
    boolean isOutput();

    // True if this is a crafting handle
    boolean isCrafting();

    // Extract output, if amount is -1 all will be extracted. Otherwise the specific amount.
    ItemStack extractOutput(TileEntity te, EntityPlayer player, int amount);

    // A key is pressed.
    void onKeyPress(TileEntity te, KeyType keyType, EntityPlayer player);
}