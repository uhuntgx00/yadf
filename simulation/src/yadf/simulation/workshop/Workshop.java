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
package yadf.simulation.workshop;

import java.util.ArrayList;
import java.util.List;

import yadf.simulation.AbstractEntity;
import yadf.simulation.IPlayer;
import yadf.simulation.job.CraftJob;
import yadf.simulation.job.IJob;
import yadf.simulation.job.IJobManager;
import yadf.simulation.map.MapIndex;
import yadf.simulation.recipe.Recipe;
import yadf.simulation.recipe.RecipeManager;

/**
 * The Class Workshop.
 */
public class Workshop extends AbstractEntity implements IWorkshop {

    /** The workshop type. */
    private final WorkshopType workshopType;

    /** Size of a workshop. */
    public static final int WORKSHOP_SIZE = 3;

    /** The craft job. */
    private IJob craftJob;

    /** The orders. */
    private final List<Recipe> orders = new ArrayList<>();

    /** Listeners to changes of the workshop. */
    private final List<IWorkshopListener> listeners = new ArrayList<>();

    /** The player that this workshop belongs to. */
    private final IPlayer player;

    /**
     * Instantiates a new workshop.
     * @param playerTmp the player that this workshop belongs to
     * @param workshopTypeTmp the workshop type
     * @param position the position
     */
    public Workshop(final IPlayer playerTmp, final WorkshopType workshopTypeTmp, final MapIndex position) {
        super(position, WORKSHOP_SIZE, WORKSHOP_SIZE);
        workshopType = workshopTypeTmp;
        player = playerTmp;
    }

    @Override
    public void addListener(final IWorkshopListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(final IWorkshopListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void cancelOrder(final int orderIndex) {
        if (orderIndex == 0 && craftJob != null) {
            craftJob.interrupt("Order canceled");
        } else {
            Recipe removedOrder = orders.remove(orderIndex);
            notifyListenersOfOrderRemoved(removedOrder);
        }
    }

    @Override
    public List<Recipe> getOrders() {
        return orders;
    }

    @Override
    public WorkshopType getType() {
        return workshopType;
    }

    @Override
    public void newOrder(final String recipeName) {
        Recipe recipe = RecipeManager.getInstance().getRecipe(recipeName);
        orders.add(recipe);
        notifyListenersOfOrderAdded(recipe);
    }

    @Override
    public void update() {
        // TODO: Remove this method
        if (craftJob == null) {
            if (!orders.isEmpty()) {
                craftJob = new CraftJob(this, orders.get(0), player);
                player.getComponent(IJobManager.class).addJob(craftJob);
            }
        } else {
            if (craftJob.isDone()) {
                Recipe removedOrder = orders.remove(0);
                notifyListenersOfOrderRemoved(removedOrder);
                craftJob = null;
            }
        }
    }

    /**
     * Notify all the listeners that an order has been added.
     * @param recipe the recipe that was added
     */
    private void notifyListenersOfOrderAdded(final Recipe recipe) {
        for (IWorkshopListener listener : listeners) {
            listener.orderAdded(recipe);
        }
    }

    /**
     * Notify all the listeners that an order has been removed.
     * @param recipe the recipe that was removed
     */
    private void notifyListenersOfOrderRemoved(final Recipe recipe) {
        for (IWorkshopListener listener : listeners) {
            listener.orderRemoved(recipe);
        }
    }
}
