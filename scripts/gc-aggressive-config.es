connect ${ECS}
management
lec
sec com.emc.ecs.chunk.gc.deletejobscanner.job_pause_interval "1 milliseconds"
sec com.emc.ecs.chunk.gc.deletejobscanner.timeout "3 hours"
sec com.emc.ecs.chunk.gc.repo.verification.new_run_interval "30 minutes"
sec com.emc.ecs.chunk.gc.repo.verification.sleep_obj_interval 10000
sec com.emc.ecs.chunk.gc.repo.verification.run_interval  "1 minutes"
lec
exit
exit