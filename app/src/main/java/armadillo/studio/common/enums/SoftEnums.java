/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.common.enums;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import armadillo.studio.CloudApp;
import armadillo.studio.R;

public enum SoftEnums {
    SingleVerify(0x1, CloudApp.getContext().getString(R.string.soft_mode_single)),
    DrainageGroup(0x2, CloudApp.getContext().getString(R.string.soft_mode_group)),
    LoginVerify(0x4, CloudApp.getContext().getString(R.string.soft_mode_login)),
    Notice(0x8, CloudApp.getContext().getString(R.string.soft_mode_notice)),
    Update(0x10, CloudApp.getContext().getString(R.string.soft_mode_update)),
    CustomModule(0x20, CloudApp.getContext().getString(R.string.soft_mode_custom)),
    Share(0x40, CloudApp.getContext().getString(R.string.soft_mode_share)),
    Admob(0x80, CloudApp.getContext().getString(R.string.soft_mode_admob));
    private final int type;
    private final String desc;

    SoftEnums(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }


    private final static SoftEnums[] allFlags;

    static {
        allFlags = SoftEnums.values();
    }

    @NotNull
    @Contract(pure = true)
    public static SoftEnums[] getFlags(int FlagValue) {
        int size = 0;
        for (SoftEnums Flag : allFlags) {
            if ((FlagValue & Flag.type) != 0) {
                size++;
            }
        }
        SoftEnums[] Flags = new SoftEnums[size];
        int FlagsPosition = 0;
        for (SoftEnums Flag : allFlags) {
            if ((FlagValue & Flag.type) != 0) {
                Flags[FlagsPosition++] = Flag;
            }
        }
        return Flags;
    }

    @Nullable
    public static SoftEnums getFor(String desc) {
        for (SoftEnums value : values()) {
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
