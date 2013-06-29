package userinterface.game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;

import logger.Logger;
import misc.MyRandom;
import settings.Settings;
import simulation.Player;
import simulation.Region;
import simulation.item.ItemTypeManager;
import simulation.labor.LaborTypeManager;
import simulation.map.MapIndex;
import simulation.recipe.RecipeManager;
import simulation.workshop.WorkshopTypeManager;
import userinterface.game.job.JobsPane;
import userinterface.game.labor.LaborsPane;
import userinterface.game.stock.StocksPane;
import userinterface.misc.ImagePanel;
import userinterface.multiplayer.IMainWindow;
import controller.AbstractController;
import controller.ClientController;
import controller.Connection;
import controller.SinglePlayerController;

/**
 * The Game Panel.
 */
public class GamePanel extends ImagePanel implements IGamePanel {

    /** The serial version UID. */
    private static final long serialVersionUID = -3057695070550769148L;

    /** The main window. */
    private final IMainWindow mainWindow;

    /** The game loop runnable. */
    private GameLoop gameLoop;

    /** The controller. */
    private AbstractController controller;

    /** The region. */
    private Region region;

    /** The player. */
    private Player player;

    /** The world pane. */
    private WorldPane worldPane;

    /** The jobs pane. */
    private JobsPane jobsPane;

    /** The labors pane. */
    private LaborsPane laborsPane;

    /** The stocks pane. */
    private StocksPane stocksPane;

    // TODO: have class that is the status panel
    /** The status panel. */
    private JPanel statusPanel;

    /** The fps label. */
    private JLabel fpsLabel;

    /** The date label. */
    private JLabel dateLabel;

    /** The state label. */
    private JLabel stateLabel;

    /**
     * Constructor.
     * @param mainWindowTmp the main window
     */
    public GamePanel(final IMainWindow mainWindowTmp) {
        mainWindow = mainWindowTmp;
        setupLayout();
    }

    /**
     * Start a single player game.
     * @param playerName the name of the player
     * @param regionSize the size of the region
     */
    public void startSinglePlayerGame(final String playerName, final MapIndex regionSize) {
        Logger.getInstance().log(this, "Starting single player game");
        try {
            ItemTypeManager.getInstance().load();
            WorkshopTypeManager.getInstance().load();
            LaborTypeManager.getInstance().load();
            RecipeManager.getInstance().load();

            int numberOfStartingDwarfs = Integer.parseInt(Settings.getInstance().getSetting("starting_dwarves"));
            MapIndex embarkPosition = new MapIndex(regionSize.x / 2, regionSize.y / 2, 0);

            region = new Region();
            player = new Player();
            region.addPlayer(player);
            controller = new SinglePlayerController();

            worldPane.setup(region, player, controller);
            jobsPane.setup(player.getJobManager());
            laborsPane.setup(player, controller);
            stocksPane.setup(player);

            region.setup(regionSize);
            embarkPosition.z = region.getMap().getHeight(embarkPosition.x, embarkPosition.y);
            player.setup(playerName, region, embarkPosition, numberOfStartingDwarfs);
            worldPane.getWorldCanvas().zoomToPosition(embarkPosition);

            gameLoop = new GameLoop(region, controller, this);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not start the game");
            mainWindow.setupMainMenu();
        }
    }

    /**
     * Start a multiplayer game.
     * @param connection the connection to the server
     * @param playerNames the names of all the players
     * @param playerIndex the index of this player
     * @param regionSize the size of the region
     */
    public void startMultiplayerGame(final Connection connection, final List<String> playerNames,
            final int playerIndex, final MapIndex regionSize) {
        Logger.getInstance().log(this, "Starting multiplayer game");
        controller = new ClientController(connection, this);
        try {
            ItemTypeManager.getInstance().load();
            WorkshopTypeManager.getInstance().load();
            LaborTypeManager.getInstance().load();
            RecipeManager.getInstance().load();
            MyRandom.getInstance().setSeed(10);

            region = new Region();
            region.setup(regionSize);

            for (String playerName : playerNames) {
                Logger.getInstance().log(this, "Adding player " + playerName);
                Player newPlayer = new Player();
                int numberOfStartingDwarfs = Integer.parseInt(Settings.getInstance().getSetting("starting_dwarves"));
                MapIndex embarkPosition = new MapIndex(regionSize.x / 2, regionSize.y / 2, 0);
                newPlayer.setup(playerName, region, embarkPosition, numberOfStartingDwarfs);
                region.addPlayer(newPlayer);
                if (playerName.equals(playerNames.get(playerIndex))) {
                    player = newPlayer;
                }
            }

            worldPane.setup(region, player, controller);
            jobsPane.setup(player.getJobManager());
            laborsPane.setup(player, controller);
            stocksPane.setup(player);

            gameLoop = new GameLoop(region, controller, this);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not start the game");
            mainWindow.setupMainMenu();
        }
    }

    /**
     * Load a single player game from a file.
     */
    public void loadSinglePlayerGame() {
        Logger.getInstance().log(this, "Loading game");
        try {
            ItemTypeManager.getInstance().load();
            WorkshopTypeManager.getInstance().load();
            LaborTypeManager.getInstance().load();
            RecipeManager.getInstance().load();

            FileInputStream fileInputStream = new FileInputStream("myobject.data");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            region = (Region) objectInputStream.readObject();
            objectInputStream.close();

            // TODO: Which player, perhaps dialog...
            player = region.getPlayers().toArray(new Player[0])[0];
            controller = new SinglePlayerController();

            worldPane.setup(region, player, controller);
            jobsPane.setup(player.getJobManager());
            laborsPane.setup(player, controller);
            stocksPane.setup(player);

            gameLoop = new GameLoop(region, controller, this);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not start the game");
            mainWindow.setupMainMenu();
        }
    }

    /**
     * Save single player game to a file.
     */
    public void saveSinglePlayerGame() {
        Logger.getInstance().log(this, "Saving game");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("myobject.data");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(region);
            objectOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not save the game");
            mainWindow.setupMainMenu();
        }
    }

    @Override
    public void update() {
        worldPane.update();
        fpsLabel.setText("FPS:" + Long.toString(gameLoop.getFps()));
        dateLabel.setText(region.getTimeString());
        stateLabel.setText(worldPane.getStateString());
    }

    @Override
    public void disconnect() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    /**
     * Setup the layout.
     */
    private void setupLayout() {
        // CHECKSTYLE:OFF
        setLayout(new BorderLayout(0, 0));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setOpaque(false);
        add(tabbedPane, BorderLayout.CENTER);

        // Create the status bar
        Border paddingBorder = BorderFactory.createEmptyBorder(2, 10, 2, 10);
        statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        statusPanel.setOpaque(false);
        add(statusPanel, BorderLayout.SOUTH);

        fpsLabel = new JLabel("FPS");
        fpsLabel.setBorder(paddingBorder);
        fpsLabel.setForeground(Color.WHITE);
        statusPanel.add(fpsLabel);

        dateLabel = new JLabel("Date");
        dateLabel.setBorder(paddingBorder);
        dateLabel.setForeground(Color.WHITE);
        statusPanel.add(dateLabel);

        stateLabel = new JLabel("State");
        stateLabel.setBorder(paddingBorder);
        stateLabel.setForeground(Color.WHITE);
        statusPanel.add(stateLabel);

        // The world pane contains the main game canvas
        worldPane = new WorldPane();
        tabbedPane.addTab("World", null, worldPane, null);

        // The jobs pane has a list of all jobs
        jobsPane = new JobsPane();
        tabbedPane.addTab("Jobs", null, jobsPane, null);

        // The labors pane shows the skills and enabled labors for each dwarf
        laborsPane = new LaborsPane();
        tabbedPane.addTab("Labors", null, laborsPane, null);

        // The stocks pane shows all the stock counts
        stocksPane = new StocksPane();
        tabbedPane.addTab("Stocks", null, stocksPane, null);
        // CHECKSTYLE:ON
    }
}
