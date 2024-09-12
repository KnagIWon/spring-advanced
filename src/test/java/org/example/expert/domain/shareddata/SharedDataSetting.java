package org.example.expert.domain.shareddata;

import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;

public class SharedDataSetting {
    public final static Long TEST_ID1 = 1L;
    public final static String TEST_EMAIL1 = "TestUser1@email.com";
    public final static String TEST_PASSWORD1 = "TestPassword1";
    public final static UserRole TEST_USERROLE1 = UserRole.valueOf("ADMIN");
    public final static User TEST_USER1 = new User(TEST_EMAIL1, TEST_PASSWORD1, TEST_USERROLE1);

    public final static Long TEST_ID2 = 2L;
    public final static String TEST_EMAIL2 = "TestUser2@email.com";
    public final static String TEST_PASSWORD2 = "TestPassword2";
    public final static UserRole TEST_USERROLE2 = UserRole.valueOf("USER");
    public final static User TEST_USER2 = new User(TEST_EMAIL2, TEST_PASSWORD2, TEST_USERROLE2);

    // Auth
    public final static SignupRequest SIGNUP_REQUEST1 = new SignupRequest();
    public final static SignupRequest SIGNUP_REQUEST2 = new SignupRequest();
    public final static SigninRequest SIGNIN_REQUEST1 = new SigninRequest();
    public final static SigninRequest SIGNIN_REQUEST2 = new SigninRequest();
}
