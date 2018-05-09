package com.emc.ecs.stat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhengf1 on 12/5/16.
 */
public class StatHistory {
    private String path;
    private List<StatSnapshot> snapshots = new ArrayList<StatSnapshot>();

    public StatHistory(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    public void addSnapshot(StatSnapshot snapshot) {
        snapshots.add(snapshot);
    }

    public List<StatSnapshot> getSnapshots() {
        return snapshots;
    }

    /**
     * transfer the snapshot list to a time continuous snapshot
     *
     * @return
     */
    public List<StatSnapshot> getContinuosSnapshots() {
        if (snapshots == null || snapshots.size()<=1) {
            return snapshots;
        }

        List<StatSnapshot> continousSnapshots = new ArrayList<StatSnapshot>();

        StatSnapshot previous = snapshots.get(0);
        continousSnapshots.add(previous);
        Long previousSnapshotTime = previous.getTimestamp();
        String previousSnapshotValue = previous.getValue();
        for (int i = 1; i<snapshots.size(); i++) {
            StatSnapshot current = snapshots.get(i);
            long currentSnapshotTime = current.getTimestamp();
            while (currentSnapshotTime - previousSnapshotTime > StatSnapshot.ONE_HOUR) {
                previousSnapshotTime += StatSnapshot.ONE_HOUR;
                continousSnapshots.add(new StatSnapshot(previousSnapshotValue, previousSnapshotTime));
            }

            continousSnapshots.add(current);
            previousSnapshotTime = current.getTimestamp();
            previousSnapshotValue = current.getValue();
        }

        return continousSnapshots;
    }
}
