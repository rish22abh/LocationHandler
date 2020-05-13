package com.location.utils;

import androidx.annotation.IntDef;

import com.google.android.gms.location.LocationRequest;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class EnumClass {
    @IntDef({LocationPriority.PRIORITY_HIGH_ACCURACY, LocationPriority.PRIORITY_BALANCED_POWER_ACCURACY,
            LocationPriority.PRIORITY_LOW_POWER, LocationPriority.PRIORITY_NO_POWER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LocationPriority {
        int PRIORITY_HIGH_ACCURACY = LocationRequest.PRIORITY_HIGH_ACCURACY; //to request the most accurate locations available.
        int PRIORITY_BALANCED_POWER_ACCURACY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY; //to request "block" level accuracy.
        int PRIORITY_LOW_POWER = LocationRequest.PRIORITY_LOW_POWER; //to request "city" level accuracy.
        int PRIORITY_NO_POWER = LocationRequest.PRIORITY_NO_POWER; //to request the best accuracy possible with zero additional power consumption.
    }

    @IntDef({PermissionConst.PERMISSIONS_LAST_LOCATION})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PermissionConst {
        int PERMISSIONS_LAST_LOCATION = 0;
    }

    @IntDef({ActivityResultConst.DIALOG_ENABLE_LOCATION})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ActivityResultConst {
        int DIALOG_ENABLE_LOCATION = 2300;
    }

    @IntDef({TaskDialogEnableConst.DIALOG_LAST_LOCATION})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TaskDialogEnableConst {
        int DIALOG_LAST_LOCATION = 3300;
        int DIALOG_CONTINUE_LOCATION = 3301;
        int DIALOG_CONTINUE_LOCATION_BACKGROUND = 3302;
    }

    @IntDef({CallbackRequestType.LAST_LOCATION})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CallbackRequestType {
        int LAST_LOCATION = 4300;
        int CONTINUE_LOCATION = 4301;
    }
}
