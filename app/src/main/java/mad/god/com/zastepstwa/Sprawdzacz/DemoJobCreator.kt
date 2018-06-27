package mad.god.com.zastepstwa.Sprawdzacz

import android.support.annotation.NonNull
import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator


class DemoJobCreator : JobCreator {

    override fun create(tag: String): Job? {
        when (tag) {
            DemoSyncJob.TAG -> {
                return DemoSyncJob()
            }
            else -> {
                return null
            }
        }
    }
}