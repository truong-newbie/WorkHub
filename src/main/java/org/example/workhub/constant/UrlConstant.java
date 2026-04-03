package org.example.workhub.constant;

public class UrlConstant {

  public static class ForgotPassword {
    public static final String PREFIX= "/forgot-password";
    public static final String VERIFY_EMAIL =PREFIX+ "/email-verification/{email}";
    public static final String VERIFY_OTP = PREFIX+"/otp-verification";
    public static final String RESET_PASSWORD =PREFIX+ "/password-update/{email}";
  }
  public static class Auth {

    private static final String PRE_FIX = "/auth";
    public static final String REGISTER = PRE_FIX + "/register";
    public static final String LOGIN = PRE_FIX + "/login";
    public static final String LOGOUT = PRE_FIX + "/logout";
    public static final String OAUTH2_AUTHORIZE = PRE_FIX + "/oauth2/authorize";
    public static final String OAUTH2_CALLBACK = PRE_FIX + "/oauth2/callback";

    private Auth() {
    }
  }

  public static class User {
    private static final String PRE_FIX = "/user";

    public static final String GET_USERS = PRE_FIX;
    public static final String GET_USER = PRE_FIX + "/{id}";
    public static final String GET_CURRENT_USER = PRE_FIX + "/current";
    public static final String CREATE_USER = PRE_FIX;
    public static final String UPDATE_USER = PRE_FIX;
    public static final String DELETE_USER = PRE_FIX + "/{id}";


    private User() {
    }
  }

  public static class Job {
    private static final String PRE_FIX = "/job";

    public static final String GET_JOBS = PRE_FIX;
    public static final String GET_JOB = PRE_FIX + "/{id}";
    public static final String CREATE_JOB = PRE_FIX;
    public static final String UPDATE_JOB = PRE_FIX;
    public static final String DELETE_JOB = PRE_FIX + "/{id}";


    private Job() {
    }
  }

}
