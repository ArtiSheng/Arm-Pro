package armadillo.studio.common.enums;

public enum SocketTypeEnums {
    SUBMITTASK(520),
    GETUPLOADTOKEN(550),
    LOCALUPLOAD(560),
    GETTASKINFO(9000),
    FREETASK(9002),
    GETlATNOTICE(1000),
    GETALLNOTICE(1001),
    GETNEWVER(1002),
    GETHELPER(1004),
    GETHANDLE(1005),
    QQLOGIN(2000),
    TOKENCHECK(2001),
    USERPAY(2002),
    USERLOGIN(2003),
    USERREG(2004),
    USERRET(2005),
    USERCHANGEPASS(2006),
    GETOTHER(2222),
    GETSOFT(3000),
    SAVESOFTHANDLE(3001),
    GETSOFTMODELINFO(3002),
    SAVEMODELINFO(3003),
    SINGLECARDMANAGEN(3004),
    SINGLETRIALMANAGEN(3005),
    DELETESOFT(3100);

    SocketTypeEnums(int type) {
        this.type = type;
    }

    private int type;

    public int getType() {
        return type;
    }
}
