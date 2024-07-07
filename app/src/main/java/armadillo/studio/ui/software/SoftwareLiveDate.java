/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.ui.software;

import android.os.Looper;

import androidx.lifecycle.LiveData;

import armadillo.studio.model.soft.UserSoft;

public class SoftwareLiveDate extends LiveData<UserSoft.data> {
    private UserSoft.data data;

    SoftwareLiveDate(UserSoft.data value) {
        super(value);
        this.data = value;
    }

    public UserSoft.data getData() {
        return data;
    }

    public void setData(UserSoft.data data) {
        this.data = data;
        if (Looper.myLooper() == Looper.getMainLooper())
            setValue(data);
        else
            postValue(data);
    }
}
