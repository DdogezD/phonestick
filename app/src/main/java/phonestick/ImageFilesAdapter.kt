package phonestick

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import java.io.File

class ImageFilesAdapter(directory: File, val activity: Activity) : RecyclerView.Adapter<ImageFilesAdapter.ImageFilesViewHolder>() {
    private var fileList = directory.listFiles()!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageFilesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_chooser_row, parent, false)
        return ImageFilesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    override fun onBindViewHolder(holder: ImageFilesViewHolder, position: Int) {
        val file = fileList[position]
        val size = if(file.isDirectory)
            0.0
        else
            file.length().toDouble() / (1 shl 20)
        holder.filename.text = file.name
        holder.fileSize.text = holder.fileSize.context.getString(R.string.image_chooser_filesize_mib, size)
        if(file.isFile) {
            holder.view.setOnClickListener {
                val result = Intent()
                result.putExtra("path", file.path)
                activity.setResult(Activity.RESULT_OK, result)
                activity.finish()
            }
        }
    }

    class ImageFilesViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val filename = view.findViewById<TextView>(R.id.filename)!!
        val fileSize = view.findViewById<TextView>(R.id.file_size)!!
    }

}