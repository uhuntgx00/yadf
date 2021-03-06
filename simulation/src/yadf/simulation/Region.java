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
package yadf.simulation;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import yadf.logger.Logger;
import yadf.simulation.map.MapIndex;
import yadf.simulation.map.RegionMap;
import yadf.simulation.tree.ITreeManager;
import yadf.simulation.tree.TreeManager;

/**
 * Region Contains all the data for a region, including the map, players and trees.
 */
public class Region implements IRegion {

    /** The Map of this region. */
    private final RegionMap map = new RegionMap();

    /** The tree manager for this region. */
    private final TreeManager treeManager = new TreeManager(this);

    /** A vector of all the players in this region. */
    private final Set<IPlayer> players = new LinkedHashSet<>();

    /** The time. */
    private long time;

    /** Listeners to be notified at a certain time. */
    private final Map<Long, Set<ITimeListener>> timeListeners = new HashMap<>();

    /** Is it paused? */
    private boolean paused = false;

    /**
     * Setup the region.
     * @param regionSize the size of the region
     */
    public void setup(final MapIndex regionSize) {
        Logger.getInstance().log(this, "Setting up");
        map.generateMap(regionSize);
        treeManager.addTrees();
    }

    /**
     * Adds the player.
     * @param player the player
     */
    public void addPlayer(final IPlayer player) {
        Logger.getInstance().log(this, "Adding player " + player.getName());
        players.add(player);
    }

    @Override
    public ITreeManager getTreeManager() {
        return treeManager;
    }

    @Override
    public RegionMap getMap() {
        return map;
    }

    /**
     * Gets the player with passed id.
     * @param playerId the player id
     * @return the player
     */
    public IPlayer getPlayer(final int playerId) {
        for (IPlayer player : players) {
            if (player.getId() == playerId) {
                return player;
            }
        }
        return null;
    }

    @Override
    public Set<IPlayer> getPlayers() {
        return players;
    }

    /**
     * Gets a string representing the current time.
     * @return the time string
     */
    public String getTimeString() {
        long hour = time / SIMULATION_STEPS_PER_HOUR;
        long day = time / SIMULATION_STEPS_PER_DAY;
        long year = time / SIMULATION_STEPS_PER_YEAR;
        return "Year " + year + " Day " + (long) (day % DAYS_IN_A_YEAR) + " Hour " + hour % HOURS_IN_A_DAY;
    }

    /**
     * Update.
     */
    public void update() {
        if (!paused) {
            time++;
            if (timeListeners.containsKey(time)) {
                for (ITimeListener listener : timeListeners.remove(time)) {
                    listener.notifyTimeEvent();
                }
            }
            for (IPlayer player : players) {
                player.update();
            }
        }
    }

    /**
     * Toggle pause.
     */
    public void togglePause() {
        paused = !paused;
    }

    @Override
    public long addTimeListener(final long duration, final ITimeListener listener) {
        long notifyTime = time + duration;
        if (!timeListeners.containsKey(notifyTime)) {
            timeListeners.put(notifyTime, new LinkedHashSet<ITimeListener>());
        }
        timeListeners.get(notifyTime).add(listener);
        return notifyTime;
    }

    @Override
    public void removeTimeListener(final long notifyTime, final ITimeListener listener) {
        if (timeListeners.containsKey(notifyTime)) {
            timeListeners.get(notifyTime).remove(listener);
        }
    }
}
