package controller.command;

import simulation.IPlayer;
import simulation.character.ICharacterManager;
import simulation.character.IGameCharacter;
import simulation.military.IMilitaryManager;

/**
 * Enlist a new dwarf into the military.
 */
public class EnlistDwarfCommand extends AbstractCommand {

    /** The serial version UID. */
    private static final long serialVersionUID = 5260775713233467350L;

    /** The dwarf to enlist. */
    private final int dwarfId;

    /**
     * Constructor.
     * @param playerTmp the player
     * @param dwarfIdTmp the dwarf to enlist
     */
    public EnlistDwarfCommand(final IPlayer playerTmp, final int dwarfIdTmp) {
        super(playerTmp);
        dwarfId = dwarfIdTmp;
    }

    @Override
    public void doCommand() {
        IGameCharacter dwarf = player.getComponent(ICharacterManager.class).getDwarf(dwarfId);
        player.getComponent(IMilitaryManager.class).enlistDwarf(dwarf);
    }
}
