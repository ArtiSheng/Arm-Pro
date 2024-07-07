package armadillo.studio.model.sys;

import org.jetbrains.annotations.NotNull;

import armadillo.studio.model.Basic;

public class User extends Basic {
    private data data;

    public static class data {
        private Integer id;

        private String username;

        private String token;

        private Integer loginCount;

        private String expireTime;

        private Integer value;

        public String getToken() {
            return token;
        }

        public String getUsername() {
            return username;
        }

        public Integer getLoginCount() {
            return loginCount;
        }

        public String getExpireTime() {
            return expireTime;
        }

        public Integer getValue() {
            return value;
        }

        public Integer getId() {
            return id;
        }

        @Override
        public String toString() {
            return "data{" +
                    "id=" + id +
                    ", username='" + username + '\'' +
                    ", token='" + token + '\'' +
                    ", loginCount=" + loginCount +
                    ", expireTime='" + expireTime + '\'' +
                    ", value=" + value +
                    '}';
        }
    }

    public User.data getData() {
        return data;
    }

    public void setData(User.data data) {
        this.data = data;
    }

    @NotNull
    @Override
    public String toString() {
        return "User{" +
                "data=" + data +
                '}';
    }
}
