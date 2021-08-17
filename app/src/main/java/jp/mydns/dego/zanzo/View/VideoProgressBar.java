package jp.mydns.dego.zanzo.View;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

import androidx.appcompat.widget.AppCompatSeekBar;

import jp.mydns.dego.zanzo.InstanceHolder;
import jp.mydns.dego.zanzo.Util.DebugLog;

public class VideoProgressBar extends AppCompatSeekBar implements SeekBar.OnSeekBarChangeListener {

    // ---------------------------------------------------------------------------------------------
    // constant values
    // ---------------------------------------------------------------------------------------------
    private static final String TAG = "VideoProgressBar";

    // ---------------------------------------------------------------------------------------------
    // constructor
    // ---------------------------------------------------------------------------------------------

    /**
     * VideoProgressBar
     *
     * @param context context
     */
    public VideoProgressBar(Context context) {
        super(context);
        DebugLog.d(TAG, "VideoProgressBar");

        init();
    }

    /**
     * VideoProgressBar
     *
     * @param context context
     * @param attrs   attribute set
     */
    public VideoProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        DebugLog.d(TAG, "VideoProgressBar");

        init();
    }

    // ---------------------------------------------------------------------------------------------
    // public method
    // ---------------------------------------------------------------------------------------------

    /**
     * onProgressChanged
     *
     * @param seekBar  seekBar
     * @param progress progress
     * @param fromUser is changed from user
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        DebugLog.d(TAG, "onProgressChanged");
        if (fromUser) {
            InstanceHolder.getInstance().getVideoController().seekTo(progress, false);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        DebugLog.d(TAG, "onStartTrackingTouch");
        InstanceHolder.getInstance().getVideoController().pause();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        DebugLog.d(TAG, "onStopTrackingTouch");
    }

    // ---------------------------------------------------------------------------------------------
    // private method
    // ---------------------------------------------------------------------------------------------

    private void init() {
        this.setOnSeekBarChangeListener(this);
    }
}