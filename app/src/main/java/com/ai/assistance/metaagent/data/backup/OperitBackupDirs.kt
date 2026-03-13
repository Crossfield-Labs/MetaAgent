package com.ai.assistance.metaagent.data.backup

import com.ai.assistance.metaagent.util.MetaAgentPaths
import java.io.File

object MetaAgentBackupDirs {

    fun metaagentRootDir(): File {
        return MetaAgentPaths.metaagentRootDir()
    }

    fun backupRootDir(): File {
        return ensureDir(File(metaagentRootDir(), "backup"))
    }

    fun rawSnapshotDir(): File {
        return ensureDir(File(backupRootDir(), "raw_snapshot"))
    }

    fun roomDbDir(): File {
        return ensureDir(File(backupRootDir(), "room_db"))
    }

    fun chatDir(): File {
        return ensureDir(File(backupRootDir(), "chat"))
    }

    fun memoryDir(): File {
        return ensureDir(File(backupRootDir(), "memory"))
    }

    fun modelConfigDir(): File {
        return ensureDir(File(backupRootDir(), "model_config"))
    }

    fun characterCardsDir(): File {
        return ensureDir(File(backupRootDir(), "character_cards"))
    }

    private fun ensureDir(dir: File): File {
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
}

