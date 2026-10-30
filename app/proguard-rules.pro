# Glance renders widgets through WorkManager 2.7.1, whose consumer rules predate R8 full
# mode: they keep classes that WorkManager instantiates by reflection but not their no-arg
# constructors, so release builds crash at startup or never render the widget.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class * extends androidx.work.InputMerger { public <init>(); }
# Our own worker is instantiated by WorkManager through this constructor.
-keep class * extends androidx.work.ListenableWorker { public <init>(android.content.Context, androidx.work.WorkerParameters); }
