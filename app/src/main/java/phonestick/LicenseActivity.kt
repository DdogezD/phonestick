package phonestick

import android.app.AlertDialog
import android.app.ListActivity
import android.content.Intent
import android.content.res.XmlResourceParser
import android.net.Uri
import android.os.Bundle
import android.preference.Preference
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import java.io.InputStreamReader

class LicenseActivity : ListActivity() {
    private val TAG = "LicenseActivity"

    private var prefLayout = -1
    private var mList: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_licenses)

        prefLayout = Preference(this).layoutResource

        val licenseList: MutableList<License> = mutableListOf()
        val xrp = resources.getXml(R.xml.licenses)
        while (xrp.eventType != XmlResourceParser.END_DOCUMENT) {
            xrp.next()
            if (xrp.eventType == XmlResourceParser.START_TAG && xrp.name == "license") {
                licenseList.add(License(xrp))
            }
        }

        mList = findViewById(android.R.id.list)
        val licensesAdapter = LicenseArrayAdapter(licenseList)
        mList!!.adapter = licensesAdapter
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        val licenseTextLayout = layoutInflater.inflate(R.layout.dialog_license, null, false)
        val licenseTextView = licenseTextLayout.findViewById<TextView>(R.id.textView)
        val lic = (l.adapter as LicenseArrayAdapter).getItem(position)!!
        licenseTextView.text = InputStreamReader(assets.open("licenses/${lic.file}")).readText()
        licenseTextView.movementMethod = ScrollingMovementMethod()
        AlertDialog.Builder(this@LicenseActivity)
            .setTitle(lic.name)
            .setView(licenseTextLayout)
            .setPositiveButton(R.string.licenses_upstream) { dialog, which ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lic.url))
                startActivity(intent)
            }
            .show()
    }

    private inner class License(xrp: XmlResourceParser) {
        val name: String = xrp.getAttributeValue(null, "name")
        val type: String? = xrp.getAttributeValue(null, "type")
        val file: String? = xrp.getAttributeValue(null, "file")
        val url: String? = xrp.getAttributeValue(null, "url")

        private var view: View? = null

        fun getView(parent: ViewGroup): View {
            if (view == null) {
                view = layoutInflater.inflate(prefLayout, parent, false)
                if (android.os.Build.VERSION.SDK_INT >= 24)
                    view!!.findViewById<View>(android.R.id.icon_frame)?.visibility = View.GONE
                view!!.findViewById<ImageView>(android.R.id.icon)?.visibility = View.GONE
                view!!.findViewById<TextView>(android.R.id.title)?.text = name
                val summary = view!!.findViewById<TextView>(android.R.id.summary)
                if (type != null)
                    summary?.text = type
                else
                    summary?.visibility = View.GONE
                view!!.findViewById<ViewGroup>(android.R.id.widget_frame)?.visibility = View.GONE
            }
            return view!!
        }
    }

    private inner class LicenseArrayAdapter(licenses: List<License>) :
        ArrayAdapter<License>(this, 0, licenses) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val license = getItem(position)!!
            return license.getView(parent)
        }
    }
}
