package mcjty.immcraft.blocks.book;

import mcjty.immcraft.api.book.IBook;
import mcjty.immcraft.api.helpers.InventoryHelper;
import mcjty.immcraft.blocks.generic.GenericImmcraftTE;
import mcjty.immcraft.books.BookPage;
import mcjty.immcraft.books.BookParser;
import mcjty.immcraft.books.RenderSection;
import mcjty.immcraft.config.GeneralConfiguration;
import mcjty.immcraft.network.ImmCraftPacketHandler;
import mcjty.immcraft.network.PacketPageFlip;
import mcjty.immcraft.sound.SoundController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class BookStandTE extends GenericImmcraftTE {

    private ItemStack currentBook = ItemStack.EMPTY;

    // Pages and pageNumber are client side only
    private List<BookPage> pages = null;
    private int pageNumber = 0;
    private String result = null;       // Last result from rendering an element

    public BookStandTE() {
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        boolean oldBook = !currentBook.isEmpty();
        super.onDataPacket(net, packet);
        if (getWorld().isRemote) {
            // If needed send a render update.
            boolean newBook = !currentBook.isEmpty();
            if (oldBook != newBook) {
                getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
            }
        }
    }

    // Only use clientside
    public List<BookPage> getPages() {
        if (pages == null) {
            if (currentBook.isEmpty()) {
                pages = Collections.emptyList();
                return pages;
            }
            ResourceLocation json = ((IBook) currentBook.getItem()).getJson();

            BookParser parser = new BookParser();
            pages = parser.parse(json, 768, 900);
            if (pageNumber >= pages.size()) {
                pageNumber = 0;
                markDirtyClient();
            }
        }
        return pages;
    }

    public boolean hasBook() {
        return !currentBook.isEmpty();
    }

    public int getPageNumber() {
        return pageNumber;
    }

    private boolean pageDec(EntityPlayer player) {
        ImmCraftPacketHandler.INSTANCE.sendTo(new PacketPageFlip(getPos(), -1), (EntityPlayerMP) player);
        return true;
    }

    private boolean pageInc(EntityPlayer player) {
        ImmCraftPacketHandler.INSTANCE.sendTo(new PacketPageFlip(getPos(), 1), (EntityPlayerMP) player);
        return true;
    }

    public void pageDecClient() {
        if (pageNumber > 0) {
            pageNumber--;
            getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
            playPageTurn(getWorld(), getPos());
        }
    }

    public void pageIncClient() {
        if (pages != null && pageNumber < pages.size()-1) {
            pageNumber++;
            getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
            playPageTurn(getWorld(), getPos());
        }
    }

    public static int findPageForSection(List<BookPage> pages, String section) {
        for (int i = 0 ; i < pages.size() ; i++) {
            for (RenderSection s : pages.get(i).getSections()) {
                if (section.equals(s.getName())) {
                    return i;
                }
            }

        }
        return -1;
    }

    private void gotoPageClient(String section) {
        if (pages != null) {
            if ("<".equals(section)) {
                pageDecClient();
            } else if (">".equals(section)) {
                pageIncClient();
            } else if ("^".equals(section)) {
                if (pageNumber != 0) {
                    pageNumber = 0;
                    getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
                    playPageTurn(getWorld(), getPos());
                }
            } else {
                int number = findPageForSection(pages, section);
                if (number != -1 && number < pages.size()) {
                    if (pageNumber != number) {
                        pageNumber = number;
                        getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
                        playPageTurn(getWorld(), getPos());
                    }
                }
            }
        }
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public static void playPageTurn(World world, BlockPos pos) {
        if (GeneralConfiguration.basePageTurnVolume > 0.01f) {
            SoundController.playPageturn(world, pos, GeneralConfiguration.basePageTurnVolume);
        }
    }


    public EnumStandState getState() {
        if (currentBook.isEmpty()) {
            return EnumStandState.EMPTY;
        } else if (pageNumber == 0) {
            return EnumStandState.CLOSED;
        } else {
            return EnumStandState.OPEN;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("book")) {
            currentBook = new ItemStack(compound.getCompoundTag("book"));
        } else {
            currentBook = ItemStack.EMPTY;
            pages = null;
            pageNumber = 0;
            result = null;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        if (!currentBook.isEmpty()) {
            NBTTagCompound compound = new NBTTagCompound();
            currentBook.writeToNBT(compound);
            tagCompound.setTag("book", compound);
        }
        return tagCompound;
    }

    @Override
    public boolean onActivate(EntityPlayer player) {
        boolean rc = super.onActivate(player);
        if (getWorld().isRemote) {
            if (pageNumber == 0 && !player.isSneaking()) {
                pageIncClient();
                return true;
            }
            if (result != null && !player.isSneaking()) {
                gotoPageClient(result);
                return true;
            }
            return false;
        }
        if (rc) {
            return rc;
        }

        if (!currentBook.isEmpty()) {
            if (player.isSneaking()) {
                InventoryHelper.giveItemToPlayer(player, currentBook);
                currentBook = ItemStack.EMPTY;
                markDirtyClient();
            }
            return true;
        }

        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (!heldItem.isEmpty()) {
            if (heldItem.getItem() instanceof IBook) {
                currentBook = heldItem.splitStack(1);
                player.openContainer.detectAndSendChanges();
                markDirtyClient();
                return true;
            } else {
                ITextComponent component = new TextComponentString(TextFormatting.YELLOW + "This is not a supported book!");
                if (player instanceof EntityPlayer) {
                    ((EntityPlayer) player).sendStatusMessage(component, false);
                } else {
                    player.sendMessage(component);
                }
                return false;
            }
        }
        return rc;
    }
}
