/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package armadillo.studio.helper;

import com.google.gson.JsonObject;

import java.util.List;

import armadillo.studio.common.manager.UserDetailManager;
import armadillo.studio.model.handle.Node;

public class InjectInfo {
    private List<Node> handleEnums;
    private String uuid;
    private JsonObject rule;
    private String token;
    private String md5;
    public InjectInfo(List<Node> handleEnums, String uuid, JsonObject rule, String md5) {
        this.handleEnums = handleEnums;
        this.uuid = uuid;
        this.rule = rule;
        this.token = UserDetailManager.getInstance().getCookie();
        this.md5 = md5;
    }

    public List<Node> getHandleEnums() {
        return handleEnums;
    }

    public void setHandleEnums(List<Node> handleEnums) {
        this.handleEnums = handleEnums;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public JsonObject getRule() {
        return rule;
    }

    public void setRule(JsonObject rule) {
        this.rule = rule;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
