package jp.mydns.dego.motionchecker.VideoPlayer;

import android.app.Activity;
import android.net.Uri;
import android.view.Display;
import android.view.Surface;

import jp.mydns.dego.motionchecker.Util.DebugLog;
import jp.mydns.dego.motionchecker.View.ViewController;

public class VideoController {

    // ---------------------------------------------------------------------------------------------
    // constant values
    // ---------------------------------------------------------------------------------------------
    private static final String TAG = "VideoController";

    // ---------------------------------------------------------------------------------------------
    // private fields
    // ---------------------------------------------------------------------------------------------
    private final ViewController viewController;
    private final VideoDecoder decoder;
    private final PlaySpeedManager speedManager;
    private Thread videoThread;

    private Video video;
    private Surface surface;

    // ---------------------------------------------------------------------------------------------
    // constructor
    // ---------------------------------------------------------------------------------------------

    /**
     * VideoController
     */
    public VideoController() {
        DebugLog.d(TAG, "VideoController");
        this.viewController = new ViewController();
        this.decoder = new VideoDecoder();
        this.speedManager = new PlaySpeedManager();

        this.decoder.setOnVideoChangeListener(new OnVideoChangeListener() {
            @Override
            public void onDurationChanged(int duration) {
                DebugLog.d(TAG, "onDurationChanged");
                DebugLog.v(TAG, "duration : " + duration);
                if (video != null) {
                    video.setDuration(duration);
                    viewController.setDuration(video.getDuration());
                }
            }

            @Override
            public void setVisibilities(VideoDecoder.DecoderStatus status) {
                viewController.setVisibilities(status);
            }
        });
    }

    // ---------------------------------------------------------------------------------------------
    // public method
    // ---------------------------------------------------------------------------------------------

    public void setViews(Activity activity) {
        this.viewController.setViews(activity);
    }

    public void bindDisplay(Display display) {
        this.viewController.bindDisplay(display);
    }

    /**
     * setVisibilities
     *
     * @param status video status
     */
    public void setVisibilities(VideoDecoder.DecoderStatus status) {
        this.viewController.setVisibilities(status);
    }

    /**
     * setViewEnable
     *
     * @param position video position
     */
    public void setViewEnable(VideoDecoder.FramePosition position) {
        DebugLog.d(TAG, "setViewEnable");
        this.viewController.updateNextPreviousViews(position);
    }

    /**
     * setProgress
     *
     * @param progress video progress
     */
    public void setProgress(int progress) {
        DebugLog.d(TAG, "setProgress");
        this.viewController.setProgress(progress);

        if (progress == 0) {
            this.setViewEnable(VideoDecoder.FramePosition.FIRST);
        } else if (this.video != null && this.video.getDuration() == progress) {
            this.setViewEnable(VideoDecoder.FramePosition.LAST);
        }
    }

    /**
     * join
     */
    public void join() {
        DebugLog.d(TAG, "join");
        if (this.videoThread != null && this.videoThread.isAlive()) {
            try {
                this.videoThread.interrupt();
                this.videoThread.join();
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * setVideo
     *
     * @param uri video uri
     * @return is set OK?
     */
    public boolean setVideo(Uri uri) {
        DebugLog.d(TAG, "setVideo");

        if (uri == null) {
            return false;
        }

        try {
            this.video = new Video(uri);
        } catch (Exception exception) {
            this.video = null;
            exception.printStackTrace();
            return false;
        }

        this.viewController.setSurfaceViewSize(this.video);
        this.viewController.setVisibilities(VideoDecoder.DecoderStatus.PAUSED);

        this.speedManager.init();
        return true;
    }

    /**
     * setSurface
     *
     * @param surface surface
     */
    public void setSurface(Surface surface) {
        DebugLog.d(TAG, "setSurface");

        if (surface != null && surface.isValid()) {
            this.surface = surface;
        } else {
            DebugLog.w(TAG, "surface is invalid.");
            this.surface = null;
            return;
        }

        this.videoSetup();
    }

    /**
     * isVideoStandby
     *
     * @return video controller is standby.
     */
    public boolean isVideoStandby() {
        return (this.video != null && this.surface != null);
    }

    /**
     * playOrPause
     */
    public void playOrPause() {
        DebugLog.d(TAG, "playOrPause");

        VideoDecoder.DecoderStatus status = this.decoder.getStatus();
        if (status == VideoDecoder.DecoderStatus.PLAYING) {
            this.pause();
        } else if (status == VideoDecoder.DecoderStatus.PAUSED) {
            this.play();
        }
    }

    /**
     * pause
     */
    public void pause() {
        DebugLog.d(TAG, "pause");

        if (this.videoThread != null && this.videoThread.isAlive()) {
            this.videoThread.interrupt();
        }
    }

    /**
     * stop
     */
    public void stop() {
        DebugLog.d(TAG, "stop");

        if (this.videoThread != null && this.videoThread.isAlive()) {
            try {
                this.videoThread.interrupt();
                this.videoThread.join();
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }

        this.decoder.release();
        this.decoder.prepare(this.video, false);

        // prepareで再生速度が初期化されるので再設定
        this.decoder.setSpeed(this.speedManager.getSpeed());

        this.threadStart();
    }

    /**
     * speedUp
     */
    public void speedUp() {
        DebugLog.d(TAG, "speedUp");

        if (this.decoder.getStatus() == VideoDecoder.DecoderStatus.PAUSED) {
            this.speedManager.speedUp();
            this.viewController.updateSpeedUpDownViews();
            this.decoder.setSpeed(this.speedManager.getSpeed());
        }
    }

    /**
     * speedDown
     */
    public void speedDown() {
        DebugLog.d(TAG, "speedDown");

        if (this.decoder.getStatus() == VideoDecoder.DecoderStatus.PAUSED) {
            this.speedManager.speedDown();
            this.viewController.updateSpeedUpDownViews();
            this.decoder.setSpeed(this.speedManager.getSpeed());
        }
    }

    /**
     * nextFrame
     */
    public void nextFrame() {
        DebugLog.d(TAG, "nextFrame");

        if (this.videoThread != null && this.videoThread.isAlive()) {
            DebugLog.i(TAG, "can not start thread. (decode thread is running.)");
            return;
        }

        if (this.decoder.getStatus() == VideoDecoder.DecoderStatus.PAUSED) {
            this.decoder.next();
            this.threadStart();
        }
    }

    /**
     * previousFrame
     */
    public void previousFrame() {
        DebugLog.d(TAG, "previousFrame");

        if (this.videoThread != null && this.videoThread.isAlive()) {
            DebugLog.i(TAG, "can not start thread. (decode thread is running.)");
            return;
        }

        if (this.decoder.getStatus() == VideoDecoder.DecoderStatus.PAUSED) {
            this.decoder.previous();
            this.threadStart();
        }
    }

    /**
     * seekTo
     *
     * @param progress video progress
     */
    public void seekTo(int progress) {
        DebugLog.d(TAG, "seekTo");
        if (decoder.getStatus() == VideoDecoder.DecoderStatus.SEEKING) {
            return;
        }
        if (this.videoThread != null && this.videoThread.isAlive()) {
            try {
                this.videoThread.join();
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }

        this.decoder.seekTo(progress);
        this.threadStart();
    }

    /**
     * getSpeedLevel
     *
     * @return speedLevel
     */
    public int getSpeedLevel() {
        return this.speedManager.getSpeedLevel();
    }

    // ---------------------------------------------------------------------------------------------
    // private method
    // ---------------------------------------------------------------------------------------------

    /**
     * videoSetup
     */
    private void videoSetup() {
        DebugLog.d(TAG, "videoSetup");

        if (this.video == null) {
            DebugLog.w(TAG, "video is null.");
            return;
        }

        if (this.video != null && surface != null) {
            if (this.decoder.init(this.video, surface)) {
                this.threadStart();
            }
        }
    }

    /**
     * play
     */
    private void play() {
        DebugLog.d(TAG, "play");

        if (this.decoder.getFramePosition() == VideoDecoder.FramePosition.LAST) {
            this.decoder.release();
            if (this.decoder.prepare(this.video, true)) {
                this.threadStart();
            }
        } else {
            this.threadStart();
        }
    }

    /**
     * threadStart
     */
    private void threadStart() {
        DebugLog.d(TAG, "threadStart");
        this.videoThread = new Thread(this.decoder);
        this.videoThread.start();
    }
}
