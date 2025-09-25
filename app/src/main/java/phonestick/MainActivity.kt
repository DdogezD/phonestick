package phonestick

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.topjohnwu.superuser.Shell

class MainActivity : Activity() {
    private val TAG = "MainActivity"
    private var mPrefs: HostPreferenceFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mPrefs = fragmentManager.findFragmentById(R.id.prefs) as HostPreferenceFragment
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.menu_licenses -> {
				val intent = Intent(this, LicenseActivity::class.java)
				startActivity(intent)
				true
			}
			R.id.menu_new_image -> {
				val intent = Intent(this, NewImageActivity::class.java)
				startActivity(intent)
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        val appContext = applicationContext as UsbMountrApplication
        appContext.onActivityResult(requestCode, resultCode, resultData)
    }

    fun onServeClicked(v: View) {
        val file = mPrefs!!.preferenceManager.sharedPreferences
            .getString(mPrefs!!.SOURCE_KEY, "")!!
            .replace("'", "'\\''")
        val ro = if (mPrefs!!.preferenceManager.sharedPreferences
                .getBoolean(mPrefs!!.RO_KEY, true)
        ) "1" else "0"
        UsbScript().execute(file, ro, "1")
    }

    fun onDisableClicked(v: View) {
        UsbScript().execute("", "1", "0")
    }

    inner class UsbScript : AsyncTask<String, Void, Int>() {
        override fun doInBackground(vararg params: String): Int {
            val file = params[0]
            val ro = params[1]
            val enable = params[2]

            val preamble = "set -e"

            val cmdEnable = arrayOf(
                "PS_MOUNT_FILE='$file'",
                "PS_MOUNT_CDROM='n'",
                "if [ $ro = 1 ]; then PS_MOUNT_READ_ONLY='y'; else PS_MOUNT_READ_ONLY='n'; fi",
                "CONFIGFS=`mount -t configfs | head -n1 | cut -d' ' -f 3`",
                "if [ ! -d \$CONFIGFS/usb_gadget/ps ]; then",
                "  mkdir -p \$CONFIGFS/usb_gadget/ps",
                "  cd \$CONFIGFS/usb_gadget/ps",
                "  echo 0x1d6b > idVendor",
                "  echo 0x0104 > idProduct",
                "  echo 0x0100 > bcdUSB",
                "  echo 0xEF > bDeviceClass",
                "  echo 2 > bDeviceSubClass",
                "  echo 1 > bDeviceProtocol",
                "  mkdir -p strings/0x409",
                "  echo 1337 > strings/0x409/serialnumber",
                "  echo phonestick > strings/0x409/manufacturer",
                "  echo '[phonestick]' > strings/0x409/product",
                "  mkdir -p configs/psconfig.1/strings/0x409",
                "  echo 'first rndis, then mass_storage to work on win32' > configs/psconfig.1/strings/0x409/configuration",
                "  mkdir -p functions/mass_storage.0",
                "  echo y > functions/mass_storage.0/lun.0/removable",
                "  echo \$PS_MOUNT_CDROM > functions/mass_storage.0/lun.0/cdrom",
                "else",
                "  cd \$CONFIGFS/usb_gadget/ps",
                "  echo '' > UDC || true",
                "  svc usb resetUsbGadget",
                "  svc usb resetUsbPort",
                "  svc usb setFunctions ''",
				"  getprop sys.usb.controller > UDC",
                "fi",
                "echo \$PS_MOUNT_READ_ONLY > functions/mass_storage.0/lun.0/ro",
                "echo \$PS_MOUNT_FILE > functions/mass_storage.0/lun.0/file",
                "ln -s functions/mass_storage.0 configs/psconfig.1 || true",
                "echo '' > ../g1/UDC || true",
                "getprop sys.usb.controller > UDC",
                "setprop sys.usb.state mass_storage"
            )

            val cmdDisable = arrayOf(
                preamble,
                "CONFIGFS=`mount -t configfs | head -n1 | cut -d' ' -f 3`",
                "cd \$CONFIGFS/usb_gadget/ps",
                "echo '' > UDC || true",
                "svc usb resetUsbGadget",
                "svc usb resetUsbPort",
                "svc usb setFunctions ''",
                "getprop sys.usb.controller > ../g1/UDC"
            )

            val result = if (enable != "0") {
                Shell.cmd(*cmdEnable).exec()
            } else {
                Shell.cmd(*cmdDisable).exec()
            }

            return if (result.code == 0) {
                if (enable != "0") R.string.host_success else R.string.host_disable_success
            } else {
                R.string.host_noroot
            }
        }

        override fun onPostExecute(result: Int) {
            Toast.makeText(applicationContext, getString(result), Toast.LENGTH_SHORT).show()
        }
    }
}
