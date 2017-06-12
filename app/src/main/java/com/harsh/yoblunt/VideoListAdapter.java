package com.harsh.yoblunt;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;

import java.lang.ref.WeakReference;

/**
 * Created by Anjan on 6/11/2017.
 */

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> {

    public static final String TAG = "VideoAdapter";
    private final RecyclerView mRecyclerView;
    private MediaPlayer mMediaPlayer;
    private Uri[] mUris;
    private int mLastPlayPos = -1;
    private ViewHolder mLastViewHolder;
    private ViewHolder mCurrentViewHolder;
    private PlaybackState[] mPlaybackStates;
    private boolean isFirstPlayed;
    private LinearLayoutManager mLinearLayoutManager;

    public VideoListAdapter(RecyclerView recyclerView, MediaPlayer mediaPlayer, Uri[] uris) {
        mRecyclerView = recyclerView;
        mUris = uris;
        mMediaPlayer = mediaPlayer;
        mMediaPlayer.addEventListener(mEventListener);
        initPlaybackState();
        mLinearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isFirstPlayed) {
                    play(0);
                    isFirstPlayed = true;
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int pos = mLinearLayoutManager.findFirstVisibleItemPosition();
                    View v = mLinearLayoutManager.findViewByPosition(pos);
                    int visibleItemPos = getCompleteVisibleItem(v, pos);
                    Log.d("VideoAdapter", "Visible View: " + visibleItemPos);
                    if (mLastPlayPos != visibleItemPos) {
                        if (mMediaPlayer.isPlaying) {
                            savePlaybackState(mLastPlayPos);
                            mMediaPlayer.stopPlayer();
                        }
                        play(visibleItemPos);
                    }
                }
            }

        });
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    private void play(int visibleItemPos) {
        ViewHolder vh = new ViewHolder(mLinearLayoutManager.findViewByPosition(visibleItemPos));
        mMediaPlayer.play(mUris[visibleItemPos], new WeakReference<>(vh.mTextureView),
                mPlaybackStates[visibleItemPos]);
        updateView(mLastViewHolder, false, true);
        updateView(vh, true, false);
        mLastViewHolder = vh;
        mLastPlayPos = visibleItemPos;
    }

    @Override
    public int getItemCount() {
        return mUris.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextureView mTextureView;
        private ProgressBar mProgressBar;
        private ImageView mVideoIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextureView = (TextureView) itemView.findViewById(R.id.texture_view);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
            mVideoIcon = (ImageView) itemView.findViewById(R.id.ic_video);
        }
    }

    private int getCompleteVisibleItem(View firstVisibleView, int firstVisibleViewPosition) {
        RecyclerView.LayoutParams recyclerViewLP = (RecyclerView.LayoutParams) firstVisibleView.getLayoutParams();
        int reqVisibleHeight = (int) (recyclerViewLP.height * 0.75f);
        int currentHeight = (int) (recyclerViewLP.height + firstVisibleView.getY());
        return currentHeight >= reqVisibleHeight ? firstVisibleViewPosition :
                (mUris.length > firstVisibleViewPosition + 1 ? firstVisibleViewPosition + 1 : firstVisibleViewPosition);
    }

    private EventListener mEventListener = new EventListener() {


        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            super.onPlayerStateChanged(playWhenReady, playbackState);
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    updateView(mLastViewHolder, false, false);
                    break;
                case ExoPlayer.STATE_READY:
                    updateView(mLastViewHolder, false, false);
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    updateView(mLastViewHolder, true, false);
                    break;
                case ExoPlayer.STATE_ENDED:
                    updateView(mLastViewHolder, false, true);
                    resetPlaybackState(mLastPlayPos);
                    break;
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            super.onPlayerError(error);
            setMediaPlayFailed(error);
            resetPlaybackState(mLastPlayPos);
        }
    };


    private void updateView(final ViewHolder vh, final boolean showProgress, final boolean showVideoIcon) {
        if (vh == null) {
            return;
        }
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                vh.mProgressBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
                vh.mVideoIcon.setVisibility(showVideoIcon ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void initPlaybackState() {
        mPlaybackStates = new PlaybackState[mUris.length];
        for (int i = 0; i < mUris.length; i++) {
            mPlaybackStates[i] = new PlaybackState();
        }
    }

    private void savePlaybackState(int position) {
        PlaybackState playbackState = mPlaybackStates[position];
        playbackState.setResumeWindow(mMediaPlayer.getPlayer().getCurrentWindowIndex());
        playbackState.setResumePosition(mMediaPlayer.getPlayer().isCurrentWindowSeekable() ?
                Math.max(0, mMediaPlayer.getPlayer().getCurrentPosition()) : C.TIME_UNSET);
    }

    private void resetPlaybackState(int position) {
        PlaybackState playbackState = mPlaybackStates[position];
        playbackState.setResumeWindow(C.INDEX_UNSET);
        playbackState.setResumePosition(C.TIME_UNSET);
    }

    private void setMediaPlayFailed(ExoPlaybackException error) {
        updateView(mLastViewHolder, false, false);
        String errMsg = "";
        if (error.type == ExoPlaybackException.TYPE_RENDERER) {
            errMsg = "Error rendering video.";
        } else if (error.type == ExoPlaybackException.TYPE_SOURCE) {
            errMsg = "Error reading source.";
        } else if (error.type == ExoPlaybackException.TYPE_UNEXPECTED) {
            errMsg = "Error unknown.";
        }
        showPlaybackError(String.format("%s\n%s", mLastViewHolder.mProgressBar.getContext()
                .getString(R.string.err_video_play), errMsg));
    }

    private void showPlaybackError(final String error) {
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mRecyclerView.getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

}
