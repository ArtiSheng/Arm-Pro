/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.model.sys;

import armadillo.studio.model.Basic;

public class Other extends Basic {
    private data data;

    public static class data {
        private int total_task;
        private int total_apps;
        private int day_task;
        private String card_buy_url;
        private int group;
        private String telegram_url;
        private String card_price;

        public int getTotal_task() {
            return total_task;
        }

        public int getTotal_apps() {
            return total_apps;
        }

        public int getDay_task() {
            return day_task;
        }

        public String getCard_buy_url() {
            return card_buy_url;
        }

        public int getGroup() {
            return group;
        }

        public String getTelegram_url() {
            return telegram_url;
        }

        public String getCard_price() {
            return card_price;
        }
    }

    public Other.data getData() {
        return data;
    }
}
