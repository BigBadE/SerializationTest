package software.bigbade.serializationtest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CraftMagicNumbers.class, Inventory.class, Bukkit.class, LogManager.class, ItemStack.class, ReflectionUtils.class})
public class SerializationTest {
    private final Inventory testInventory = new TestInventory();
    private final Inventory deserialized = mock(Inventory.class);
    private final World world = mock(World.class);
    private final ItemFactory itemFactory = mock(ItemFactory.class);
    private final Logger logger = mock(Logger.class);
    private CraftMagicNumbers magicNumbers;
    private Location location;

    @Before
    public void setupTest() throws Exception {
        mockStatic(ReflectionUtils.class);
        mockStatic(ItemStack.class);
        mockStatic(Bukkit.class);
        mockStatic(LogManager.class);
        mockStatic(CraftMagicNumbers.class);

        when(LogManager.getLogger()).thenReturn(logger);
        whenNew(CraftMagicNumbers.class).withNoArguments().thenReturn(magicNumbers);

        magicNumbers = mock(CraftMagicNumbers.class);

        when(magicNumbers.getDataVersion()).thenReturn(1);
        when(itemFactory.getItemMeta(Material.GOLD_INGOT)).thenReturn(CraftItemFactory.instance().getItemMeta(Material.GOLD_INGOT));
        when(Bukkit.getItemFactory()).thenReturn(itemFactory);
        when(Bukkit.getUnsafe()).thenReturn(magicNumbers);
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        for (int i = 0; i < 36; i++) {
            meta.setDisplayName("Test " + i);
            meta.setLore(Arrays.asList("Test item #" + i, "This is a test item"));
            meta.setCustomModelData(i);
            item.setItemMeta(meta.clone());
            testInventory.setItem(i, item.clone());
        }
        when(world.getName()).thenReturn("test");
        location = new Location(world, 1, 2, 3);
        when(Bukkit.getWorld("test")).thenReturn(world);
    }

    @Test
    public void testSerialization() {
        String serialized = SerializationUtils.serializeInventory(testInventory, false);
        SerializationUtils.deserializeInventory(deserialized, serialized);
        for (int i = 0; i < 36; i++) {
            System.out.println(testInventory.getItem(i).getItemMeta().getDisplayName());
            Mockito.verify(deserialized).setItem(i, testInventory.getItem(i));
        }
        String serializedLocation = SerializationUtils.serializeLocation(location);
        Location found = SerializationUtils.deserializationLocation(serializedLocation);
        Assert.assertEquals(found.hashCode(), location.hashCode());
    }
}

class TestInventory implements Inventory {
    private final List<ItemStack> items = new ArrayList<>();

    @Override
    public int getSize() {
        return 36;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setMaxStackSize(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack getItem(int i) {
        return items.get(i);
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        items.add(i, itemStack);
    }

    @Override
    public HashMap<Integer, ItemStack> addItem(org.bukkit.inventory.ItemStack... itemStacks) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public HashMap<Integer, ItemStack> removeItem(org.bukkit.inventory.ItemStack... itemStacks) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack[] getContents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setContents(org.bukkit.inventory.ItemStack[] itemStacks) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack[] getStorageContents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStorageContents(org.bukkit.inventory.ItemStack[] itemStacks) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Material material) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(org.bukkit.inventory.ItemStack itemStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Material material, int i) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(org.bukkit.inventory.ItemStack itemStack, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAtLeast(org.bukkit.inventory.ItemStack itemStack, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HashMap<Integer, ? extends ItemStack> all(Material material) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public HashMap<Integer, ? extends ItemStack> all(org.bukkit.inventory.ItemStack itemStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int first(Material material) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int first(org.bukkit.inventory.ItemStack itemStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int firstEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(Material material) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(org.bukkit.inventory.ItemStack itemStack) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<HumanEntity> getViewers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InventoryType getType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InventoryHolder getHolder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<ItemStack> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<ItemStack> iterator(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location getLocation() {
        throw new UnsupportedOperationException();
    }
}