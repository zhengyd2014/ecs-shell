#!/bin/bash

zgrep "ChunkServer.java (line 790) created chunk" cm.log* > ../created.txt

zgrep "ChunkActiveState.java (line 96) seal chunk" cm.log* > ../sealed.txt

zgrep "RepoReclaimer.java (line 304) try to get progress for" cm-chunk-reclaim.log* > ../gc-candidate.txt

zgrep "GeoSendTrackerTaskScanner.java (line 419) Successfully commit chunk" cm.log* > ../replicated.txt

zgrep "has collected all references for repo" cm-chunk-reclaim.log* > ../cleanup-started.txt

zgrep "fullChunkReclaimable true, garbageRangeBit -1, repoUsageSize 0" cm-chunk-reclaim.log* > ../cleanup-done.txt

zgrep "verification status is status: SCHEDULED" cm-chunk-reclaim.log* > ../verification-started.txt

zgrep "gc verification is complete, deleting chunk" cm-chunk-reclaim.log* > ../verification-done.txt

zgrep "are freed, last chunk info with copies: status: DELETING" cm.log* > ../chunk-freed.txt