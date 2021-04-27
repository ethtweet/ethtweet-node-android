package fr.rhaz.ipfs.sweet.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.kelin.okpermission.OkPermission
import fr.rhaz.ipfs.sweet.R

class SplashActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        OkPermission.with(this)
            .addDefaultPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .interceptMissingPermissionDialog {
                AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage(getString(R.string.permission_storage))
                    .setNegativeButton(getString(R.string.permission_exit)) { _, _ ->
                        it.continueWorking(false)
                    }
                    .setPositiveButton(getString(R.string.permission_setting)) { _, _ ->
                        it.continueWorking(true)
                    }.show()
            }.checkAndApply { granted, permissions ->
                if (permissions.isEmpty()) {
                    startActivity(Intent(this@SplashActivity, WebActivity::class.java))
                } else {
                    Toast.makeText(this, getString(R.string.permission_fail), Toast.LENGTH_SHORT).show()
                }
            }


    }



}
