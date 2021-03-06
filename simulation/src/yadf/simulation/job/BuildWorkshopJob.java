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

import java.util.Set;

import yadf.simulation.IPlayer;
import yadf.simulation.IRegion;
import yadf.simulation.character.IGameCharacter;
import yadf.simulation.character.component.ISkillComponent;
import yadf.simulation.item.Item;
import yadf.simulation.job.jobstate.HaulResourcesState;
import yadf.simulation.job.jobstate.IJobState;
import yadf.simulation.job.jobstate.LookingForDwarfState;
import yadf.simulation.job.jobstate.WalkToPositionState;
import yadf.simulation.job.jobstate.WasteTimeState;
import yadf.simulation.labor.LaborType;
import yadf.simulation.labor.LaborTypeManager;
import yadf.simulation.map.MapIndex;
import yadf.simulation.workshop.IWorkshop;
import yadf.simulation.workshop.IWorkshopManager;
import yadf.simulation.workshop.Workshop;
import yadf.simulation.workshop.WorkshopType;

/**
 * The Class BuildWorkshopJob.
 */
public class BuildWorkshopJob extends AbstractJob {

    /** How long it takes to build the workshop. */
    private static final long BUILD_DURATION = IRegion.SIMULATION_STEPS_PER_DAY;

    /** The labor type required for this job. */
    private static final LaborType REQUIRED_LABOR = LaborTypeManager.getInstance().getLaborType("Building");

    /** The position. */
    private final MapIndex position;

    /** The workshop type. */
    private final WorkshopType workshopType;

    /** The dwarf that will do the building. */
    private IGameCharacter builder;

    /** The hauled building resources. */
    private Set<Item> resources;

    /**
     * Instantiates a new build workshop job.
     * @param positionTmp the position
     * @param workshopTypeTmp the workshop type
     * @param player the player that this job belongs to
     */
    public BuildWorkshopJob(final MapIndex positionTmp, final WorkshopType workshopTypeTmp, final IPlayer player) {
        super(player);
        position = positionTmp;
        workshopType = workshopTypeTmp;
        setJobState(new HaulBuildingMaterialsState());
    }

    @Override
    public String toString() {
        String string = "Building ";
        if (workshopType != null) {
            string += workshopType.name;
        }
        return string;
    }

    @Override
    public MapIndex getPosition() {
        return position;
    }

    @Override
    public void interrupt(final String message) {
        super.interrupt(message);
        if (builder != null) {
            builder.setAvailable(true);
        }
        if (resources != null) {
            for (Item resource : resources) {
                resource.setAvailable(true);
            }
        }
    }

    /**
     * The haul building materials job state.
     */
    private class HaulBuildingMaterialsState extends HaulResourcesState {

        /**
         * Constructor.
         */
        public HaulBuildingMaterialsState() {
            super(workshopType.resources, position, BuildWorkshopJob.this);
        }

        @Override
        protected void doFinalActions() {
            resources = getResources();
        }

        @Override
        public IJobState getNextState() {
            return new LookingForBuilderState();
        }
    }

    /**
     * The looking for builder job state.
     */
    private class LookingForBuilderState extends LookingForDwarfState {

        /**
         * Constructor.
         */
        public LookingForBuilderState() {
            super(REQUIRED_LABOR, BuildWorkshopJob.this);
        }

        @Override
        protected void doFinalActions() {
            builder = getDwarf();
        }

        @Override
        public IJobState getNextState() {
            return new WalkToBuildingSiteState();
        }
    }

    /**
     * The walk to building site job state.
     */
    private class WalkToBuildingSiteState extends WalkToPositionState {

        /**
         * Constructor.
         */
        public WalkToBuildingSiteState() {
            super(position, builder, false, BuildWorkshopJob.this);
        }

        @Override
        public IJobState getNextState() {
            return new BuildWorkshopState();
        }
    }

    /**
     * The build workshop state.
     */
    private class BuildWorkshopState extends WasteTimeState {

        /**
         * Constructor.
         */
        public BuildWorkshopState() {
            super(BUILD_DURATION, builder, BuildWorkshopJob.this);
        }

        @Override
        protected void doFinalActions() {
            IWorkshop workshop = new Workshop(getPlayer(), workshopType, position);
            getPlayer().getComponent(IWorkshopManager.class).addGameObject(workshop);
            builder.getComponent(ISkillComponent.class).increaseSkillLevel(REQUIRED_LABOR);
            builder.setAvailable(true);
            for (Item resource : resources) {
                resource.delete();
            }
        }

        @Override
        public IJobState getNextState() {
            return null;
        }
    }
}
