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
package yadf.simulation.job;

import yadf.simulation.IPlayer;
import yadf.simulation.IRegion;
import yadf.simulation.character.IGameCharacter;
import yadf.simulation.character.component.ISkillComponent;
import yadf.simulation.item.IStockManager;
import yadf.simulation.item.Item;
import yadf.simulation.item.ItemType;
import yadf.simulation.item.ItemTypeManager;
import yadf.simulation.job.jobstate.AbstractJobState;
import yadf.simulation.job.jobstate.IJobState;
import yadf.simulation.job.jobstate.LookingForDwarfState;
import yadf.simulation.job.jobstate.WalkToPositionState;
import yadf.simulation.job.jobstate.WasteTimeState;
import yadf.simulation.labor.LaborType;
import yadf.simulation.labor.LaborTypeManager;
import yadf.simulation.map.BlockType;
import yadf.simulation.map.IMapListener;
import yadf.simulation.map.MapIndex;
import yadf.simulation.map.RegionMap;

/**
 * Task to do some mining.
 */
public class MineJob extends AbstractJob {

    /** Amount of time to spend mining (simulation steps). */
    private static final long DURATION = IRegion.SIMULATION_STEPS_PER_HOUR;

    /** The labor type required for this job. */
    private static final LaborType REQUIRED_LABOR = LaborTypeManager.getInstance().getLaborType("Mining");

    /** The index of the block to be mined. */
    private final MapIndex position;

    /** The miner dwarf. */
    private IGameCharacter miner;

    /** The map that will be mined. */
    final RegionMap map;

    /**
     * Constructor for the mine task.
     * @param positionTmp the position to mine
     * @param mapTmp the map that the construction will be built in
     * @param player the player
     */
    public MineJob(final MapIndex positionTmp, final RegionMap mapTmp, final IPlayer player) {
        super(player);
        position = positionTmp;
        map = mapTmp;
        setJobState(new WaitUntilAccessibleState());
    }

    @Override
    public MapIndex getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return "Mine";
    }

    @Override
    public void interrupt(final String message) {
        super.interrupt(message);
        if (miner != null) {
            miner.setAvailable(true);
        }
    }

    /**
     * Job state that waits until one of the blocks around the block to be mined is free.
     */
    private class WaitUntilAccessibleState extends AbstractJobState implements IMapListener {

        /**
         * Constructor.
         */
        public WaitUntilAccessibleState() {
            super(MineJob.this);
        }

        @Override
        public String toString() {
            return "Waiting until the mine site is accessible";
        }

        @Override
        public void start() {
            BlockType[] neighbourTypes = new BlockType[8];
            map.getNeighbourTypes(position, neighbourTypes);
            boolean accessible = false;
            for (BlockType blockType : neighbourTypes) {
                if (blockType.isStandIn) {
                    accessible = true;
                    break;
                }
            }
            if (accessible) {
                finishState();
            } else {
                map.addListener(this);
            }
        }

        @Override
        protected void doFinalActions() {
            map.removeListener(this);
        }

        @Override
        public IJobState getNextState() {
            return new LookingForMinerState();
        }

        @Override
        public void mapChanged(final MapIndex mapIndex) {
            BlockType blockType = map.getBlock(mapIndex);
            if (blockType.isStandIn
                    && (mapIndex.x == position.x || mapIndex.x == position.x - 1 || mapIndex.x == position.x + 1)
                    && (mapIndex.y == position.y || mapIndex.y == position.y - 1 || mapIndex.y == position.y + 1)) {
                finishState();
            }
        }

        @Override
        public void interrupt(final String message) {
            map.removeListener(this);
        }
    }

    /**
     * The looking for miner job state.
     */
    private class LookingForMinerState extends LookingForDwarfState {

        /**
         * Constructor.
         */
        public LookingForMinerState() {
            super(REQUIRED_LABOR, MineJob.this);
        }

        @Override
        protected void doFinalActions() {
            miner = getDwarf();
        }

        @Override
        public IJobState getNextState() {
            return new WalkToMiningSiteState();
        }
    }

    /**
     * The walk to mining site job state.
     */
    private class WalkToMiningSiteState extends WalkToPositionState {

        /**
         * Constructor.
         */
        public WalkToMiningSiteState() {
            super(position, miner, true, MineJob.this);
        }

        @Override
        public IJobState getNextState() {
            return new MineState();
        }
    }

    /**
     * The mine job state.
     */
    private class MineState extends WasteTimeState {

        /**
         * Constructor.
         */
        public MineState() {
            super(DURATION, miner, MineJob.this);
        }

        @Override
        protected void doFinalActions() {
            String itemTypeName = map.getBlock(position).itemMined;
            if (itemTypeName != null) {
                ItemType itemType = ItemTypeManager.getInstance().getItemType(itemTypeName);
                Item blockItem = new Item(position, itemType, getPlayer());
                getPlayer().getComponent(IStockManager.class).getUnstoredItemManager().addGameObject(blockItem);
            }
            map.mineBlock(position);
            miner.getComponent(ISkillComponent.class).increaseSkillLevel(REQUIRED_LABOR);
            miner.setAvailable(true);
        }

        @Override
        public IJobState getNextState() {
            return null;
        }
    }
}
