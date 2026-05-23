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

    public static final String USER_BASE = PRE_FIX;
    public static final String GET_USERS = PRE_FIX;
    public static final String GET_USER = PRE_FIX + "/{userId}";
    public static final String GET_CURRENT_USER = PRE_FIX + "/me/profile";
    public static final String UPDATE_PROFILE = PRE_FIX + "/me/profile";
    public static final String CHANGE_PASSWORD = PRE_FIX + "/me/password";
    public static final String UPLOAD_AVATAR = PRE_FIX + "/me/avatar";
    public static final String LOCK_USER = PRE_FIX + "/{userId}/lock";
    public static final String UNLOCK_USER = PRE_FIX + "/{userId}/unlock";
    public static final String CHANGE_ROLE = PRE_FIX + "/{userId}/role";
    public static final String STATISTICS = PRE_FIX + "/statistics";

    private User() {
    }
  }

  public static class Company{
    public static final String COMPANY_BASE = "/company";
    public static final String ID = COMPANY_BASE+"/{id}";
    public static final String ME = COMPANY_BASE+"/me";
  }

  public static class Skill{
    public static final String SKILL_BASE="/skill";
    public static final String ID= SKILL_BASE +"/{id}";
    public static final String ME=SKILL_BASE+"/me";
  }

  public static class Job{
    private static final String PRE_FIX = "/job";

    public static final String JOB_BASE = PRE_FIX;
    public static final String ID = PRE_FIX + "/{id}";
    public static final String PUBLISH = PRE_FIX + "/{id}/publish";
    public static final String UNPUBLISH = PRE_FIX + "/{id}/unpublish";
    public static final String STATISTICS = PRE_FIX + "/statistics";
    public static final String MY_JOBS = PRE_FIX + "/me";

    private Job() {
    }
  }

  public static class JobApplication{
    private static final String PRE_FIX = "/job";

    public static final String APPLY = PRE_FIX + "/{jobId}/apply";
    public static final String WITHDRAW = PRE_FIX + "/{jobId}/apply";
    public static final String JOB_APPLICATIONS = PRE_FIX + "/{jobId}/applications";
    public static final String MY_APPLICATIONS = "/applications/me";
    public static final String UPDATE_STATUS = "/applications/{applicationId}/status";

    private JobApplication() {
    }
  }

  public static class FavoriteJob{
    private static final String PRE_FIX = "/job";

    public static final String SAVE = PRE_FIX + "/{jobId}/favorite";
    public static final String REMOVE = PRE_FIX + "/{jobId}/favorite";
    public static final String MY_FAVORITES = "/jobs/favorites";

    private FavoriteJob() {
    }
  }

  public static class Resume{
    private static final String PRE_FIX = "/resume";

    public static final String RESUME_BASE = PRE_FIX;
    public static final String ID = PRE_FIX + "/{id}";
    public static final String FILE = PRE_FIX + "/{id}/file";
    public static final String MY_RESUMES = PRE_FIX + "/me";
    public static final String ADMIN_RESUMES = PRE_FIX + "/admin";
    public static final String DEFAULT = PRE_FIX + "/{id}/default";
    public static final String DOWNLOAD = PRE_FIX + "/{id}/download";
    public static final String RECRUITER_CANDIDATE_RESUME = "/job/{jobId}/candidates/{candidateId}/resume";
    public static final String RECRUITER_DOWNLOAD = "/job/{jobId}/candidates/{candidateId}/resume/download";

    private Resume() {
    }
  }

}
