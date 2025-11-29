# SurfaceView Blur Setup Guide

## Why Blur May Not Work with SurfaceView

SurfaceView uses a **separate rendering surface** that exists outside the normal Android view hierarchy. This creates several challenges for blur effects:

1. **Separate Window Layer**: SurfaceView renders in a separate surface below the main window
2. **Z-Ordering Issues**: By default, SurfaceView appears below or above regular views, ignoring XML layout order
3. **Asynchronous Capture**: Content must be captured using PixelCopy API (Android 7.0+), which introduces lag

## ✨ Automatic Configuration (Recommended)

**Good news!** As of the latest version, BlurView **automatically detects and configures** SurfaceView for proper blur rendering.

### What Happens Automatically

When BlurView detects a SurfaceView in the view hierarchy, it will:
1. ✅ Automatically call `setZOrderMediaOverlay(true)` on the SurfaceView
2. ✅ Log informative messages about the configuration
3. ✅ Handle PixelCopy for content capture (Android 7.0+)

### Simple Setup - No Extra Code Needed!

```java
// Just create your SurfaceView normally
SurfaceView surfaceView = findViewById(R.id.surfaceView);
surfaceView.getHolder().addCallback(this);

// That's it! BlurView handles the rest automatically
```

## Manual Configuration (Optional)

If you prefer to configure SurfaceView manually or need more control:

```java
SurfaceView surfaceView = findViewById(R.id.surfaceView);

// Optional: Set z-order manually (BlurView does this automatically)
surfaceView.setZOrderMediaOverlay(true);
```

## Android Version Compatibility

The BlurView uses `PixelCopy` API to capture SurfaceView content, which requires **Android 7.0 (API 24) or higher**.

For devices below API 24, the SurfaceView content will NOT be blurred.

```java
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    // PixelCopy is available - blur will work
} else {
    // PixelCopy not available - SurfaceView content won't be captured
    // Consider using TextureView instead for older devices
}
```

## Layout Ordering

Ensure your BlurView is positioned **after** the SurfaceView in your XML layout:

```xml
<FrameLayout>
    <!-- SurfaceView FIRST -->
    <SurfaceView
        android:id="@+id/surfaceView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <!-- BlurView SECOND (on top) -->
    <com.qmdeve.blurview.widget.BlurView
        android:layout_width="300dp"
        android:layout_height="200dp"
        app:blurRadius="15dp" />
</FrameLayout>
```

## Complete Example: Video Player with Blur

### Java Code

```java
public class VideoBlurActivity extends AppCompatActivity {

    private SurfaceView mVideoSurface;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_blur);

        mVideoSurface = findViewById(R.id.videoSurface);

        // No extra configuration needed! BlurView auto-configures SurfaceView
        setupVideoPlayer();
    }

    private void setupVideoPlayer() {
        SurfaceHolder holder = mVideoSurface.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                try {
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setDisplay(holder);
                    mMediaPlayer.setDataSource("your_video_path.mp4");
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder,
                                      int format, int width, int height) {
                // Handle size changes
            }
        });
    }
}
```

### XML Layout

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Video SurfaceView -->
    <SurfaceView
        android:id="@+id/videoSurface"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <!-- Blur overlay on top of video -->
    <com.qmdeve.blurview.widget.BlurView
        android:layout_width="match_parent"
        android:layout_height="200dp"
        android:layout_gravity="bottom"
        app:blurRadius="20dp"
        app:overlayColor="#60FFFFFF"
        app:cornerRadius="0dp" />

    <!-- Your UI controls on top of blur -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="200dp"
        android:layout_gravity="bottom"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Video Title"
            android:textColor="@android:color/black"
            android:textSize="24sp" />

        <!-- Add more controls -->
    </LinearLayout>

</FrameLayout>
```

## Expected Behavior

### ✅ What Will Work

1. **Android 7.0+ (API 24+)**: Blur will capture and blur the SurfaceView content with a slight lag
2. **Z-ordering**: With `setZOrderMediaOverlay(true)`, BlurView will render on top properly
3. **Dynamic content**: Video/animation frames will be continuously captured and blurred

### ⚠️ Limitations

1. **Asynchronous lag**: There will be a slight delay (usually 1-2 frames) between video content and blur
2. **Performance**: Continuous PixelCopy operations are CPU-intensive
3. **Android < 7.0**: SurfaceView content will NOT be blurred (only background will blur)

## Alternative: Use TextureView

For better performance and synchronous rendering, consider using **TextureView** instead:

### Advantages of TextureView
- Renders in normal view hierarchy (no z-order issues)
- Synchronous blur (no lag)
- Works on older Android versions
- Better integration with BlurView

### Example

```java
TextureView textureView = findViewById(R.id.textureView);
// No need for setZOrderMediaOverlay!
// BlurView will capture it synchronously
```

```xml
<TextureView
    android:id="@+id/textureView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />

<com.qmdeve.blurview.widget.BlurView
    android:layout_width="match_parent"
    android:layout_height="200dp"
    app:blurRadius="20dp" />
```

## Troubleshooting

### Problem: Blur shows black/empty content

**Causes:**
- Android version < 7.0 (API 24) - PixelCopy not available
- SurfaceView not fully initialized yet
- Auto-configuration failed (check logcat)

**Solution:**
```java
// Check logcat for auto-configuration messages
// BlurView logs: "Automatically configured SurfaceView..."

// Check Android version
if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
    Log.w(TAG, "SurfaceView blur requires Android 7.0+");
}
```

### Problem: Video appears on top of blur

**Causes:**
- Incorrect XML ordering
- Auto-configuration failed (rare)

**Solution:**
```java
// Check logcat for: "Automatically configured SurfaceView..."
// If not present, manually configure:
surfaceView.setZOrderMediaOverlay(true);
```

### Problem: Blur lags behind video

**Cause:** This is expected behavior due to asynchronous PixelCopy

**Solution:** Accept the lag or switch to TextureView for synchronous rendering

## Performance Optimization

```java
// Reduce blur update frequency for better performance
BlurView blurView = findViewById(R.id.blurView);
blurView.setDownsampleFactor(4f); // Higher = faster but less quality

// Use lower blur radius
blurView.setBlurRadius(10f); // Lower = faster
```

## Reference Implementation

See the demo app: `app/src/main/java/com/qmdeve/blurview/demo/SurfaceViewActivity.java`

This file demonstrates the proper setup for blurring animated SurfaceView content.
