package com.ss.android.ugc.aweme;

public interface IAccountUserService {

    com.ss.android.ugc.aweme.profile.model.User getCurUser();

    String getCurUserId();

    String getCurSecUserId();
}
