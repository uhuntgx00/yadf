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
package yadf.ui.swing.game.stock;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.jdesktop.swingx.JXTreeTable;

import yadf.simulation.IPlayer;
import yadf.simulation.item.IStockManager;

/**
 * The Class StocksPane.
 */
public class StocksPane extends JPanel {

    /** The serial version UID. */
    private static final long serialVersionUID = 3427702994977889616L;

    /** The stocks table. */
    private JXTreeTable stocksTable;

    /** The stocks scroll pane. */
    private final JScrollPane stocksScrollPane;

    /**
     * Instantiates a new stocks pane.
     */
    public StocksPane() {
        super(new BorderLayout());
        setOpaque(false);

        stocksScrollPane = new JScrollPane();
        stocksScrollPane.setBorder(new EmptyBorder(2, 2, 2, 2));
        stocksScrollPane.setOpaque(false);
        add(stocksScrollPane, BorderLayout.CENTER);

        stocksTable = new JXTreeTable();
        stocksTable.setBorder(null);
        stocksScrollPane.setViewportView(stocksTable);
    }

    /**
     * Setsup the layout.
     * @param player the new player
     */
    public void setup(final IPlayer player) {
        stocksTable = new JXTreeTable(new StockTreeTableModel(player.getComponent(IStockManager.class)));
        stocksScrollPane.setViewportView(stocksTable);
    }
}
