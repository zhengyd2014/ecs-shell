package com.emc.ecs.log;

import java.io.Serializable;

/**
 * Created by zhengf1 on 1/12/17.
 */
public enum ChunkState implements Serializable {
    Created,
    Sealed,
    Replicated,
    GcCandidate,
    CleanupJobStart,
    CleanupJobDone,
    VerificationTaskCreated,
    VerificationTaskDone,
    ChunkFreed
    ;

    private static final long serialVersionUID = 3L;

//    @Override
//    public String toString() {
//        switch (this) {
//            case Created:
//                return "Created";
//            case Sealed:
//                return "Sealed";
//            case Replicated:
//                return "Replicated";
//            case GcCandidate:
//                return "GC Candidate";
//            case CleanupJobStart:
//                return "Cleanup Start";
//            case CleanupJobDone:
//                return "Cleanup Done";
//            case VerificationTaskCreated:
//                return "Veri Start";
//            case VerificationTaskDone:
//                return "Veri Done";
//            case ChunkFreed:
//                return "Freed";
//            default:
//                break;
//        }
//
//        return super.toString();
//    }

}
