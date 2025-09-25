# Notice
This is a fork of a fork of a fork of a fork of streetwalrus' USB Mountr application.
I am planning to call it PhoneStick, but this project is still in its infancy.

forked from [JinbaIttai/phonestick](https://github.com/Swyter/phonestick)

who forked from [Swyter/phonestick](https://github.com/Swyter/phonestick)

who forked it from [dratini0/phonestick](https://github.com/dratini0/phonestick)

who forked it from [donfanning/android\_usb\_msd](https://github.com/donfanning/android_usb_msd) (aka streetwalrus.usbmountr ?)

filepicker fixes from [kodiak-it/USB\_Mountr/](https://github.com/kodiak-it/USB_Mountr)
USB gadget code from [Swyter/android-usb-mass-storage-enable](https://github.com/Swyter/android-usb-mass-storage-enable)

---

Changes by [DdogezD](https://github.com/DdogezD):

- stop renaming everyting, let us just call it "com.phonestick", and leave our credits only in readme and licenses, okay?

- bump Sdk33
- switch chainfire's libsuperuser to magisk libsu
- fixup: let usb features working properly after unmount
- feat: create image
- license: add licenses

---

Below is the original README of USB Mountr.

# USB Mountr
A helper application to set the Mass Storage Device gadget up in Android kernels  

## How it works
Android kernels still include a USB MSD component in their device gadget nowadays, though it is mostly unused since
Android started using MTP. Some OEM ROMs still use it to provide a drivers installation "disc", but it is otherwise
useless.  
This application leverages the module in order to let you use your device as a standard USB thumbdrive for the purpose
of, e.g., booting a distro ISO.

## Building
Standard gradle build process.

## Contributions...
...are welcome, I'm looking for a better icon, and if you feel like implementing it before I do, a menu to create blank
images. Feel free to translate the application to your own language as well.

## See also
- @morfikov has written up [a tutorial](https://gist.github.com/morfikov/0bd574817143d0239c5a0e1259613b7d) on setting up
  your phone as a boot device for a LUKS setup

