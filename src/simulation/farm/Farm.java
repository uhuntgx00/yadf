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
package simulation.farm;

import java.util.ArrayList;
import java.util.List;

import simulation.GameObject;
import simulation.Player;
import simulation.map.MapArea;
import simulation.map.MapIndex;

/**
 * The Class Farm.
 */
public class Farm extends GameObject {

    /** The area. */
    private final MapArea area;

    /** The farm plots. */
    private final List<FarmPlot> farmPlots = new ArrayList<>();

    /**
     * Instantiates a new farm.
     * 
     * @param area the area
     */
    public Farm(final MapArea area) {
        this.area = area;

        for (int x = 0; x < area.width; x++) {
            for (int y = 0; y < area.height; y++) {
                farmPlots.add(new FarmPlot(new MapIndex(x + area.pos.x, y + area.pos.y, area.pos.z)));
            }
        }
    }

    /**
     * Gets the area.
     * 
     * @return the area
     */
    public MapArea getArea() {
        return area;
    }

    /**
     * Gets the plots.
     * 
     * @return the plots
     */
    public List<FarmPlot> getPlots() {
        return farmPlots;
    }

    /**
     * Update.
     * 
     * @param player the player
     */
    public void update(final Player player) {
        for (FarmPlot farmPlot : farmPlots) {
            farmPlot.update(player);
        }
    }
}