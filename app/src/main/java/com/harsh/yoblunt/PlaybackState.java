package com.harsh.yoblunt;

import com.google.android.exoplayer2.C;

/**
 * Created by Anjan on 6/11/2017.
 */

public class PlaybackState {
    private int mResumeWindow;
    private long mResumePosition;

    public PlaybackState(){
        mResumeWindow = C.INDEX_UNSET;
        mResumePosition = C.TIME_UNSET;
    }

    public void setResumeWindow(int resumeWindow){
        mResumeWindow = resumeWindow;
    }

    public void setResumePosition(long resumePosition){
        mResumePosition = resumePosition;
    }

    public int getResumeWindow() {
        return mResumeWindow;
    }

    public long getResumePosition() {
        return mResumePosition;
    }
}
