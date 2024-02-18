package ishino.kzk.view_to_video

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ishino.kzk.view_to_video.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = checkNotNull(_binding)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setTimer()
    }

    private fun setTimer() {

    }
}