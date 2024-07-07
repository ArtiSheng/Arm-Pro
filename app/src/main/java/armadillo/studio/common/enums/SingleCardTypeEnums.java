/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.common.enums;

import armadillo.studio.CloudApp;
import armadillo.studio.R;

public enum SingleCardTypeEnums {
    minute(0x1, CloudApp.getContext().getString(R.string.single_card_type_minute)),//分钟
    hour(0x2, CloudApp.getContext().getString(R.string.single_card_type_hour)),//小时
    day(0x3, CloudApp.getContext().getString(R.string.single_card_type_day)),//天
    week(0x4, CloudApp.getContext().getString(R.string.single_card_type_week)),//周
    month(0x5, CloudApp.getContext().getString(R.string.single_card_type_month)),//月
    year(0x6, CloudApp.getContext().getString(R.string.single_card_type_year));//年
    private final int type;
    private final String desc;

    SingleCardTypeEnums(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }


    private final static SingleCardTypeEnums[] allFlags;

    static {
        allFlags = SingleCardTypeEnums.values();
    }

    public static String getFlags(int type) {
        for (SingleCardTypeEnums flag : allFlags) {
            if (flag.getType() == type)
                return flag.getDesc();
        }
        return "Unknown Card";
    }

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
