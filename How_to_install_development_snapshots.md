# Installation #

Development snapshots are released as zipped archive of plugins for uDig. There are few steps to follow in order to try them out.

  * Download the latest uDig release for your operating system from the [download page](http://udig.refractions.net/download/). The current release is the 1.2.1
  * Install uDig following the installation instructions on the uDig website
  * Download the latest BeeGIS development snapshot plugins from the [download area](http://code.google.com/p/beegis/downloads/list). Development snapshots are named as: **BeeGIS development snapshot: 2011-02-08**. You can pick the latest by checking the date.
  * unzip the BeeGIS archive on your disk
  * copy the BeeGIS plugins into the **plugins** folder of the uDig istallation folder
  * start uDig

# Tweak the .ini script #

Another important step to take for BeeGIS to work is to change the **udig\_internal.ini** file from:

```
-vmargs
-Xmx386M
-Dosgi.parentClassloader=ext
-Dorg.eclipse.emf.ecore.plugin.EcorePlugin.doNotLoadResourcesPlugin=true
```

to

```
-vmargs
-Xmx1386M
-Dosgi.parentClassloader=ext
-Dorg.eclipse.emf.ecore.plugin.EcorePlugin.doNotLoadResourcesPlugin=true
-XX:MaxPermSize=256m
```

This basically means to:
  * raise the memory dedicated to BeeGIS (which by default is 386mb), if you have a pc with enough ram. If you do not, leave the `-Xmx386M` as it is.
  * add the perm size argument `-XX:MaxPermSize=256m`. This is necessary, else your application will freeze.



# Troubleshooting #

Up to that point no problems were reported. If you have problems in installing the snapshots, please report it and we will add further instructions.