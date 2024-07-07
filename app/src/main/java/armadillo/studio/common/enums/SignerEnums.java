/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.common.enums;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum SignerEnums {
    V1(0x1),
    V2(0x2);
    private final int type;

    SignerEnums(int type) {
        this.type = type;
    }


    private final static SignerEnums[] allFlags;

    static {
        allFlags = SignerEnums.values();
    }

    @NotNull
    @Contract(pure = true)
    public static SignerEnums[] getFlags(int FlagValue) {
        int size = 0;
        for (SignerEnums Flag : allFlags) {
            if ((FlagValue & Flag.type) != 0) {
                size++;
            }
        }
        SignerEnums[] Flags = new SignerEnums[size];
        int FlagsPosition = 0;
        for (SignerEnums Flag : allFlags) {
            if ((FlagValue & Flag.type) != 0) {
                Flags[FlagsPosition++] = Flag;
            }
        }
        return Flags;
    }


    public int getType() {
        return type;
    }

}
