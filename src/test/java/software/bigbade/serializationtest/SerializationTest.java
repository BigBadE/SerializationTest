package software.bigbade.serializationtest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftMagicNumbers;
import org.bukkit.inventory.Inventory;
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

import java.util.Arrays;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CraftMagicNumbers.class, Inventory.class, Bukkit.class, LogManager.class, ItemStack.class})
public class SerializationTest {
    private final Inventory testInventory = mock(Inventory.class);
    private final Inventory deserialized = mock(Inventory.class);
    private final World world = mock(World.class);
    private final ItemFactory itemFactory = mock(ItemFactory.class);
    private final Logger logger = mock(Logger.class);
    private CraftMagicNumbers magicNumbers;
    private Location location;

    private static final int MAX_ITEMS = 1;

    @Before
    public void setupTest() throws Exception {
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
        for (int i = 0; i < MAX_ITEMS; i++) {
            meta.setDisplayName("Test " + i);
            meta.setLore(Arrays.asList("Test item #" + i, "This is a test item"));
            meta.setCustomModelData(i);
            item.setItemMeta(meta.clone());
            when(testInventory.getItem(i)).thenReturn(item.clone());
        }

        when(world.getName()).thenReturn("test");
        location = new Location(world, 1, 2, 3);
        when(Bukkit.getWorld("test")).thenReturn(world);
    }

    @Test
    public void testSerialization() {
        String serialized = SerializationUtils.serializeInventory(testInventory, false);
        SerializationUtils.deserializeInventory(deserialized, serialized);
        for (int i = 0; i < MAX_ITEMS; i++) {
            Mockito.verify(deserialized).setItem(i, testInventory.getItem(i));
        }
        String serializedLocation = SerializationUtils.serializeLocation(location);
        Location found = SerializationUtils.deserializationLocation(serializedLocation);
        Assert.assertEquals(found.hashCode(), location.hashCode());
    }
}