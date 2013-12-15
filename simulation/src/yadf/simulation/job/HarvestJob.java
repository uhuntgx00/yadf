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
import yadf.simulation.farm.IFarmPlot;
import yadf.simulation.item.IStockManager;
import yadf.simulation.item.Item;
import yadf.simulation.item.ItemType;
import yadf.simulation.item.ItemTypeManager;
import yadf.simulation.job.jobstate.IJobState;
import yadf.simulation.job.jobstate.LookingForDwarfState;
import yadf.simulation.job.jobstate.WalkToPositionState;
import yadf.simulation.job.jobstate.WasteTimeState;
import yadf.simulation.labor.LaborType;
import yadf.simulation.labor.LaborTypeManager;
import yadf.simulation.map.MapIndex;

/**
 * The Class HarvestJob.
 */
public class HarvestJob extends AbstractJob {

    /** The labor type required for this job. */
    private static final LaborType REQUIRED_LABOR = LaborTypeManager.getInstance().getLaborType("Farming");

    /** The Constant harvestDuration. */
    private static final long HARVEST_DURATION = 2 * IRegion.SIMULATION_STEPS_PER_HOUR;

    /** The farm plot. */
    private final IFarmPlot farmPlot;

    /** The farmer dwarf. */
    private IGameCharacter farmer;

    /**
     * Instantiates a new harvest job.
     * @param farmPlotTmp the farm plot
     * @param player the player that this job belongs to
     */
    public HarvestJob(final IFarmPlot farmPlotTmp, final IPlayer player) {
        super(player);
        farmPlot = farmPlotTmp;
        setJobState(new LookingForFarmerState());
    }

    @Override
    public String toString() {
        return "Harvest crop";
    }

    @Override
    public MapIndex getPosition() {
        return farmPlot.getPosition();
    }

    @Override
    public void interrupt(final String message) {
        super.interrupt(message);
        if (farmer != null) {
            farmer.setAvailable(true);
        }
    }

    /**
     * The waiting for farmer job state.
     */
    private class LookingForFarmerState extends LookingForDwarfState {

        /**
         * Constructor.
         */
        public LookingForFarmerState() {
            super(REQUIRED_LABOR, HarvestJob.this);
        }

        @Override
        protected void doFinalActions() {
            farmer = getDwarf();
        }

        @Override
        public IJobState getNextState() {
            return new WalkToCropState();
        }
    }

    /**
     * The walk to crop state.
     */
    private class WalkToCropState extends WalkToPositionState {

        /**
         * Constructor.
         */
        public WalkToCropState() {
            super(farmPlot.getPosition(), farmer, false, HarvestJob.this);
        }

        @Override
        public IJobState getNextState() {
            return new HarvestCropState();
        }
    }

    /**
     * The harvest crop state.
     */
    private class HarvestCropState extends WasteTimeState {

        /**
         * Constructor.
         */
        public HarvestCropState() {
            super(HARVEST_DURATION, farmer, HarvestJob.this);
        }

        @Override
        protected void doFinalActions() {
            ItemType itemType = ItemTypeManager.getInstance().getItemType("Wheat");
            Item newItem = new Item(farmPlot.getPosition(), itemType, getPlayer());
            getPlayer().getComponent(IStockManager.class).getUnstoredItemManager().addGameObject(newItem);
            itemType = ItemTypeManager.getInstance().getItemType("Seed");
            newItem = new Item(farmPlot.getPosition(), itemType, getPlayer());
            getPlayer().getComponent(IStockManager.class).getUnstoredItemManager().addGameObject(newItem);
            farmer.getComponent(ISkillComponent.class).increaseSkillLevel(REQUIRED_LABOR);
            farmer.setAvailable(true);
        }

        @Override
        public IJobState getNextState() {
            return null;
        }
    }
}
