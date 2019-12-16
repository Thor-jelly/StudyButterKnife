package com.wudongdong.butterknife;

import androidx.annotation.UiThread;

/**
 * 创建人：吴冬冬<br/>
 * 创建时间：2019/12/16 17:50 <br/>
 */
public interface Unbinder {
    @UiThread
    void unbind();

    Unbinder EMPTY = new Unbinder() {
        @Override
        public void unbind() {

        }
    };
}
