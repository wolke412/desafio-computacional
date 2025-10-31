package com.dc
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.dc.utils.SessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.osmdroid.config.Configuration
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SessionManager.getInstance(this).isLoggedIn()) {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

//        ActivityCompat.requestPermissions(this, arrayOf(
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.ACCESS_NETWORK_STATE,
//            Manifest.permission.ACCESS_WIFI_STATE
//            ),
//           1
//        )

        val cacheDir = this.applicationContext.getExternalFilesDir(null)
        val osmConfig = Configuration.getInstance()

        osmConfig.osmdroidBasePath = File(cacheDir, "osmdroid")
        osmConfig.osmdroidTileCache= File(cacheDir, "tiles")
        osmConfig.load(this, PreferenceManager.getDefaultSharedPreferences(this))
        osmConfig.userAgentValue = this.packageName // Set User Agent

        if (isNetworkAvailable(this)) {
            Log.d("OsmDroid", "Network is available. Initializing map...")
        } else {
            Log.w("OsmDroid", "Network is NOT available.")
            Toast.makeText( this, "No internet connection. Map tiles may not load.", Toast.LENGTH_LONG).show()
        }

        // Retrieve the NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment

        navController = navHostFragment.navController
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        NavigationUI.setupWithNavController(navView, navController)


        setupActionBarWithNavController(navController)

        setCreatePostAction()
    }

    override fun onSupportNavigateUp(): Boolean {
        // back button press on the action bar
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun setCreatePostAction() {
        val newPost  = findViewById<Button>(R.id.btnNewPost);
        newPost.setOnClickListener {
                navController.navigate(R.id.navigation_create_post)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED && // WRITE_EXTERNAL_STORAGE
                    grantResults[1] == PackageManager.PERMISSION_GRANTED    // READ_EXTERNAL_STORAGE
                ) {
                    Log.d("Permissions", "Storage permissions granted")
                } else {
                    Log.e("Permissions", "Storage permissions denied")
                    Toast.makeText(this, "Storage permission is required for map functionality.", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }


    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                // for other device how are able to connect with Ethernet
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                // for check internet over Bluetooth
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }
}