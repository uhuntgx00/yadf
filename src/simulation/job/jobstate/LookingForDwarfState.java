package simulation.job.jobstate;

import simulation.character.Dwarf;
import simulation.character.ICharacterAvailableListener;
import simulation.character.IGameCharacter;
import simulation.character.component.ISkillComponent;
import simulation.job.AbstractJob;
import simulation.labor.LaborType;

/**
 * Generic state to look for a dwarf.
 */
public abstract class LookingForDwarfState extends AbstractJobState implements ICharacterAvailableListener {

    /** The found dwarf. */
    private Dwarf dwarf;

    /** The required labor type. */
    private final LaborType requiredLabor;

    /**
     * Constructor.
     * @param requiredLaborTmp the required labor
     * @param jobTmp the job that this state belongs to
     */
    public LookingForDwarfState(final LaborType requiredLaborTmp, final AbstractJob jobTmp) {
        super(jobTmp);
        requiredLabor = requiredLaborTmp;
    }

    @Override
    public String toString() {
        return "Looking for dwarf";
    }

    @Override
    public void transitionInto() {
        dwarf = getJob().getPlayer().getDwarfManager().getIdleDwarf(requiredLabor);
        if (dwarf == null) {
            getJob().getPlayer().getDwarfManager().addListener(this);
        } else {
            getJob().stateDone(this);
        }
    }

    @Override
    public void transitionOutOf() {
    }

    @Override
    public void characterAvailable(final IGameCharacter character) {
        assert character instanceof Dwarf;
        if (character.getComponent(ISkillComponent.class).canDoJob(requiredLabor) && character.acquireLock()) {
            dwarf = (Dwarf) character;
            getJob().stateDone(this);
            getJob().getPlayer().getDwarfManager().removeListener(this);
        }
    }

    /**
     * Gets the found dwarf.
     * @return the dwarf
     */
    public Dwarf getDwarf() {
        return dwarf;
    }
}