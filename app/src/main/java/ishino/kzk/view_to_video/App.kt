package ishino.kzk.view_to_video

import android.app.Application
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}