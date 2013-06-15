/**
 * yadf
 * 
 * https://sourceforge.net/projects/yadf
 * 
 * Ben Smith (bensmith87@gmail.com)
 * 
 * yadf is placed under the BSD license.
 * 
 * Copyright (c) 2012-2013, Ben Smith All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 * 
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 
 * - Neither the name of the yadf project nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package simulation.stock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import logger.Logger;
import simulation.AbstractGameObject;
import simulation.Player;
import simulation.item.ContainerItem;
import simulation.item.Item;
import simulation.item.ItemType;
import simulation.map.MapArea;
import simulation.map.MapIndex;

/**
 * The Class StockManager, manages items for a player including stockpiles.
 */
public class StockManager extends AbstractGameObject implements IStockManager, Serializable {

    /** The serial version UID. */
    private static final long serialVersionUID = 2947690954735554619L;

    /** The stockpiles owned by this stock manager. */
    private final Set<Stockpile> stockpiles = new HashSet<>();

    /** All the items looked after by this stock manager. */
    private final Set<Item> items = new HashSet<>();

    /** Listeners to this stock manager. */
    private final Map<ItemType, Set<IStockManagerListener>> listeners = new HashMap<>();

    @Override
    public boolean addItem(final Item item) {
        Logger.getInstance().log(this, "Adding item - itemType: " + item.getType());
        items.add(item);
        notifyListeners(item.getType());
        return true;
    }

    /**
     * Gets all the unstored items.
     * @return A list of references to all the unstored items
     */
    public Set<Item> getItems() {
        return items;
    }

    /**
     * Get an item located at a specific map position.
     * @param index the map position
     * @return the item, null if no item found
     */
    public Item getItem(final MapIndex index) {
        for (Item item : items) {
            if (index.equals(item.getPosition())) {
                return item;
            }
        }
        for (Stockpile stockpile : stockpiles) {
            Item item = stockpile.getItem(index);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /**
     * Gets the item count.
     * @param itemType the item type
     * @return the item count
     */
    public int getItemQuantity(final ItemType itemType) {
        int count = 0;
        for (Item item : items) {
            if (item.getType().equals(itemType)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get the number of items in a category.
     * @param category the category
     * @return the number of items in the category
     */
    public int getItemQuantity(final String category) {
        int count = 0;
        for (Item item : items) {
            if (item.getType().category.equals(category)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public void addListener(final ItemType itemType, final IStockManagerListener listener) {
        if (!listeners.containsKey(itemType)) {
            listeners.put(itemType, new HashSet<IStockManagerListener>());
        }
        listeners.get(itemType).add(listener);
    }

    @Override
    public void removeListener(final ItemType itemType, final IStockManagerListener listener) {
        if (listeners.containsKey(itemType)) {
            listeners.get(itemType).remove(listener);
        }
    }

    @Override
    public void removeListener(final IStockManagerListener listener) {
        for (Set<IStockManagerListener> listenersTmp : listeners.values()) {
            listenersTmp.remove(listener);
        }
    }

    /**
     * Notify all listeners that something has changed in the stock manager.
     * @param itemType the listeners of this item type will be notified
     */
    private void notifyListeners(final ItemType itemType) {
        for (IStockManagerListener listener : listeners.get(itemType)) {
            listener.stockManagerChanged();
        }
    }

    /**
     * Finds an item that is not stored, including containers.
     * @param itemTypes the item types to find
     * @return A reference to the found item, will be null if none could be found
     */
    @Override
    public Item getUnstoredItem(final List<ItemType> itemTypes) {
        Item foundItem = null;
        for (Item item : items) {
            if (item.canBeStored(itemTypes) && !item.isUsed()) {
                foundItem = item;
                break;
            }
        }
        return foundItem;
    }

    /**
     * Finds an item that is not stored, not including containers.
     * @param itemType the item type to find
     * @return A reference to the found item, will be null if none could be found
     */
    public Item getUnstoredItem(final ItemType itemType) {
        for (Item item : items) {
            // TODO: move this into item
            if (itemType.equals(item.getType()) && !item.isUsed() && !item.getRemove() && !item.isPlaced()) {
                return item;
            }
        }
        return null;
    }

    @Override
    public Item getUnusedItem(final String itemTypeName) {
        for (Stockpile stockpile : stockpiles) {
            Item item = stockpile.getUnusedItem(itemTypeName);
            if (item != null) {
                return item;
            }
        }
        for (Item item : items) {
            if (item.getType().equals(itemTypeName) && !item.isUsed() && !item.getRemove() && !item.isPlaced()) {
                return item;
            }
            // TODO: can this be done neatly without using instance of?
            if (item instanceof ContainerItem) {
                Item contentItem = ((ContainerItem) item).getUnusedItem(itemTypeName);
                if (contentItem != null) {
                    return contentItem;
                }
            }
        }
        return null;
    }

    @Override
    public Item getUnusedItemFromCategory(final String category) {
        for (Stockpile stockpile : stockpiles) {
            Item item = stockpile.getUnusedItemFromCategory(category);
            if (item != null) {
                return item;
            }
        }
        for (Item item : this.items) {
            if (item.getType().category.equals(category) && !item.isUsed() && !item.getRemove() && !item.isPlaced()) {
                return item;
            }
            // TODO: can this be done neatly without using instance of?
            if (item instanceof ContainerItem) {
                Item contentItem = ((ContainerItem) item).getUnusedItemFromCategory(category);
                if (contentItem != null) {
                    return contentItem;
                }
            }
        }

        return null;
    }

    @Override
    public boolean removeItem(final Item item) {
        Logger.getInstance().log(this, "Removing item - itemType: " + item.getType());
        boolean itemRemoved = false;
        itemRemoved = items.remove(item);
        if (!itemRemoved) {
            // TODO: change this to loop through all stockpiles and ask them to remove item until it finds the right
            // one.
            Stockpile stockpile = getStockpile(item.getPosition());
            if (stockpile != null) {
                itemRemoved = stockpile.removeItem(item);
            }
        }
        if (!itemRemoved) {
            for (Item container : items) {
                if (container instanceof ContainerItem) {
                    if (((ContainerItem) container).removeItem(item)) {
                        itemRemoved = true;
                        break;
                    }
                }
            }
        }
        if (itemRemoved) {
            notifyListeners(item.getType());
        }
        return itemRemoved;
    }

    /**
     * Adds a new stockpile to this stock manager.
     * @param stockpile The new stockpile
     */
    public void addStockpile(final Stockpile stockpile) {
        stockpiles.add(stockpile);
    }

    /**
     * Gets the stockpile.
     * @param stockpileId the stockpile id
     * @return the stockpile
     */
    public Stockpile getStockpile(final int stockpileId) {
        for (Stockpile stockpile : stockpiles) {
            if (stockpile.getId() == stockpileId) {
                return stockpile;
            }
        }
        return null;
    }

    /**
     * Gets the stockpile.
     * @param index the index
     * @return the stockpile
     */
    public Stockpile getStockpile(final MapIndex index) {
        for (Stockpile stockpile : stockpiles) {
            MapArea area = stockpile.getArea();
            if (index.x >= area.pos.x && index.x < area.pos.x + area.width && index.y >= area.pos.y
                    && index.y < area.pos.y + area.height && area.pos.z == index.z) {
                return stockpile;
            }
        }
        return null;
    }

    /**
     * Gets references to all the stockpiles.
     * @return A vector of references to all the stockpiles
     */
    public Set<Stockpile> getStockpiles() {
        return stockpiles;
    }

    /**
     * Updates all the stockpiles, and removes items that have been tagged for removal.
     * @param player the player
     */
    public void update(final Player player) {
        for (Stockpile stockpile : stockpiles.toArray(new Stockpile[0])) {
            if (stockpile.getRemove()) {
                stockpiles.remove(stockpile);
            }
        }
        for (Item item : items.toArray(new Item[0])) {
            if (item.getRemove()) {
                items.remove(item);
            }
        }
    }
}
