package simulation.job.jobstate;

import simulation.item.IStockManagerListener;
import simulation.item.Item;
import simulation.item.ItemType;
import simulation.job.AbstractJob;

/**
 * Generic looking for item job state.
 */
public abstract class LookingForItemState extends AbstractJobState implements IStockManagerListener {

    /** The required itemType. */
    private final ItemType itemType;

    /** The required category. */
    private final String category;

    /** The found item. */
    private Item item;

    /**
     * Constructor.
     * @param itemTypeTmp the type of item to look for
     * @param jobTmp the job that this state belongs to
     */
    public LookingForItemState(final ItemType itemTypeTmp, final AbstractJob jobTmp) {
        super(jobTmp);
        itemType = itemTypeTmp;
        category = null;
    }

    /**
     * Constructor.
     * @param categoryTmp the category of item to look for
     * @param jobTmp the job that this state belongs to
     */
    public LookingForItemState(final String categoryTmp, final AbstractJob jobTmp) {
        super(jobTmp);
        category = categoryTmp;
        itemType = null;
    }

    @Override
    public String toString() {
        return "Looking for item";
    }

    @Override
    public void transitionInto() {
        if (itemType != null) {
            item = getJob().getPlayer().getStockManager().getUnusedItem(itemType.name);
        } else {
            item = getJob().getPlayer().getStockManager().getUnusedItemFromCategory(category);
        }
        if (item == null) {
            if (itemType != null) {
                getJob().getPlayer().getStockManager().addListener(itemType, this);
            } else {
                // TODO: add listener for all item types in category
                getJob().getPlayer().getStockManager().addListener(this);
            }
        } else {
            getJob().stateDone(this);
        }
    }

    @Override
    public void transitionOutOf() {
        getJob().getPlayer().getStockManager().removeListener(this);
    }

    @Override
    public void itemNowAvailable(final Item availableItem) {
        if (!availableItem.isUsed()) {
            availableItem.setUsed(true);
            item = availableItem;
            getJob().stateDone(this);
        }
    }

    /**
     * Get the item that was found.
     * @return the item
     */
    public Item getItem() {
        return item;
    }
}
