package simulation.item;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import simulation.AbstractGameObject;

/**
 * This is a component that will contain an implementation fo the IContainer interface, other classes that implement the
 * IContainer interface can contain a field of this type and delegate all the methods to it.
 */
public class ContainerComponent extends AbstractGameObject implements IContainer, IItemAvailableListener {

    /** The content items. */
    private final Set<Item> items = new LinkedHashSet<>();

    private final IContainer container;

    /** Listeners for available items. */
    private final Map<ItemType, Set<IItemAvailableListener>> itemAvailableListeners = new ConcurrentHashMap<>();

    public ContainerComponent(final IContainer containerTmp) {
        container = containerTmp;
    }

    @Override
    public boolean addItem(final Item item) {
        boolean itemAdded = items.add(item);
        if (itemAdded) {
            item.addListener(this);
            if (!item.used) {
                notifyItemAvailable(item);
            }
        }
        return itemAdded;
    }

    @Override
    public boolean removeItem(final Item item) {
        boolean itemRemoved = items.remove(item);
        if (!itemRemoved) {
            for (Item container : getItems()) {
                if (container instanceof ContainerItem) {
                    if (((ContainerItem) container).removeItem(item)) {
                        itemRemoved = true;
                        break;
                    }
                }
            }
        }
        if (itemRemoved) {
            item.removeListener(this);
        }
        return itemRemoved;
    }

    @Override
    public Set<Item> getItems() {
        return items;
    }

    @Override
    public Item getUnusedItem(final String itemTypeName) {
        Item foundItem = null;
        for (Item item : items) {
            if (item.getType().name.equals(itemTypeName) && !item.isUsed() && !item.isDeleted() && !item.isPlaced()) {
                foundItem = item;
                break;
            }
            if (item instanceof IContainer) {
                Item contentItem = ((IContainer) item).getUnusedItem(itemTypeName);
                if (contentItem != null) {
                    return contentItem;
                }
            }
        }
        return foundItem;
    }

    @Override
    public Item getUnusedItemFromCategory(final String category) {
        Item foundItem = null;
        for (Item item : items) {
            if (item.getType().category.equals(category) && !item.isUsed() && !item.isDeleted() && !item.isPlaced()) {
                foundItem = item;
                break;
            }
            if (item instanceof IContainer) {
                Item contentItem = ((IContainer) item).getUnusedItemFromCategory(category);
                if (contentItem != null) {
                    return contentItem;
                }
            }
        }
        return foundItem;
    }

    @Override
    public int getItemQuantity(final ItemType itemType) {
        int count = 0;
        for (Item item : getItems()) {
            if (item.getType().equals(itemType)) {
                count++;
            }
            if (item instanceof IContainer) {
                count += ((IContainer) item).getItemQuantity(itemType);
            }
        }
        return count;
    }

    @Override
    public int getItemQuantity(final String category) {
        int count = 0;
        for (Item item : items) {
            if (item.getType().category.equals(category)) {
                count++;
            }
            if (item instanceof IContainer) {
                count += ((IContainer) item).getItemQuantity(category);
            }
        }
        return count;
    }

    @Override
    public void addListener(final ItemType itemType, final IItemAvailableListener listener) {
        if (!itemAvailableListeners.containsKey(itemType)) {
            itemAvailableListeners.put(itemType, new CopyOnWriteArraySet<IItemAvailableListener>());
        }
        itemAvailableListeners.get(itemType).add(listener);
    }

    @Override
    public void addListener(final String category, final IItemAvailableListener listener) {
        for (ItemType itemType : ItemTypeManager.getInstance().getItemTypes()) {
            if (itemType.category.equals(category)) {
                addListener(itemType, listener);
            }
        }
    }

    @Override
    public void removeListener(final ItemType itemType, final IItemAvailableListener listener) {
        if (itemAvailableListeners.containsKey(itemType)) {
            Set<IItemAvailableListener> listeners = itemAvailableListeners.get(itemType);
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                itemAvailableListeners.remove(itemType);
            }
        }
    }

    @Override
    public void removeListener(final String category, final IItemAvailableListener listener) {
        Set<ItemType> itemTypes = ItemTypeManager.getInstance().getItemTypesFromCategory(category);
        for (ItemType itemType : itemTypes) {
            removeListener(itemType, listener);
        }
    }

    /**
     * Notify all listeners that an item has become available.
     * @param item the listeners of this item type will be notified with this item
     */
    private void notifyItemAvailable(final Item item) {
        if (itemAvailableListeners.containsKey(item.getType())) {
            for (IItemAvailableListener listener : itemAvailableListeners.get(item.getType())) {
                listener.itemAvailable(item, container);
                if (item.isUsed()) {
                    break;
                }
            }
        }
    }

    @Override
    public void itemAvailable(final Item availableItem, final IContainer container) {
        notifyItemAvailable(availableItem);
    }
}
