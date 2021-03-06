package yadf.ui.swing.game;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTabbedPane;

import yadf.controller.AbstractController;
import yadf.simulation.IGameObject;
import yadf.simulation.IGameObjectListener;
import yadf.simulation.IPlayer;
import yadf.simulation.character.Dwarf;
import yadf.simulation.item.ContainerItem;
import yadf.simulation.item.Item;
import yadf.simulation.item.Stockpile;
import yadf.simulation.room.Room;
import yadf.simulation.workshop.Workshop;
import yadf.ui.swing.game.dwarf.DwarfInterface;
import yadf.ui.swing.game.item.ItemInterface;
import yadf.ui.swing.game.job.JobsPane;
import yadf.ui.swing.game.labor.LaborsPane;
import yadf.ui.swing.game.military.MilitaryPane;
import yadf.ui.swing.game.room.RoomInterface;
import yadf.ui.swing.game.stock.StocksPane;
import yadf.ui.swing.game.stockpile.StockpileInterface;
import yadf.ui.swing.game.workshop.WorkshopInterface;

/**
 * The south panel that has all the info and stuff in it.
 */
public class ManagementPanel extends JTabbedPane implements IGameObjectListener {

    /** The serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The player. */
    private IPlayer player;

    /** The controller. */
    private AbstractController controller;

    /** The game panel. */
    private GamePanel gamePanel;

    /** The jobs pane. */
    private JobsPane jobsPane;

    /** The labors pane. */
    private LaborsPane laborsPane;

    /** The stocks pane. */
    private StocksPane stocksPane;

    /** The military pane. */
    private MilitaryPane militaryPane;

    /** All the game object interfaces. */
    private final Map<Class<? extends IGameObject>, AbstractGameObjectInterface> gameObjectInterfaces = new HashMap<>();

    /** The currently selected game object. */
    private IGameObject gameObject;

    /**
     * Constructor.
     */
    public ManagementPanel() {
        setPreferredSize(new Dimension(0, 200));
        setRequestFocusEnabled(false);
        setTabPlacement(JTabbedPane.LEFT);
        setBorder(null);
        setOpaque(false);

        jobsPane = new JobsPane();
        laborsPane = new LaborsPane();
        stocksPane = new StocksPane();
        militaryPane = new MilitaryPane();

        addTab("Jobs", null, jobsPane, null);
        addTab("Labors", null, laborsPane, null);
        addTab("Stocks", null, stocksPane, null);
        addTab("Military", null, militaryPane, null);

        gameObjectInterfaces.put(Room.class, new RoomInterface());
        gameObjectInterfaces.put(Workshop.class, new WorkshopInterface());
        gameObjectInterfaces.put(Stockpile.class, new StockpileInterface());
        gameObjectInterfaces.put(Dwarf.class, new DwarfInterface());
        gameObjectInterfaces.put(Item.class, new ItemInterface());
        gameObjectInterfaces.put(ContainerItem.class, new ItemInterface());
    }

    /**
     * Setup the panel.
     * @param playerTmp the player
     * @param controllerTmp the controller
     * @param gamePanelTmp the game panel
     */
    public void setup(final IPlayer playerTmp, final AbstractController controllerTmp, final GamePanel gamePanelTmp) {
        player = playerTmp;
        controller = controllerTmp;
        gamePanel = gamePanelTmp;
        jobsPane.setup(playerTmp, controllerTmp, gamePanel.getWorldPanel());
        laborsPane.setup(playerTmp, controllerTmp, gamePanel.getWorldPanel());
        stocksPane.setup(playerTmp);
        militaryPane.setup(playerTmp, controllerTmp, gamePanel);
    }

    /**
     * Open a game object interface.
     * @param gameObjectTmp the game object to open the interface for
     */
    public void openGameObjectInterface(final IGameObject gameObjectTmp) {
        AbstractGameObjectInterface gameObjectInterface = gameObjectInterfaces.get(gameObjectTmp.getClass());
        if (gameObjectInterface != null) {
            if (getTabCount() == 5) {
                gameObject.removeGameObjectListener(this);
                removeTabAt(4);
            }
            gameObject = gameObjectTmp;
            gameObject.addGameObjectListener(this);
            gameObjectInterface.setup(gameObject, player, controller, gamePanel);
            addTab(gameObjectInterface.getTitle(), null, gameObjectInterface, null);
            setSelectedComponent(gameObjectInterface);
        }
    }

    @Override
    public void gameObjectDeleted(final Object gameObjectTmp) {
        assert getTabCount() == 5;
        removeTabAt(4);
        gameObject.removeGameObjectListener(this);
    }
}
