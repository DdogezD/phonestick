package phonestick

import android.app.Activity
import android.app.AlertDialog
import android.os.AsyncTask
import android.os.Bundle
import android.widget.*
import com.topjohnwu.superuser.Shell
import java.io.File

class NewImageActivity : Activity() {
    private var progressDialog: AlertDialog? = null
    private var busyboxPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_image)

        val nameEdit = findViewById<EditText>(R.id.editName)
        val sizeEdit = findViewById<EditText>(R.id.editSize)
        val unitSpinner = findViewById<Spinner>(R.id.spinnerUnit)
        val btnCreate = findViewById<Button>(R.id.btnCreate)

        ArrayAdapter.createFromResource(
            this, R.array.size_units, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            unitSpinner.adapter = adapter
        }

        btnCreate.setOnClickListener {
            val name = nameEdit.text.toString().ifEmpty { "phonestick.img" }
            val size = sizeEdit.text.toString().toIntOrNull() ?: 1
            val unit = unitSpinner.selectedItem.toString()

            val bytes = if (unit == "GB") size * 1024L * 1024L * 1024L else size * 1024L * 1024L
            val outFile = File("/sdcard/Download/$name")

            busyboxPath = listOf(
                "/data/adb/ksu/bin/busybox",
                "/data/adb/magisk/bin/busybox",
                "/data/adb/apatch/bin/busybox",
            ).firstOrNull { path -> Shell.cmd("[ -x $path ]").exec().code == 0 }

            if (busyboxPath == null) {
                Toast.makeText(this, R.string.error_no_busybox, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CreateImageTask(outFile.absolutePath, bytes).execute()
        }
    }

    private inner class CreateImageTask(
        val path: String,
        val bytes: Long
    ) : AsyncTask<Void, Void, Boolean>() {
        override fun onPreExecute() {
            progressDialog = AlertDialog.Builder(this@NewImageActivity)
                .setMessage(getString(R.string.dialog_creating))
                .setCancelable(false)
                .create()
            progressDialog?.show()
        }

        override fun doInBackground(vararg params: Void?): Boolean {
            val fallocateCmd = "$busyboxPath fallocate -l $bytes '$path'"
            return Shell.cmd(fallocateCmd).exec().code == 0
        }

        override fun onPostExecute(success: Boolean) {
            progressDialog?.dismiss()
            Toast.makeText(
                this@NewImageActivity,
                if (success) R.string.toast_create_success else R.string.toast_create_failed,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
