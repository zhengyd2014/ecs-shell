package com.emc.ecs.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhengf1 on 10/27/16.
 */
public class Constants {
    static public long  NORMAL_CHUNK_SIZE = 128 * 1024 * 1024;
    static public double EC_CHUNK_SIZE = 16.0/12 * NORMAL_CHUNK_SIZE;
    //static public double EC_CHUNK_SIZE = 1.33 * NORMAL_CHUNK_SIZE;

    public static long ONE_HOUR_IN_MILLISECONDS = 3600 * 1000; // milliseconds

    public static List<String> CONFIG_PARAMETER_NAMES = Arrays.asList(
            "com.emc.ecs.chunk.gc.journalparser.numberOfJP",
            "com.emc.ecs.chunk.gc.deletejobscanner.job_pause_interval",
            "com.emc.ecs.chunk.gc.deletejobscanner.query_conflict_range_interval",
            "com.emc.ecs.chunk.gc.deletejobscanner.timeout",
            "object.RepoGcReclaimerScanBatchSize",
            "com.emc.ecs.chunk.gc.repo.repoReclaimer_batch_pause_interval",
            "com.emc.ecs.chunk.gc.repo.reclaimer.version_pause",
            "com.emc.ecs.chunk.gc.repo.checkReclaimable.max_running_tasks",
            "com.emc.ecs.chunk.gc.scanner.task.max",
            "com.emc.ecs.chunk.gc.scanner.task.tracker.list_max_size",
            "com.emc.ecs.chunk.gc.repo.verification.new_run_interval",
            "com.emc.ecs.chunk.gc.repo.verification.sleep_obj_interval",
            "com.emc.ecs.chunk.gc.repo.verification.sleep_duration",
            "com.emc.ecs.chunk.gc.repo.verification.run_interval",
            "com.emc.ecs.chunk.gc.repo.verification.rr_scanner_timeout"
    );


    public static List<String> STAT_IDS = Arrays.asList(
            "deleted_chunks_repo.TOTAL",
            "ec_freed_slots.TOTAL",
            "total_ec_free_slots.TOTAL",
            "slots_waiting_shipping.TOTAL",
            "slots_waiting_verification.TOTAL",
            "full_reclaimable_repo_chunk.TOTAL",
            "full_reclaimable_aligned_chunk.TOTAL",
            "total_repo_garbage.TOTAL"
    );
}
