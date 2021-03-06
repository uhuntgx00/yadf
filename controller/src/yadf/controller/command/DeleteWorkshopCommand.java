package yadf.controller.command;

import yadf.simulation.IPlayer;
import yadf.simulation.workshop.IWorkshop;
import yadf.simulation.workshop.IWorkshopManager;

/**
 * Command to delete a workshop.
 */
public class DeleteWorkshopCommand extends AbstractCommand {

    /** The serial version UID. */
    private static final long serialVersionUID = 6706293486524959536L;

    /** The workshop id. */
    private final int workshopId;

    /**
     * Instantiates a new delete workshop command.
     * 
     * @param player the player
     * @param workshopIdTmp the workshop id
     */
    public DeleteWorkshopCommand(final IPlayer player, final int workshopIdTmp) {
        super(player);
        workshopId = workshopIdTmp;
    }

    @Override
    public void doCommand() {
        IWorkshop workshop = player.getComponent(IWorkshopManager.class).getGameObject(workshopId);
        workshop.delete();
    }
}
