connect ${ECS}
management
lec
sec com.emc.ecs.chunk.gc.deletejobscanner.job_pause_interval "10 milliseconds"
sec com.emc.ecs.chunk.gc.deletejobscanner.timeout "1 hours"
sec com.emc.ecs.chunk.gc.repo.verification.new_run_interval "3 hours"
sec com.emc.ecs.chunk.gc.repo.verification.sleep_obj_interval 400
sec com.emc.ecs.chunk.gc.repo.verification.run_interval  "7 minutes"
lec
exit
exit