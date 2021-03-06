package yadf.simulation;

import java.util.Set;

import yadf.simulation.map.RegionMap;
import yadf.simulation.tree.ITreeManager;

/**
 * Interface for a region.
 */
public interface IRegion {

    /** How many hours in one day. */
    int HOURS_IN_A_DAY = 24;

    /** How many days in one year. */
    double DAYS_IN_A_YEAR = 365.242;

    /** The number of simulation steps in one minute. */
    long SIMULATION_STEPS_PER_MINUTE = 1;

    /** The number of simulation steps in one hour. */
    long SIMULATION_STEPS_PER_HOUR = SIMULATION_STEPS_PER_MINUTE * 60;

    /** The number of simulation steps in one day. */
    long SIMULATION_STEPS_PER_DAY = SIMULATION_STEPS_PER_HOUR * 24;

    /** The number of simulation steps in one week. */
    long SIMULATION_STEPS_PER_WEEK = SIMULATION_STEPS_PER_DAY * 7;

    /** The number of simulation steps in one month. */
    long SIMULATION_STEPS_PER_MONTH = (long) (SIMULATION_STEPS_PER_DAY * 30.4368);

    /** The number of simulation steps in one season. */
    long SIMULATION_STEPS_PER_SEASON = (long) (SIMULATION_STEPS_PER_DAY * 91.3105);

    /** The number of simulation steps in one year. */
    long SIMULATION_STEPS_PER_YEAR = (long) (SIMULATION_STEPS_PER_DAY * DAYS_IN_A_YEAR);

    /**
     * Gets the map.
     * @return the map
     */
    RegionMap getMap();

    /**
     * Gets all the players.
     * @return the players
     */
    Set<IPlayer> getPlayers();

    /**
     * Get the tree manager.
     * @return the tree manager
     */
    ITreeManager getTreeManager();

    /**
     * Add a time listener.
     * @param duration how long until they want to be notified
     * @param listener the listener to add
     * @return the time which they will be notified
     */
    long addTimeListener(long duration, ITimeListener listener);

    /**
     * Remove a time listener.
     * @param notifyTime the time which they were being notified
     * @param listener the listener to remove
     */
    void removeTimeListener(long notifyTime, ITimeListener listener);
}
