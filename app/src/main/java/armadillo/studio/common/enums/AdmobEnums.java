/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.enums;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import armadillo.studio.CloudApp;
import armadillo.studio.R;

public enum  AdmobEnums {
    Banner(0x1,CloudApp.getContext().getString(R.string.admob_banner)),
    interstitial(0x2, CloudApp.getContext().getString(R.string.admob_interstitial)),
    rewarded(0x4, CloudApp.getContext().getString(R.string.admob_rewarded));
    private final int type;
    private final String desc;

    AdmobEnums(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }


    private final static AdmobEnums[] allFlags;

    static {
        allFlags = AdmobEnums.values();
    }

    @NotNull
    @Contract(pure = true)
    public static AdmobEnums[] getFlags(int FlagValue) {
        int size = 0;
        for (AdmobEnums Flag : allFlags) {
            if ((FlagValue & Flag.type) != 0) {
                size++;
            }
        }
        AdmobEnums[] Flags = new AdmobEnums[size];
        int FlagsPosition = 0;
        for (AdmobEnums Flag : allFlags) {
            if ((FlagValue & Flag.type) != 0) {
                Flags[FlagsPosition++] = Flag;
            }
        }
        return Flags;
    }

    @Nullable
    public static AdmobEnums getFor(String desc) {
        for (AdmobEnums value : values()) {
            if (value.getDesc().equals(desc))
                return value;
        }
        return null;
    }

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

}
