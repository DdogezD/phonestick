package phonestick

import android.os.Bundle
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.provider.OpenableColumns
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileInputStream

class ImageChooserActivity : Activity() {
    @Suppress("unused")
    private val TAG = "ImageChooserActivity"

    private var directory = File("/")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        directory = filesDir
        setContentView(R.layout.activity_image_chooser)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        val adapter = ImageFilesAdapter(directory, this)
        recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_image_chooser, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.image_chooser_add -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "*/*"
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)

                startActivityForResult(intent, -1)

                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if(resultCode == RESULT_CANCELED) return
        if(data.data == null) return
        CopyInTask(this, directory).execute(data.data)
        Log.d(TAG, "onActivityResult")
    }

    private class CopyInTask(context: Context, private val directory: File): AsyncTask<Uri, Pair<Int, Int>, Unit>() {
        var totalSize = 0L
        private val progressDialog = ProgressDialog(context)
        private val contentResolver = context.contentResolver
        val TAG = "CopyInTask"
        override fun doInBackground(vararg params: Uri) {
            val fds = params.map { Pair(it, contentResolver.openFileDescriptor(it, "r")!!) }
            fds.last {(_, fileDescriptor) ->
                val statSize = fileDescriptor.statSize
                if(statSize == -1L) {
                    totalSize = -1L
                    false
                } else {
                    totalSize += statSize
                    true
                }
            }
            var transfered = 0L
            var counter = 0
            fds.forEach {(uri, fileDescriptor) ->
                var filename: String =
                contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)!!.use {
                    it.moveToFirst()
                    it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
                filename = filename.replace('/', '_')
                if(File(directory, filename).exists()) {
                    var i = 1
                    val index = filename.findLastAnyOf(arrayListOf("."))?.first ?: filename.length
                    val basename = filename.substring(0 until index)
                    val extension = filename.substring(index until filename.length)
                    do {
                        filename = "%s.%d%s".format(basename, i, extension)
                        i++
                    } while (File(directory, filename).exists())
                }

                FileInputStream(fileDescriptor.fileDescriptor).use { fileInputStream ->
                    File(directory, filename).outputStream().use { fileOutputStream ->
                        val buffer = ByteArray(1 shl 14)
                        while (true){
                            if(isCancelled) return@forEach
                            val readBytes = fileInputStream.read(buffer)
                            if(readBytes <= 0) break
                            fileOutputStream.write(buffer, 0, readBytes)
                            transfered += readBytes
                            counter++
                            if (counter % 10 == 0) publishProgress(Pair((transfered shr 10).toInt(), (totalSize shr 10).toInt()))
                        }
                    }
                }

            }
        }

        override fun onPreExecute() {
            //progressDialog.isIndeterminate = false
            progressDialog.setTitle(R.string.image_chooser_copying_dialog)
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            progressDialog.show()
        }

        override fun onProgressUpdate(vararg values: Pair<Int, Int>) {
            val (progress, max) = values[0]
            progressDialog.progress = progress
            progressDialog.max = max
            progressDialog.isIndeterminate = max == -1
        }

        override fun onPostExecute(result: Unit?) {
            progressDialog.dismiss()
        }

    }
}
