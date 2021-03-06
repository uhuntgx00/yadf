package yadf.simulation.military;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import yadf.simulation.character.IGameCharacter;
import yadf.simulation.character.component.IMilitaryComponent;
import yadf.simulation.character.component.MilitaryComponent;
import yadf.simulation.job.IJob;
import yadf.simulation.job.IJobListener;
import yadf.simulation.job.IJobManager;
import yadf.simulation.job.MilitaryStationJob;
import yadf.simulation.map.MapIndex;

/**
 * The military manager.
 */
public class MilitaryManager implements IMilitaryManager, IJobListener {

    /** The dwarves that are enlisted. */
    private Set<IGameCharacter> soldiers = new CopyOnWriteArraySet<>();

    /** The listeners. */
    private Set<IMilitaryManagerListener> listeners = new CopyOnWriteArraySet<>();

    /** All the station jobs. */
    private Set<IJob> stationJobs = new CopyOnWriteArraySet<>();

    @Override
    public void enlistDwarf(final IGameCharacter dwarf) {
        assert !soldiers.contains(dwarf);
        soldiers.add(dwarf);
        dwarf.setComponent(IMilitaryComponent.class, new MilitaryComponent(dwarf));
        for (IMilitaryManagerListener listener : listeners) {
            listener.soldierAdded();
        }
    }

    @Override
    public Set<IGameCharacter> getSoldiers() {
        return soldiers;
    }

    @Override
    public void station(final MapIndex target) {
        for (IJob stationJob : stationJobs) {
            stationJob.interrupt("New station ordered");
        }
        for (IGameCharacter soldier : soldiers) {
            IJob stationJob = new MilitaryStationJob(target, soldier);
            stationJobs.add(stationJob);
            stationJob.addListener(this);
            soldier.getPlayer().getComponent(IJobManager.class).addJob(stationJob);
        }
    }

    @Override
    public Set<IJob> getStationJobs() {
        return stationJobs;
    }

    @Override
    public void addMilitaryManagerListener(final IMilitaryManagerListener listener) {
        assert !listeners.contains(listener);
        listeners.add(listener);
    }

    @Override
    public void removeMilitaryManagerListener(final IMilitaryManagerListener listener) {
        assert listeners.contains(listener);
        listeners.remove(listener);
    }

    @Override
    public void jobDone(final IJob job) {
        assert stationJobs.contains(job);
        stationJobs.remove(job);
    }

    @Override
    public void jobChanged(final IJob job) {
    }

    @Override
    public void update() {
        // nothing to do
    }
}
