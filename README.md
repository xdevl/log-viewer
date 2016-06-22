# Android log viewer
Android application to display/export/share system logs and log files. Root permissions are required in order to access kernel logs. Android logs (aka logcat) will be accessible if your device is running a version of Android older than 4.1, otherwise you will need to either way:
* root your phone
* manually grant the READ_LOGS permission from adb by running the following command:
```markdown
pm grant com.xdevl.logviewer android.permission.READ_LOGS
```

# How to
In order to build the application you will need the [android-sdk](https://developer.android.com/studio/index.html),  [gradle](http://gradle.org/), [python3](https://www.python.org), [git](https://git-scm.com/) and [inkscape](https://inkscape.org/en/) installed locally on your machine.

Start off by generating the application images by running at the root of the project:
```markdown
python src/main/svg/generate.py
```
Then, to build the app, simply run:
```markdown
gradle build
```
# License
This plugin is licensed under the [GNU General Public License v3](https://www.gnu.org/licenses/gpl-3.0.html)
