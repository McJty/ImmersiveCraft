package mcjty.immcraft.items;


import mcjty.immcraft.blocks.ModBlocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModItems {
    public static ItemChisel chisel;
    public static ItemSaw saw;
    public static ItemDummyBook dummyBook;

    public static void init() {
        chisel = new ItemChisel();
        saw = new ItemSaw();
        dummyBook = new ItemDummyBook();
    }

    public static void initCrafting() {
        GameRegistry.addShapedRecipe(new ItemStack(saw), " sr", "sr ", "s  ", 's', Items.STICK, 'r', ModBlocks.rockBlock);
        GameRegistry.addShapedRecipe(new ItemStack(chisel), "  r", " r ", "s  ", 's', Items.STICK, 'r', ModBlocks.rockBlock);
    }

    @SideOnly(Side.CLIENT)
    public static void initModels() {
        chisel.initModel();
        saw.initModel();
        dummyBook.initModel();
    }
}
