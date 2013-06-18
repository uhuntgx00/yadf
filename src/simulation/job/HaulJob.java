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
package simulation.job;

import logger.Logger;
import simulation.IPlayer;
import simulation.character.GameCharacter;
import simulation.character.component.IInventoryComponent;
import simulation.item.IContainer;
import simulation.item.Item;
import simulation.item.ItemType;
import simulation.job.jobstate.IJobState;
import simulation.job.jobstate.LookingForDwarfState;
import simulation.job.jobstate.LookingForItemState;
import simulation.job.jobstate.WalkToPositionState;
import simulation.labor.LaborType;
import simulation.labor.LaborTypeManager;
import simulation.map.MapIndex;

/**
 * The Class HaulJob.
 * 
 * A job which hauls an item to some place.
 */
public class HaulJob extends AbstractJob {

    /** The serial version UID. */
    private static final long serialVersionUID = -1705328544980248473L;

    /** The labor type required for this job. */
    private static final LaborType REQUIRED_LABOR = LaborTypeManager.getInstance().getLaborType("Hauling");

    /** The character that will haul the item. */
    private GameCharacter hauler;

    /** The item to be hauled. */
    private Item item;

    /** If not null, the item should be stored here. */
    private final IContainer container;

    /** The drop position. */
    private final MapIndex dropPosition;

    /** The lock on the dwarf needs to be released, it should only be released if we acquired the dwarf. */
    private boolean needToReleaseLock = false;

    /** The item type. */
    private final ItemType itemType;

    /**
     * Instantiates a new haul job, start with dwarf and item.
     * @param characterTmp the character
     * @param itemTmp the item
     * @param containerTmp the container to put the item in
     * @param dropPositionTmp the drop position
     */
    public HaulJob(final GameCharacter characterTmp, final Item itemTmp, final IContainer containerTmp,
            final MapIndex dropPositionTmp) {
        super(characterTmp.getPlayer());
        item = itemTmp;
        container = containerTmp;
        dropPosition = dropPositionTmp;
        hauler = characterTmp;
        itemType = item.getType();
        setJobState(new WalkToItemState());
    }

    @Override
    public MapIndex getPosition() {
        return dropPosition;
    }

    /**
     * Instantiates a new haul job, start with item.
     * @param itemTmp the item
     * @param containerTmp the container to put the item in
     * @param dropPositionTmp the drop position
     * @param playerTmp the player
     */
    public HaulJob(final Item itemTmp, final IContainer containerTmp, final MapIndex dropPositionTmp,
            final IPlayer playerTmp) {
        super(playerTmp);
        item = itemTmp;
        container = containerTmp;
        dropPosition = dropPositionTmp;
        itemType = item.getType();
        needToReleaseLock = true;
        setJobState(new LookingForHaulerState());
    }

    /**
     * Instantiates a new haul job.
     * @param itemTypeTmp the item
     * @param containerTmp the container to put the item in
     * @param dropPositionTmp the drop position
     * @param playerTmp the player
     */
    public HaulJob(final ItemType itemTypeTmp, final IContainer containerTmp, final MapIndex dropPositionTmp,
            final IPlayer playerTmp) {
        super(playerTmp);
        container = containerTmp;
        dropPosition = dropPositionTmp;
        itemType = itemTypeTmp;
        needToReleaseLock = true;
        setJobState(new LookingForHaulItemState());
    }

    @Override
    public String toString() {
        return "Hauling " + item.getType().name;
    }

    /**
     * Gets the drop position.
     * @return the drop position
     */
    public MapIndex getDropPosition() {
        return dropPosition;
    }

    /**
     * Gets the item.
     * @return the item
     */
    public Item getItem() {
        return item;
    }

    /**
     * Looking for haul item job state.
     */
    private class LookingForHaulItemState extends LookingForItemState {

        /**
         * Constructor.
         */
        public LookingForHaulItemState() {
            super(itemType, HaulJob.this);
        }

        @Override
        public IJobState getNextState() {
            IJobState nextState;
            if (hauler == null) {
                nextState = new LookingForHaulerState();
            } else {
                nextState = new WalkToItemState();
            }
            return nextState;
        }
    }

    /**
     * Looking for hauler job state.
     */
    private class LookingForHaulerState extends LookingForDwarfState {

        /**
         * Constructor.
         */
        public LookingForHaulerState() {
            super(REQUIRED_LABOR, HaulJob.this);
        }

        @Override
        public void transitionOutOf() {
            hauler = getDwarf();
        }

        @Override
        public IJobState getNextState() {
            return new WalkToItemState();
        }
    }

    /**
     * Walk to item job state.
     */
    private class WalkToItemState extends WalkToPositionState {

        /**
         * Constructor.
         */
        public WalkToItemState() {
            super(item.getPosition(), hauler, HaulJob.this);
        }

        @Override
        public void transitionOutOf() {
            super.transitionOutOf();
            getPlayer().getStockManager().removeItem(item);
            hauler.getComponent(IInventoryComponent.class).pickupHaulItem(item);
        }

        @Override
        public IJobState getNextState() {
            return new WalkToDropState();
        }

    }

    /**
     * Walk to drop job state.
     */
    private class WalkToDropState extends WalkToPositionState {

        /**
         * Constructor.
         */
        public WalkToDropState() {
            super(dropPosition, hauler, HaulJob.this);
        }

        @Override
        public void transitionOutOf() {
            hauler.getComponent(IInventoryComponent.class).dropHaulItem(false);
            if (needToReleaseLock) {
                hauler.releaseLock();
            }
            if (container != null) {
                if (container.getRemove()) {
                    Logger.getInstance().log(this, "Can't store item, container has been removed", true);
                    getPlayer().getStockManager().addItem(item);
                } else {
                    if (!container.addItem(item)) {
                        Logger.getInstance().log(this, "Can't store item, container didn't accept it", true);
                        getPlayer().getStockManager().addItem(item);
                    }
                }
            }
        }

        @Override
        public IJobState getNextState() {
            return null;
        }
    }
}
