package com.wudongdong.butterknife;

import android.app.Activity;
import android.view.View;

/**
 * 创建人：吴冬冬<br/>
 * 创建时间：2019/12/16 19:29 <br/>
 */
public class Utils {
    public static <T extends View> T findViewById(Activity activity, int id){
        return activity.<T>findViewById(id);
    }
}
