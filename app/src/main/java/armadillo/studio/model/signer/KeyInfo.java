/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.model.signer;

public class KeyInfo {
    private String PassWord;
    private String Alias;
    private String AliasPass;
    private String Signer;

    public KeyInfo(String passWord, String alias, String aliasPass, String signer) {
        PassWord = passWord;
        Alias = alias;
        AliasPass = aliasPass;
        Signer = signer;
    }

    public String getPassWord() {
        return PassWord;
    }

    public void setPassWord(String passWord) {
        PassWord = passWord;
    }

    public String getAlias() {
        return Alias;
    }

    public void setAlias(String alias) {
        Alias = alias;
    }

    public String getAliasPass() {
        return AliasPass;
    }

    public void setAliasPass(String aliasPass) {
        AliasPass = aliasPass;
    }

    public String getSigner() {
        return Signer;
    }

    public void setSigner(String signer) {
        Signer = signer;
    }
}
