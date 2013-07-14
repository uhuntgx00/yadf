package simulation.job;

import java.util.Set;

import simulation.IRegion;
import simulation.character.IGameCharacter;
import simulation.character.component.ISleepComponent;
import simulation.item.Item;
import simulation.job.jobstate.IJobState;
import simulation.job.jobstate.WalkToPositionState;
import simulation.job.jobstate.WasteTimeState;
import simulation.map.MapIndex;
import simulation.room.IRoomManager;
import simulation.room.Room;

/**
 * The dwarf looks for somewhere to sleep, walks there, then sleeps.
 */
public class SleepJob extends AbstractJob {

    /** How many simulation steps the dwarf should sleep for. */
    private static final long SLEEP_DURATION = IRegion.SIMULATION_STEPS_PER_HOUR;

    /** The dwarf that wants to sleep. */
    private final IGameCharacter dwarf;

    /** The bed that the dwarf sleeps in, null if he can't find one. */
    private Item bed;

    /**
     * Constructor.
     * @param dwarfTmp the dwarf that want's to sleep
     */
    public SleepJob(final IGameCharacter dwarfTmp) {
        super(dwarfTmp.getPlayer());
        dwarf = dwarfTmp;
        setJobState(new WaitingForDwarfState());
    }

    @Override
    public String toString() {
        return "Sleep";
    }

    @Override
    public MapIndex getPosition() {
        return dwarf.getPosition();
    }

    @Override
    public void interrupt(final String message) {
        super.interrupt(message);
        if (dwarf != null) {
            dwarf.setFree();
        }
        if (bed != null) {
            bed.setUsed(false);
        }
    }

    /**
     * The waiting for dwarf job state.
     */
    private class WaitingForDwarfState extends simulation.job.jobstate.WaitingForDwarfState {

        /**
         * Constructor.
         */
        public WaitingForDwarfState() {
            super(dwarf, SleepJob.this);
        }

        @Override
        protected void doFinalActions() {
            Set<Room> rooms = getPlayer().getComponent(IRoomManager.class).getRooms();
            for (Room room : rooms) {
                // TODO: do I have my own room?
                if (room.getType().equals("Dormitory")) {
                    bed = room.getItem("Bed", false, true);
                    if (bed != null) {
                        bed.setUsed(true);
                        break;
                    }
                }
            }
        }

        @Override
        public IJobState getNextState() {
            IJobState nextState;
            if (bed != null) {
                nextState = new WalkToBedState();
            } else {
                nextState = new SleepState();
            }
            return nextState;
        }
    }

    /**
     * The walk to bed job state.
     */
    private class WalkToBedState extends WalkToPositionState {

        /**
         * Constructor.
         */
        public WalkToBedState() {
            super(bed.getPosition(), dwarf, false, SleepJob.this);
        }

        @Override
        public IJobState getNextState() {
            return new SleepState();
        }
    }

    /**
     * The sleep job state.
     */
    private class SleepState extends WasteTimeState {

        /**
         * Constructor.
         */
        public SleepState() {
            super(SLEEP_DURATION, dwarf, SleepJob.this);
        }

        @Override
        protected void doFinalActions() {
            dwarf.getComponent(ISleepComponent.class).sleep();
            dwarf.setFree();
            if (bed != null) {
                bed.setUsed(false);
            }
        }

        @Override
        public IJobState getNextState() {
            return null;
        }
    }
}
